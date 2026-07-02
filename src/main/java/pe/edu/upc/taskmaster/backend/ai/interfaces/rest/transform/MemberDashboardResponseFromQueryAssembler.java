package pe.edu.upc.taskmaster.backend.ai.interfaces.rest.transform;

import org.springframework.stereotype.Component;
import pe.edu.upc.taskmaster.backend.ai.domain.model.aggregates.MemberWeeklySummary;
import pe.edu.upc.taskmaster.backend.ai.interfaces.rest.resources.MemberDashboardResponseResource;
import pe.edu.upc.taskmaster.backend.ai.interfaces.rest.resources.WeeklySummaryResource;

import java.time.LocalDate;
import java.util.List;

@Component
public class MemberDashboardResponseFromQueryAssembler {

    public MemberDashboardResponseResource toResource(MemberWeeklySummary summary) {
        if (summary == null) {
            return new MemberDashboardResponseResource(
                    new WeeklySummaryResource(0.0, "Sin datos disponibles", "Sin datos", LocalDate.now().toString()),
                    List.of("No hay sugerencias disponibles")
            );
        }

        WeeklySummaryResource weeklySummary = new WeeklySummaryResource(
                summary.getSmartVelocity().value(),
                summary.getSummaryText(),
                summary.getRiskPrediction(),
                summary.getGeneratedAt().toString()
        );

        return new MemberDashboardResponseResource(
                weeklySummary,
                summary.getPerformanceSuggestions()
        );
    }
}
