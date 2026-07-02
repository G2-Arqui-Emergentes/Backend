package pe.edu.upc.taskmaster.backend.ai.interfaces.rest.transform;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.edu.upc.taskmaster.backend.ai.domain.model.aggregates.AIInsight;
import pe.edu.upc.taskmaster.backend.ai.domain.model.valueobjects.AIRecommendation;
import pe.edu.upc.taskmaster.backend.ai.domain.model.valueobjects.PerformanceLevel;
import pe.edu.upc.taskmaster.backend.ai.interfaces.rest.resources.DashboardResponseResource;
import pe.edu.upc.taskmaster.backend.ai.interfaces.rest.resources.MemberPerformanceResource;
import pe.edu.upc.taskmaster.backend.ai.interfaces.rest.resources.ProjectRiskResource;
import pe.edu.upc.taskmaster.backend.iam.domain.model.aggregates.User;
import pe.edu.upc.taskmaster.backend.ai.domain.model.valueobjects.RiskLevel;
import pe.edu.upc.taskmaster.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import pe.edu.upc.taskmaster.backend.task.domain.model.aggregates.Task;
import pe.edu.upc.taskmaster.backend.task.infrastructure.persistence.jpa.repositories.TaskRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DashboardResponseFromQueryAssembler {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public DashboardResponseResource toResource(List<AIInsight> insights, Long leaderId) {
        if (insights == null || insights.isEmpty()) {
            return new DashboardResponseResource(
                    null,
                    List.of("No tienes proyectos asignados"),
                    Collections.emptyList(),
                    LocalDate.now().toString(),
                    0,
                    0
            );
        }

        AIInsight topRisk = insights.get(0);

        ProjectRiskResource projectRisk = new ProjectRiskResource(
                topRisk.getProjectId(),
                topRisk.getProjectName(),
                "ACTIVE",
                topRisk.getDelayRisk(),
                topRisk.getOverallEfficiency(),
                topRisk.getRiskLevel().name(),
                topRisk.getTotalTasks(),
                topRisk.getCompletedTasks(),
                topRisk.getDelayedTasks(),
                topRisk.getInProgressTasks()
        );

        List<String> recommendations = topRisk.getRecommendations().stream()
                .map(AIRecommendation::text)
                .collect(Collectors.toList());

        List<MemberPerformanceResource> memberPerformances = calculateMemberPerformances(topRisk.getProjectId());

        int totalProjects = insights.size();
        int highRiskProjects = (int) insights.stream()
                .filter(i -> i.getRiskLevel() == RiskLevel.HIGH)
                .count();

        return new DashboardResponseResource(
                projectRisk,
                recommendations,
                memberPerformances,
                LocalDate.now().toString(),
                totalProjects,
                highRiskProjects
        );
    }

    private List<MemberPerformanceResource> calculateMemberPerformances(Long projectId) {
        try {
            List<Task> tasks = taskRepository.findByProjectId(projectId);

            Set<User> assignedUsers = new HashSet<>();
            for (Task task : tasks) {
                if (task.getAssignedUsers() != null) {
                    assignedUsers.addAll(task.getAssignedUsers());
                }
            }

            if (assignedUsers.isEmpty()) {
                return Collections.emptyList();
            }

            List<MemberPerformanceResource> performances = new ArrayList<>();
            for (User user : assignedUsers) {
                List<Task> userTasks = tasks.stream()
                        .filter(task -> task.getAssignedUsers() != null &&
                                task.getAssignedUsers().contains(user))
                        .collect(Collectors.toList());

                if (userTasks.isEmpty()) continue;

                long completed = userTasks.stream()
                        .filter(task -> "DONE".equals(task.getStatus().toString()))
                        .count();

                long delayed = userTasks.stream()
                        .filter(task -> {
                            Date endDate = task.getEndDate();
                            if (endDate == null) return false;
                            LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            return endLocalDate.isBefore(LocalDate.now()) &&
                                    !"DONE".equals(task.getStatus().toString());
                        })
                        .count();

                double score = calculateMemberScore(userTasks, completed, delayed);
                PerformanceLevel level = PerformanceLevel.fromScore(score);

                performances.add(new MemberPerformanceResource(
                        user.getId(),
                        user.getName() + " " + user.getLastName(),
                        level.name(),
                        Math.round(score * 100.0) / 100.0,
                        (int) completed,
                        (int) delayed
                ));
            }

            performances.sort((a, b) -> Double.compare(b.score(), a.score()));
            return performances;

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private double calculateMemberScore(List<Task> tasks, long completed, long delayed) {
        if (tasks.isEmpty()) return 0.0;

        double completionScore = (double) completed / tasks.size() * 60;

        double onTimeBonus = 0.0;
        if (completed > 0) {
            long onTime = tasks.stream()
                    .filter(task -> "DONE".equals(task.getStatus().toString()))
                    .filter(task -> {
                        Date endDate = task.getEndDate();
                        if (endDate == null) return true;
                        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        return !endLocalDate.isBefore(LocalDate.now());
                    })
                    .count();
            onTimeBonus = (double) onTime / completed * 30;
        }

        double delayPenalty = (double) delayed / tasks.size() * 40;

        long inProgress = tasks.stream()
                .filter(task -> "IN_PROGRESS".equals(task.getStatus().toString()))
                .count();
        double progressBonus = (double) inProgress / tasks.size() * 10;

        return Math.max(0, Math.min(100, completionScore + onTimeBonus + progressBonus - delayPenalty));
    }
}
