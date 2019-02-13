package email.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BodyPartMapper {

    void insert(@Param("messageId") long messageId, @Param("seqNum") long seqNum,
                @Param("contentType") String contentType, @Param("content") byte[] content);

    void delete(@Param("messageId") long messageId);
}
