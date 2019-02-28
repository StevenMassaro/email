package email.mapper;

import email.model.Attachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AttachmentMapper {

    Attachment get(@Param("id") long id);

    void insert(@Param("messageId") long messageId,
                @Param("seqNum") long seqNum,
                @Param("name") String name,
                @Param("contentType") String contentType,
                @Param("file") byte[] file);

    void delete(@Param("messageId") long messageId);
}
