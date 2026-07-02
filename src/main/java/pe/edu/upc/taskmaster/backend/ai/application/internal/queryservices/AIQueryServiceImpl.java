package pe.edu.upc.taskmaster.backend.ai.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upc.taskmaster.backend.ai.domain.model.aggregates.AIInsight;
import pe.edu.upc.taskmaster.backend.ai.domain.model.aggregates.MemberWeeklySummary;
import pe.edu.upc.taskmaster.backend.ai.domain.model.queries.GetLeaderDashboardQuery;
import pe.edu.upc.taskmaster.backend.ai.domain.model.queries.GetMemberDashboardQuery;
import pe.edu.upc.taskmaster.backend.ai.domain.model.valueobjects.AIRecommendation;
import pe.edu.upc.taskmaster.backend.ai.domain.model.valueobjects.RiskLevel;
import pe.edu.upc.taskmaster.backend.ai.domain.model.valueobjects.SmartVelocity;
import pe.edu.upc.taskmaster.backend.ai.domain.services.AIQueryService;
import pe.edu.upc.taskmaster.backend.ai.infrastructure.client.GeminiApiClient;
import pe.edu.upc.taskmaster.backend.iam.domain.model.aggregates.User;
import pe.edu.upc.taskmaster.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import pe.edu.upc.taskmaster.backend.project.domain.model.aggregates.Project;
import pe.edu.upc.taskmaster.backend.project.infrastructure.persistence.jpa.repositories.ProjectRepository;
import pe.edu.upc.taskmaster.backend.task.domain.model.aggregates.Task;
import pe.edu.upc.taskmaster.backend.task.infrastructure.persistence.jpa.repositories.TaskRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIQueryServiceImpl implements AIQueryService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final GeminiApiClient geminiClient;

    private static final String STATUS_DONE = "DONE";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_TO_DO = "TO_DO";

    @Override
    public List<AIInsight> handle(GetLeaderDashboardQuery query) {
        List<Project> projects = projectRepository.findByLeaderId(query.leaderId());

        if (projects.isEmpty()) {
            String prompt = "No tienes proyectos asignados como líder. Genera un mensaje corto (maximo 50 caracteres) recomendando crear un proyecto. Responde SOLO con el mensaje.";
            String response = geminiClient.generateContent(prompt);
            String message = (response != null && !response.isEmpty() && !response.contains("Error")) ?
                    cleanResponse(response) :
                    "No tienes proyectos asignados. Crea tu primer proyecto para comenzar.";

            AIInsight emptyInsight = AIInsight.builder()
                    .projectId(0L)
                    .projectName("Sin proyectos")
                    .delayRisk(0.0)
                    .overallEfficiency(0.0)
                    .riskLevel(RiskLevel.LOW)
                    .totalTasks(0)
                    .completedTasks(0)
                    .delayedTasks(0)
                    .inProgressTasks(0)
                    .recommendations(List.of(new AIRecommendation(message, "INFO", 1)))
                    .generatedAt(LocalDateTime.now())
                    .build();

            return List.of(emptyInsight);
        }

        List<AIInsight> allInsights = projects.stream()
                .map(this::createInsightForProject)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<AIInsight> insightsWithTasks = allInsights.stream()
                .filter(insight -> insight.getTotalTasks() > 0)
                .collect(Collectors.toList());

        if (insightsWithTasks.isEmpty()) {
            String projectNames = allInsights.stream()
                    .map(AIInsight::getProjectName)
                    .collect(Collectors.joining(", "));

            String prompt = String.format("""
                    Los proyectos '%s' no tienen tareas asignadas.
                    Genera un mensaje corto (maximo 80 caracteres) recomendando crear tareas para avanzar.
                    Responde SOLO con el mensaje.
                    """, projectNames);

            String response = geminiClient.generateContent(prompt);
            String message = (response != null && !response.isEmpty() && !response.contains("Error")) ?
                    cleanResponse(response) :
                    "Tus proyectos no tienen tareas. Crea tareas para comenzar a trabajar.";

            AIInsight noTasksInsight = AIInsight.builder()
                    .projectId(allInsights.get(0).getProjectId())
                    .projectName(allInsights.get(0).getProjectName())
                    .delayRisk(0.0)
                    .overallEfficiency(100.0)
                    .riskLevel(RiskLevel.LOW)
                    .totalTasks(0)
                    .completedTasks(0)
                    .delayedTasks(0)
                    .inProgressTasks(0)
                    .recommendations(List.of(new AIRecommendation(message, "ACTION", 1)))
                    .generatedAt(LocalDateTime.now())
                    .build();

            return List.of(noTasksInsight);
        }

        return insightsWithTasks.stream()
                .sorted((a, b) -> Double.compare(b.getDelayRisk(), a.getDelayRisk()))
                .limit(1)
                .collect(Collectors.toList());
    }

    private AIInsight createInsightForProject(Project project) {
        try {
            List<Task> tasks = taskRepository.findByProjectId(project.getId());

            if (tasks.isEmpty()) {
                String prompt = String.format("""
                        El proyecto '%s' no tiene tareas asignadas.
                        Genera un mensaje corto (maximo 80 caracteres) recomendando al lider crear tareas.
                        Responde SOLO con el mensaje.
                        """, project.getName());

                String response = geminiClient.generateContent(prompt);
                String message = (response != null && !response.isEmpty() && !response.contains("Error")) ?
                        cleanResponse(response) :
                        "No hay tareas en '" + project.getName() + "'. Crea tareas para avanzar.";

                return AIInsight.builder()
                        .projectId(project.getId())
                        .projectName(project.getName())
                        .delayRisk(0.0)
                        .overallEfficiency(100.0)
                        .riskLevel(RiskLevel.LOW)
                        .totalTasks(0)
                        .completedTasks(0)
                        .delayedTasks(0)
                        .inProgressTasks(0)
                        .recommendations(List.of(new AIRecommendation(message, "ACTION", 1)))
                        .generatedAt(LocalDateTime.now())
                        .build();
            }

            long totalTasks = tasks.size();
            long completedTasks = tasks.stream()
                    .filter(task -> STATUS_DONE.equals(task.getStatus().toString()))
                    .count();
            long inProgressTasks = tasks.stream()
                    .filter(task -> STATUS_IN_PROGRESS.equals(task.getStatus().toString()))
                    .count();
            long toDoTasks = tasks.stream()
                    .filter(task -> STATUS_TO_DO.equals(task.getStatus().toString()))
                    .count();
            long delayedTasks = tasks.stream()
                    .filter(task -> {
                        Date endDate = task.getEndDate();
                        if (endDate == null) return false;
                        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        return endLocalDate.isBefore(LocalDate.now()) && !STATUS_DONE.equals(task.getStatus().toString());
                    })
                    .count();

            double delayRisk = calculateDelayRisk(tasks);
            double efficiency = calculateEfficiency(tasks, completedTasks);
            RiskLevel riskLevel = RiskLevel.fromDelayRisk(delayRisk);

            List<AIRecommendation> recommendations = generateLeaderRecommendation(project, tasks);

            return AIInsight.builder()
                    .projectId(project.getId())
                    .projectName(project.getName())
                    .delayRisk(Math.round(delayRisk * 100.0) / 100.0)
                    .overallEfficiency(Math.round(efficiency * 100.0) / 100.0)
                    .riskLevel(riskLevel)
                    .totalTasks((int) totalTasks)
                    .completedTasks((int) completedTasks)
                    .delayedTasks((int) delayedTasks)
                    .inProgressTasks((int) inProgressTasks)
                    .recommendations(recommendations)
                    .generatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error creando insight para proyecto: {}", project.getId(), e);
            String prompt = String.format("""
                    Hubo un error al analizar el proyecto '%s'.
                    Genera un mensaje corto (maximo 80 caracteres) recomendando revisar el proyecto manualmente.
                    Responde SOLO con el mensaje.
                    """, project.getName());

            String response = geminiClient.generateContent(prompt);
            String message = (response != null && !response.isEmpty() && !response.contains("Error")) ?
                    cleanResponse(response) :
                    "Error al analizar '" + project.getName() + "'. Revisa manualmente.";

            return AIInsight.builder()
                    .projectId(project.getId())
                    .projectName(project.getName())
                    .delayRisk(0.0)
                    .overallEfficiency(0.0)
                    .riskLevel(RiskLevel.LOW)
                    .totalTasks(0)
                    .completedTasks(0)
                    .delayedTasks(0)
                    .inProgressTasks(0)
                    .recommendations(List.of(new AIRecommendation(message, "INFO", 1)))
                    .generatedAt(LocalDateTime.now())
                    .build();
        }
    }

    private List<AIRecommendation> generateLeaderRecommendation(Project project, List<Task> tasks) {
        long total = tasks.size();

        long completed = tasks.stream()
                .filter(t -> STATUS_DONE.equals(t.getStatus().toString()))
                .count();

        long inProgress = tasks.stream()
                .filter(t -> STATUS_IN_PROGRESS.equals(t.getStatus().toString()))
                .count();

        long toDo = tasks.stream()
                .filter(t -> STATUS_TO_DO.equals(t.getStatus().toString()))
                .count();

        long delayed = tasks.stream()
                .filter(t -> {
                    Date endDate = t.getEndDate();
                    if (endDate == null) return false;
                    LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return endLocalDate.isBefore(LocalDate.now()) && !STATUS_DONE.equals(t.getStatus().toString());
                })
                .count();

        long highPriorityTasks = tasks.stream()
                .filter(t -> "HIGH".equals(t.getPriority().toString()) && !STATUS_DONE.equals(t.getStatus().toString()))
                .count();

        long highPriorityDelayed = tasks.stream()
                .filter(t -> "HIGH".equals(t.getPriority().toString()))
                .filter(t -> {
                    Date endDate = t.getEndDate();
                    if (endDate == null) return false;
                    LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return endLocalDate.isBefore(LocalDate.now()) && !STATUS_DONE.equals(t.getStatus().toString());
                })
                .count();

        List<String> toDoTaskNames = tasks.stream()
                .filter(t -> STATUS_TO_DO.equals(t.getStatus().toString()))
                .map(t -> "'" + t.getTitle() + "'" +
                        ("HIGH".equals(t.getPriority().toString()) ? " (ALTA PRIORIDAD)" : ""))
                .collect(Collectors.toList());

        List<String> inProgressTaskNames = tasks.stream()
                .filter(t -> STATUS_IN_PROGRESS.equals(t.getStatus().toString()))
                .map(t -> "'" + t.getTitle() + "'" +
                        ("HIGH".equals(t.getPriority().toString()) ? " (ALTA PRIORIDAD)" : ""))
                .collect(Collectors.toList());

        List<String> delayedTaskNames = tasks.stream()
                .filter(t -> {
                    Date endDate = t.getEndDate();
                    if (endDate == null) return false;
                    LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return endLocalDate.isBefore(LocalDate.now()) && !STATUS_DONE.equals(t.getStatus().toString());
                })
                .map(t -> {
                    long days = ChronoUnit.DAYS.between(
                            t.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                            LocalDate.now()
                    );
                    return "'" + t.getTitle() + "' (atrasada " + days + " dias)" +
                            ("HIGH".equals(t.getPriority().toString()) ? " - ALTA PRIORIDAD" : "");
                })
                .collect(Collectors.toList());

        String toDoText = toDoTaskNames.isEmpty() ? "Ninguna" :
                String.join(", ", toDoTaskNames);

        String inProgressText = inProgressTaskNames.isEmpty() ? "Ninguna" :
                String.join(", ", inProgressTaskNames);

        String delayedText = delayedTaskNames.isEmpty() ? "Ninguna" :
                String.join(", ", delayedTaskNames);

        Map<User, List<String>> userTaskMap = new HashMap<>();
        Map<User, TaskStats> userStats = new HashMap<>();

        for (Task task : tasks) {
            if (task.getAssignedUsers() != null && !task.getAssignedUsers().isEmpty()) {
                for (User user : task.getAssignedUsers()) {
                    userTaskMap.computeIfAbsent(user, k -> new ArrayList<>())
                            .add("'" + task.getTitle() + "'" +
                                    (STATUS_DONE.equals(task.getStatus().toString()) ? " (COMPLETADA)" :
                                            STATUS_IN_PROGRESS.equals(task.getStatus().toString()) ? " (EN PROGRESO)" :
                                            " (PENDIENTE)") +
                                    ("HIGH".equals(task.getPriority().toString()) ? " [ALTA]" : ""));

                    TaskStats stats = userStats.getOrDefault(user, new TaskStats());
                    stats.totalTasks++;
                    if (STATUS_DONE.equals(task.getStatus().toString())) {
                        stats.completedTasks++;
                    }
                    if (STATUS_IN_PROGRESS.equals(task.getStatus().toString())) {
                        stats.inProgressTasks++;
                    }
                    if (STATUS_TO_DO.equals(task.getStatus().toString())) {
                        stats.toDoTasks++;
                    }
                    if (!STATUS_DONE.equals(task.getStatus().toString())) {
                        Date endDate = task.getEndDate();
                        if (endDate != null && endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                                .isBefore(LocalDate.now())) {
                            stats.delayedTasks++;
                        }
                    }
                    userStats.put(user, stats);
                }
            }
        }

        List<String> unassignedTasks = tasks.stream()
                .filter(t -> t.getAssignedUsers() == null || t.getAssignedUsers().isEmpty())
                .map(t -> "'" + t.getTitle() + "'" +
                        ("HIGH".equals(t.getPriority().toString()) ? " [ALTA]" : ""))
                .collect(Collectors.toList());

        StringBuilder assignmentsText = new StringBuilder();
        for (Map.Entry<User, List<String>> entry : userTaskMap.entrySet()) {
            User user = entry.getKey();
            TaskStats stats = userStats.get(user);
            assignmentsText.append(String.format("  - %s %s: %d tareas [%d TO_DO, %d IN_PROGRESS, %d DONE, %d atrasadas] -> %s\n",
                    user.getName(), user.getLastName(),
                    stats.totalTasks,
                    stats.toDoTasks,
                    stats.inProgressTasks,
                    stats.completedTasks,
                    stats.delayedTasks,
                    String.join(", ", entry.getValue())
            ));
        }

        String assignments = assignmentsText.length() > 0 ? assignmentsText.toString() : "Ninguna tarea asignada";

        List<String> overloadedUsers = new ArrayList<>();
        List<String> underloadedUsers = new ArrayList<>();
        List<String> highPerformingUsers = new ArrayList<>();
        List<String> lowPerformingUsers = new ArrayList<>();

        for (Map.Entry<User, TaskStats> entry : userStats.entrySet()) {
            User user = entry.getKey();
            TaskStats stats = entry.getValue();
            String userName = user.getName() + " " + user.getLastName();

            if (stats.totalTasks > 5) {
                overloadedUsers.add(userName + " (" + stats.totalTasks + " tareas)");
            } else if (stats.totalTasks <= 2) {
                underloadedUsers.add(userName + " (" + stats.totalTasks + " tareas)");
            }

            if (stats.completedTasks > 0 && stats.delayedTasks == 0) {
                highPerformingUsers.add(userName + " (" + stats.completedTasks + "/" + stats.totalTasks + " completadas)");
            } else if (stats.delayedTasks > 0) {
                lowPerformingUsers.add(userName + " (" + stats.delayedTasks + " tareas atrasadas)");
            }
        }

        double efficiency = calculateEfficiency(tasks, completed);
        double delayRisk = calculateDelayRisk(tasks);
        RiskLevel riskLevel = RiskLevel.fromDelayRisk(delayRisk);
        double completionRate = total > 0 ? (double) completed / total * 100 : 0;

        String prompt = String.format("""
        Eres un asistente de gestión de proyectos.
        Genera UNA recomendación profesional para el LÍDER del proyecto (máximo 120 caracteres):
        
        === PROYECTO ===
        Nombre: %s
        Eficiencia: %.0f%%
        Riesgo de retraso: %.0f%% (%s)
        
        === TAREAS ===
        Total: %d
        Completadas (DONE): %d (%.0f%%)
        En progreso (IN_PROGRESS): %d
        Por hacer (TO_DO): %d
        Atrasadas: %d
        Alta prioridad pendientes: %d (de las cuales %d atrasadas)
        
        === TAREAS DESTACADAS ===
        TO_DO: %s
        IN_PROGRESS: %s
        ATRASADAS: %s
        
        === ASIGNACIONES ACTUALES ===
        %s
        
        === TAREAS SIN ASIGNAR ===
        %s
        
        === EQUIPO ===
        Usuarios sobrecargados (>5 tareas): %s
        Usuarios con baja carga (<=2 tareas): %s
        Alto rendimiento (0 atrasadas): %s
        Bajo rendimiento (con atrasos): %s
        
        === INSTRUCCIONES ===
        Genera UNA recomendación profesional para el LÍDER que:
        1. Sea accionable y específica
        2. Mencione nombres de tareas y miembros del equipo
        3. No menciones al líder directamente (no uses "tú", "debes", etc.)
        4. Sea como un consejo o sugerencia para el líder
        5. MÁXIMO 120 CARACTERES
        6. Responde SOLO con la recomendación
        """,
                project.getName(),
                efficiency,
                delayRisk,
                riskLevel.getDescription(),
                total,
                completed, completionRate,
                inProgress,
                toDo,
                delayed,
                highPriorityTasks,
                highPriorityDelayed,
                toDoText,
                inProgressText,
                delayedText,
                assignments,
                unassignedTasks.isEmpty() ? "Ninguna" : String.join(", ", unassignedTasks),
                overloadedUsers.isEmpty() ? "Ninguno" : String.join(", ", overloadedUsers),
                underloadedUsers.isEmpty() ? "Ninguno" : String.join(", ", underloadedUsers),
                highPerformingUsers.isEmpty() ? "Ninguno" : String.join(", ", highPerformingUsers),
                lowPerformingUsers.isEmpty() ? "Ninguno" : String.join(", ", lowPerformingUsers)
        );

        log.info("📝 Prompt para líder: {}", prompt);

        String response = geminiClient.generateContent(prompt);
        String recommendation = cleanResponse(response);

        if (recommendation == null || recommendation.isEmpty() || recommendation.length() < 5) {
            String retryPrompt = String.format("""
                Genera UNA recomendación corta para el proyecto '%s' con %d tareas (%d completadas, %d pendientes).
                Responde SOLO con la recomendación.
                """, project.getName(), total, completed, total - completed);

            String retryResponse = geminiClient.generateContent(retryPrompt);
            recommendation = cleanResponse(retryResponse);

            if (recommendation == null || recommendation.isEmpty() || recommendation.length() < 5) {
                recommendation = String.format("Proyecto '%s': %d/%d tareas completadas. %d pendientes.",
                        project.getName(), completed, total, total - completed);
            }
        }

        return List.of(new AIRecommendation(recommendation, "ACTION", 1));
    }

    private static class TaskStats {
        int totalTasks = 0;
        int completedTasks = 0;
        int inProgressTasks = 0;
        int toDoTasks = 0;
        int delayedTasks = 0;
    }

    @Override
    public MemberWeeklySummary handle(GetMemberDashboardQuery query) {
        try {
            User member = userRepository.findById(query.memberId())
                    .orElseThrow(() -> new RuntimeException("Miembro no encontrado"));

            List<Task> memberTasks = getAllTasksForMember(member);

            if (memberTasks.isEmpty()) {
                String prompt = String.format("""
                        El miembro %s %s no tiene tareas asignadas actualmente.
                        Genera un resumen corto (maximo 80 caracteres) recomendando al miembro solicitar tareas.
                        Responde SOLO con el resumen.
                        """, member.getName(), member.getLastName());

                String response = geminiClient.generateContent(prompt);
                String summary = (response != null && !response.isEmpty() && !response.contains("Error")) ?
                        cleanResponse(response) :
                        "No tienes tareas asignadas. Solicita a tu líder que te asigne tareas.";

                return MemberWeeklySummary.builder()
                        .userId(member.getId())
                        .userName(member.getName() + " " + member.getLastName())
                        .smartVelocity(new SmartVelocity(0.0, 0, 0))
                        .summaryText(summary)
                        .riskPrediction("Sin datos para predecir riesgos")
                        .performanceSuggestions(List.of(
                                "Solicita a tu líder que te asigne tareas en el proyecto.",
                                "Pregunta por tareas disponibles para colaborar."
                        ))
                        .generatedAt(LocalDateTime.now())
                        .build();
            }

            return generateMemberSummaryWithAI(member, memberTasks);

        } catch (Exception e) {
            log.error("Error generando dashboard para miembro", e);
            String prompt = "Hubo un error al generar el dashboard. Genera un mensaje corto (maximo 50 caracteres) recomendando al usuario intentar nuevamente. Responde SOLO con el mensaje.";
            String response = geminiClient.generateContent(prompt);
            String summary = (response != null && !response.isEmpty() && !response.contains("Error")) ?
                    cleanResponse(response) :
                    "Error al generar el dashboard. Intenta nuevamente.";

            return MemberWeeklySummary.builder()
                    .userId(query.memberId())
                    .userName("Usuario")
                    .smartVelocity(new SmartVelocity(0.0, 0, 0))
                    .summaryText(summary)
                    .riskPrediction("Error al predecir el riesgo")
                    .performanceSuggestions(List.of("Intenta nuevamente mas tarde.", "Verifica que tengas tareas asignadas."))
                    .generatedAt(LocalDateTime.now())
                    .build();
        }
    }

    private MemberWeeklySummary generateMemberSummaryWithAI(User member, List<Task> memberTasks) {
        int totalTasks = memberTasks.size();
        int completedTasks = (int) memberTasks.stream()
                .filter(task -> STATUS_DONE.equals(task.getStatus().toString()))
                .count();
        int inProgressTasks = (int) memberTasks.stream()
                .filter(task -> STATUS_IN_PROGRESS.equals(task.getStatus().toString()))
                .count();
        int toDoTasks = (int) memberTasks.stream()
                .filter(task -> STATUS_TO_DO.equals(task.getStatus().toString()))
                .count();
        int delayedTasks = (int) memberTasks.stream()
                .filter(task -> {
                    Date endDate = task.getEndDate();
                    if (endDate == null) return false;
                    LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return endLocalDate.isBefore(LocalDate.now()) && !STATUS_DONE.equals(task.getStatus().toString());
                })
                .count();

        SmartVelocity smartVelocity = SmartVelocity.calculate(totalTasks, completedTasks);
        Project highestRiskProject = findHighestRiskProjectForMember(memberTasks);

        String riskPrediction = generateMemberRiskPrediction(member, memberTasks, highestRiskProject);
        List<String> performanceSuggestions = generateMemberSuggestions(member, memberTasks);
        String summaryText = generateMemberWeeklySummary(member, smartVelocity, highestRiskProject, memberTasks, delayedTasks);

        return MemberWeeklySummary.builder()
                .userId(member.getId())
                .userName(member.getName() + " " + member.getLastName())
                .smartVelocity(smartVelocity)
                .summaryText(summaryText)
                .riskPrediction(riskPrediction)
                .performanceSuggestions(performanceSuggestions)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private String generateMemberRiskPrediction(User member, List<Task> tasks, Project highestRiskProject) {
        if (highestRiskProject == null) {
            String prompt = String.format("""
                    El miembro %s %s no tiene proyectos asignados para analisis de riesgo.
                    Genera un mensaje corto (maximo 30 caracteres) recomendando asignarse a un proyecto.
                    Responde SOLO con el mensaje.
                    """, member.getName(), member.getLastName());

            String response = geminiClient.generateContent(prompt);
            return (response != null && !response.isEmpty() && !response.contains("Error")) ?
                    cleanResponse(response) :
                    "Sin proyectos asignados. Solicita unirte a un proyecto.";
        }

        double projectRisk = calculateDelayRisk(tasks);
        long delayedTasks = tasks.stream()
                .filter(task -> {
                    Date endDate = task.getEndDate();
                    if (endDate == null) return false;
                    LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return endLocalDate.isBefore(LocalDate.now()) && !STATUS_DONE.equals(task.getStatus().toString());
                })
                .count();

        long toDoTasks = tasks.stream()
                .filter(task -> STATUS_TO_DO.equals(task.getStatus().toString()))
                .count();

        Optional<Task> mostDelayed = tasks.stream()
                .filter(task -> {
                    Date endDate = task.getEndDate();
                    if (endDate == null) return false;
                    LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return endLocalDate.isBefore(LocalDate.now()) && !STATUS_DONE.equals(task.getStatus().toString());
                })
                .min((a, b) -> {
                    Date endA = a.getEndDate();
                    Date endB = b.getEndDate();
                    if (endA == null) return 1;
                    if (endB == null) return -1;
                    return endA.compareTo(endB);
                });

        String prompt = String.format("""
                Analiza el riesgo del miembro %s %s:
                Proyecto: %s
                Tareas: %d TO_DO, %d IN_PROGRESS, %d DONE, %d atrasadas
                Riesgo: %.0f%%
                Tarea mas retrasada: %s
                
                Genera UNA frase corta (maximo 25 palabras) sobre el riesgo de retraso.
                Responde SOLO con la frase.
                """,
                member.getName(), member.getLastName(),
                highestRiskProject.getName(),
                toDoTasks,
                tasks.stream().filter(t -> STATUS_IN_PROGRESS.equals(t.getStatus().toString())).count(),
                tasks.stream().filter(t -> STATUS_DONE.equals(t.getStatus().toString())).count(),
                delayedTasks,
                projectRisk,
                mostDelayed.isPresent() ? "'" + mostDelayed.get().getTitle() + "'" : "ninguna"
        );

        String response = geminiClient.generateContent(prompt);
        String prediction = cleanResponse(response);

        if (prediction == null || prediction.isEmpty() || prediction.length() < 10) {
            String retryPrompt = String.format("""
                    Genera una frase corta sobre el riesgo de retraso para el miembro %s en el proyecto %s.
                    Responde SOLO con la frase.
                    """, member.getName(), highestRiskProject.getName());
            String retryResponse = geminiClient.generateContent(retryPrompt);
            prediction = cleanResponse(retryResponse);

            if (prediction == null || prediction.isEmpty() || prediction.length() < 10) {
                prediction = String.format("Riesgo de retraso en %s: %.0f%%", highestRiskProject.getName(), projectRisk);
            }
        }

        return prediction;
    }

    private List<String> generateMemberSuggestions(User member, List<Task> tasks) {
        long completed = tasks.stream().filter(t -> STATUS_DONE.equals(t.getStatus().toString())).count();
        long total = tasks.size();
        long toDo = tasks.stream().filter(t -> STATUS_TO_DO.equals(t.getStatus().toString())).count();
        long inProgress = tasks.stream().filter(t -> STATUS_IN_PROGRESS.equals(t.getStatus().toString())).count();

        Optional<Task> highPriorityTask = tasks.stream()
                .filter(t -> "HIGH".equals(t.getPriority().toString()) && !STATUS_DONE.equals(t.getStatus().toString()))
                .findFirst();

        Optional<Task> delayedTask = tasks.stream()
                .filter(t -> {
                    Date endDate = t.getEndDate();
                    if (endDate == null) return false;
                    LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return endLocalDate.isBefore(LocalDate.now()) && !STATUS_DONE.equals(t.getStatus().toString());
                })
                .findFirst();

        List<Task> pendingTasks = tasks.stream()
                .filter(t -> !STATUS_DONE.equals(t.getStatus().toString()))
                .sorted((a, b) -> {
                    int priorityA = "HIGH".equals(a.getPriority().toString()) ? 0 :
                            "MEDIUM".equals(a.getPriority().toString()) ? 1 : 2;
                    int priorityB = "HIGH".equals(b.getPriority().toString()) ? 0 :
                            "MEDIUM".equals(b.getPriority().toString()) ? 1 : 2;
                    return Integer.compare(priorityA, priorityB);
                })
                .collect(Collectors.toList());

        String taskContext;
        if (completed == total && total > 0) {
            taskContext = "todas las tareas completadas";
        } else if (delayedTask.isPresent()) {
            taskContext = "tarea ATRASADA: '" + delayedTask.get().getTitle() + "'";
        } else if (highPriorityTask.isPresent()) {
            taskContext = "tarea de ALTA prioridad: '" + highPriorityTask.get().getTitle() + "'";
        } else if (toDo > 0) {
            String pendingList = pendingTasks.stream()
                    .limit(2)
                    .map(t -> "'" + t.getTitle() + "'")
                    .collect(Collectors.joining(", "));
            taskContext = "tareas pendientes (TO_DO): " + pendingList;
        } else if (inProgress > 0) {
            taskContext = "tareas en progreso: " + inProgress;
        } else {
            taskContext = "sin tareas activas";
        }

        String prompt = String.format("""
                Miembro: %s %s
                Tareas: %d TO_DO, %d IN_PROGRESS, %d DONE
                Contexto: %s
                
                Genera 2 sugerencias de productividad cortas (maximo 15 palabras cada una).
                Formato: viñetas (•) en lineas separadas.
                Responde SOLO con las viñetas.
                """,
                member.getName(), member.getLastName(),
                toDo, inProgress, completed,
                taskContext
        );

        String response = geminiClient.generateContent(prompt);
        List<String> suggestions = parseMemberSuggestions(response);

        if (suggestions == null || suggestions.size() < 2) {
            String retryPrompt = String.format("""
                    Genera 2 sugerencias de productividad para %s %s.
                    Formato: viñetas (•) en lineas separadas.
                    """, member.getName(), member.getLastName());
            String retryResponse = geminiClient.generateContent(retryPrompt);
            suggestions = parseMemberSuggestions(retryResponse);

            if (suggestions == null || suggestions.size() < 2) {
                suggestions = List.of(
                        "Organiza tus tareas pendientes por prioridad.",
                        "Dedica 2 horas sin interrupciones a tus tareas."
                );
            }
        }

        return suggestions.stream().limit(2).collect(Collectors.toList());
    }

    private List<String> parseMemberSuggestions(String response) {
        if (response == null || response.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> result = Arrays.stream(response.split("\n"))
                .map(String::trim)
                .filter(line -> line.matches("^[•\\-\\*\\d+\\).].*"))
                .map(line -> line.replaceAll("^[•\\-\\*\\d+\\).]\\s*", ""))
                .filter(line -> !line.isEmpty() && line.length() > 5)
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            result = Arrays.stream(response.split("\n"))
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && line.length() > 5)
                    .limit(2)
                    .collect(Collectors.toList());
        }

        return result;
    }

    private String generateMemberWeeklySummary(User member, SmartVelocity smartVelocity,
                                               Project highestRiskProject, List<Task> tasks,
                                               long delayedTasks) {
        long completed = tasks.stream().filter(t -> STATUS_DONE.equals(t.getStatus().toString())).count();
        long total = tasks.size();
        long toDo = tasks.stream().filter(t -> STATUS_TO_DO.equals(t.getStatus().toString())).count();
        String projectName = highestRiskProject != null ? highestRiskProject.getName() : "sin proyecto";

        String prompt = String.format("""
                Resumen semanal para %s %s:
                Smart Velocity: %.1f/5.0
                Tareas: %d TO_DO, %d IN_PROGRESS, %d DONE
                Tareas atrasadas: %d
                Proyecto principal: %s
                
                Genera un resumen corto (maximo 20 palabras) con el estado actual.
                Responde SOLO con el resumen.
                """,
                member.getName(), member.getLastName(),
                smartVelocity.value(),
                toDo,
                tasks.stream().filter(t -> STATUS_IN_PROGRESS.equals(t.getStatus().toString())).count(),
                completed,
                delayedTasks,
                projectName
        );

        String response = geminiClient.generateContent(prompt);
        String summary = cleanResponse(response);

        if (summary == null || summary.isEmpty() || summary.length() < 10) {
            String retryPrompt = String.format("""
                    Genera un resumen corto para %s %s con Smart Velocity %.1f/5.0 y %d tareas completadas de %d.
                    """, member.getName(), member.getLastName(), smartVelocity.value(), completed, total);
            String retryResponse = geminiClient.generateContent(retryPrompt);
            summary = cleanResponse(retryResponse);

            if (summary == null || summary.isEmpty() || summary.length() < 10) {
                summary = String.format("%s: %.1f/5.0 SV, %d/%d tareas completadas.",
                        member.getName(), smartVelocity.value(), completed, total);
            }
        }

        return summary;
    }

    private List<Task> getAllTasksForMember(User member) {
        List<Task> allTasks = new ArrayList<>();
        for (Project project : member.getMemberInProjects()) {
            allTasks.addAll(taskRepository.findByProjectId(project.getId()));
        }
        return allTasks.stream()
                .filter(task -> task.getAssignedUsers() != null && task.getAssignedUsers().contains(member))
                .collect(Collectors.toList());
    }

    private Project findHighestRiskProjectForMember(List<Task> memberTasks) {
        Map<Long, List<Task>> tasksByProject = memberTasks.stream()
                .filter(task -> task.getProject() != null)
                .collect(Collectors.groupingBy(task -> task.getProject().getId()));

        if (tasksByProject.isEmpty()) {
            return null;
        }

        Map<Long, Double> projectRisk = new HashMap<>();
        for (Map.Entry<Long, List<Task>> entry : tasksByProject.entrySet()) {
            projectRisk.put(entry.getKey(), calculateDelayRisk(entry.getValue()));
        }

        Long highestRiskProjectId = projectRisk.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        return highestRiskProjectId != null ? projectRepository.findById(highestRiskProjectId).orElse(null) : null;
    }

    private String cleanResponse(String response) {
        if (response == null) return "";
        return response.trim()
                .replaceAll("^[•\\-\\*\\d+\\).]\\s*", "")
                .replaceAll("\n.*", "")
                .replaceAll("^\"|\"$", "")
                .replaceAll("RECOMENDACION:", "")
                .replaceAll("RECOMENDACIÓN:", "")
                .replaceAll("RESUMEN:", "")
                .replaceAll("SUGERENCIAS:", "")
                .trim();
    }

    private double calculateDelayRisk(List<Task> tasks) {
        if (tasks.isEmpty()) return 0.0;

        LocalDate now = LocalDate.now();

        long delayedTasks = tasks.stream()
                .filter(task -> {
                    Date endDate = task.getEndDate();
                    if (endDate == null) return false;
                    LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return endLocalDate.isBefore(now) && !STATUS_DONE.equals(task.getStatus().toString());
                })
                .count();

        long urgentTasks = tasks.stream()
                .filter(task -> {
                    Date endDate = task.getEndDate();
                    if (endDate == null) return false;
                    LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return !STATUS_DONE.equals(task.getStatus().toString()) &&
                            ChronoUnit.DAYS.between(now, endLocalDate) <= 3 &&
                            ChronoUnit.DAYS.between(now, endLocalDate) >= 0;
                })
                .count();

        long unassignedTasks = tasks.stream()
                .filter(task -> task.getAssignedUsers() == null || task.getAssignedUsers().isEmpty())
                .count();

        return Math.min(100, ((double) delayedTasks / tasks.size() * 100) +
                ((double) urgentTasks / tasks.size() * 30) +
                ((double) unassignedTasks / tasks.size() * 20));
    }

    private double calculateEfficiency(List<Task> tasks, long completedTasks) {
        if (tasks.isEmpty()) return 100.0;

        double completionRate = (double) completedTasks / tasks.size() * 100;

        long onTimeTasks = tasks.stream()
                .filter(task -> STATUS_DONE.equals(task.getStatus().toString()))
                .filter(task -> {
                    Date endDate = task.getEndDate();
                    if (endDate == null) return true;
                    LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return !endLocalDate.isBefore(LocalDate.now());
                })
                .count();

        long activeDelayed = tasks.stream()
                .filter(task -> {
                    Date endDate = task.getEndDate();
                    if (endDate == null) return false;
                    LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return endLocalDate.isBefore(LocalDate.now()) && !STATUS_DONE.equals(task.getStatus().toString());
                })
                .count();

        double onTimeBonus = (double) onTimeTasks / Math.max(1, completedTasks) * 20;
        double delayPenalty = (double) activeDelayed / tasks.size() * 30;

        return Math.max(0, Math.min(100, completionRate + onTimeBonus - delayPenalty));
    }
}
