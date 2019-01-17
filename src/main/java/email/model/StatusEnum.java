package email.model;

public enum StatusEnum {
    SUCCESS(0, "Success"),
    FAILURE(1, "Failure"),
    NOTE(2, "Note");

    private long id;
    private String identifier;

    StatusEnum(long id, String identifier) {
        this.id = id;
        this.identifier = identifier;
    }
}
