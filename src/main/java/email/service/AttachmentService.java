package email.service;

import email.mapper.AttachmentMapper;
import email.model.Attachment;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class AttachmentService {

    @Autowired
    private AttachmentMapper attachmentMapper;

    public Attachment get(long id) {
        return attachmentMapper.get(id);
    }

    public void insert(long messageId, List<Attachment> attachments) {
        for (int i = 0; i < attachments.size(); i++) {
            Attachment attachment = attachments.get(i);
            attachmentMapper.insert(messageId, i, attachment.getName(), attachment.getContentType(), attachment.getFile());
        }
    }

    public void delete(long messageId) {
        attachmentMapper.delete(messageId);
    }
}
