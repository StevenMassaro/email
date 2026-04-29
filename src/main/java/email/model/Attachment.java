package email.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import email.service.MessageService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.activation.DataSource;
import java.io.IOException;
import java.util.UUID;

@Getter
@Setter
public class Attachment {

    private long id;
    private long messageId;
    private long seqNum;
    private String name;
    private String contentType;
    @JsonIgnore
    private byte[] file;
    private String contentId;
    @JsonIgnore
    private long messageUid;
    @JsonIgnore
    private UUID accountBitwardenId;
    @JsonIgnore
    private boolean loaded;

    public Attachment(String name, String contentType, byte[] file, String contentId) {
        this.id = MessageService.attachmentIdSequence.incrementAndGet();
        this.name = name;
        this.contentType = contentType;
        this.file = file;
        this.contentId = contentId;
        this.loaded = true;
    }

    private Attachment(String name, String contentType, long messageUid, UUID accountBitwardenId) {
        this.id = MessageService.attachmentIdSequence.incrementAndGet();
        this.name = name;
        this.contentType = contentType;
        this.messageUid = messageUid;
        this.accountBitwardenId = accountBitwardenId;
        this.loaded = false;
    }

    /**
     * Load attachment bytes from a DataSource. Used for inline (CID) attachments
     * that must be available for body rendering.
     */
    public void loadFromDataSource(DataSource dataSource) throws IOException {
        this.file = IOUtils.toByteArray(dataSource.getInputStream());
        this.loaded = true;
    }

    public ResponseEntity<Resource> toResponseEntity() {
        Resource resource = new ByteArrayResource(getFile());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + getName() + "\"")
                .contentType(MediaType.parseMediaType(getContentType()))
                .body(resource);
    }

    /**
     * Create a lazy-loading attachment from a DataSource. Only metadata is stored;
     * the actual file bytes are downloaded on-demand via {@link email.service.ImapService#downloadAttachment}.
     */
    public static Attachment fromDataSource(DataSource dataSource, long messageUid, UUID accountBitwardenId) throws IOException {
        String name = dataSource.getName();
        String contentType = dataSource.getContentType();
        if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(contentType)) {
            return new Attachment(name, contentType, messageUid, accountBitwardenId);
        }
        return null;
    }
}
