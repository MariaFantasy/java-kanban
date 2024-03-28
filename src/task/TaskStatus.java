package task;

public enum TaskStatus {
    NEW,
    IN_PROGRESS,
    DONE;

    @Override
    public String toString(){
        return switch (this) {
            case NEW -> "New";
            case IN_PROGRESS -> "In Progress";
            case DONE -> "Done";
            default -> "Unknown";
        };
    }
}
