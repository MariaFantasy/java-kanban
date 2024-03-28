package task;

public enum TaskType {
    EPIC,
    COMMON,
    SUBTASK;

    @Override
    public String toString(){
        return switch (this) {
            case EPIC -> "Epic";
            case COMMON -> "Common";
            case SUBTASK -> "Subtask";
            default -> "Unknown";
        };
    }
}
