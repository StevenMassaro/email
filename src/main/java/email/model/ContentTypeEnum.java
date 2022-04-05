package email.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ContentTypeEnum {

    /**
     * These enums are in order of preference.
     */
    TEXT_HTML("text/html"),
    MULTIPART_ALTERNATIVE("multipart/alternative", "text/plain"),
    ALTERNATIVE("alternative"),
    TEXT_PLAIN("text/plain");

    private final String imapContentType;
    private final String returnContentType;

    ContentTypeEnum(String imapContentType) {
        this(imapContentType, imapContentType);
    }
    ContentTypeEnum(String imapContentType, String returnContentType) {
        this.imapContentType = imapContentType;
        this.returnContentType = returnContentType;
    }

    public String getImapContentType() {
        return imapContentType;
    }

    /**
     * The content type that should be used when returning the body content.
     */
    public String getReturnContentType() {
        return returnContentType;
    }

    public static List<ContentTypeEnum> getNonMultipartContentTypeEnums() {
        return Arrays.stream(ContentTypeEnum.values()).filter(cte -> !cte.getImapContentType().contains("multipart")).collect(Collectors.toList());
    }
}
