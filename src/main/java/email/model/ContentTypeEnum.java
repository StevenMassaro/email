package email.model;

public enum ContentTypeEnum {

    /**
     * These enums are in order of preference.
     */
    TEXT_HTML("text/html"),
    ALTERNATIVE("alternative"),
    TEXT_PLAIN("text/plain");

    private String imapContentType;

    ContentTypeEnum(String imapContentType) {
        this.imapContentType = imapContentType;
    }

    public String getImapContentType() {
        return imapContentType;
    }

    public void setImapContentType(String imapContentType) {
        this.imapContentType = imapContentType;
    }
}
