package pe.edu.upc.taskmaster.backend.ai.domain.model.valueobjects;

public record SmartVelocity(double value, int totalTasks, int completedTasks) {
    public SmartVelocity {
        if (value < 0 || value > 5) {
            throw new IllegalArgumentException("SmartVelocity must be between 0 and 5");
        }
    }

    public static SmartVelocity calculate(int totalTasks, int completedTasks) {
        if (totalTasks == 0) return new SmartVelocity(0.0, 0, 0);
        double ratio = (double) completedTasks / totalTasks;
        double velocity = ratio * 5;
        return new SmartVelocity(Math.round(velocity * 10.0) / 10.0, totalTasks, completedTasks);
    }
}
