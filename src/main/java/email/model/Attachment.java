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

    public Attachment(String name, String contentType, byte[] file) {
        this.id = MessageService.attachmentIdSequence.incrementAndGet();
        this.name = name;
        this.contentType = contentType;
        this.file = file;
    }

    public ResponseEntity<Resource> toResponseEntity() {
        Resource file = new ByteArrayResource(getFile());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + getName() + "\"")
                .contentType(MediaType.parseMediaType(getContentType()))
                .body(file);
    }

    /**
     * Given a {@link DataSource}, create an {@link Attachment} object from it.
     * @return the attachment, or null if the DataSource is missing required parts (name, contentType, data).
     */
    public static Attachment fromDataSource(DataSource dataSource) throws IOException {
        String name = dataSource.getName();
        String contentType = dataSource.getContentType();
        byte[] file = IOUtils.toByteArray(dataSource.getInputStream());
        if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(contentType) && file != null) {
            return new Attachment(dataSource.getName(), dataSource.getContentType(), IOUtils.toByteArray(dataSource.getInputStream()));
        }
        return null;
    }
}
