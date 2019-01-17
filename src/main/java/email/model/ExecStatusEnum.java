package email.model;

public enum ExecStatusEnum {
    RULE_END_SUCCESS(0, "Success", "Successfully finished running email sync processor."),
    RULE_END_MESSAGE_FAILURE(1, "Failure", "Finished running email sync processor, failed to insert message(s)."),
    RULE_END_ACCOUNT_FAILURE(2, "Critical failure", "Finished running email sync processor, failed entire account(s)."),
    RULE_START(10, "Rule processing start", "Start running email sync processor.");

    private long id;
    private String identifier;
    private String message;

    ExecStatusEnum(long id, String identifier, String message) {
        this.id = id;
        this.identifier = identifier;
        this.message = message;
    }

    public long getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getMessage() {
        return message;
    }
}
