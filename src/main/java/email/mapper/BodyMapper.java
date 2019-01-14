package email.mapper;

import email.model.Body;
import email.model.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BodyMapper {

    @Select("INSERT INTO email.body (body) VALUES(#{body}) RETURNING id")
    long insert(@Param("body") String body);

    @Select("SELECT body FROM email.body WHERE id = #{bodyId}")
    String get(@Param("bodyId") long bodyId);

    long delete(@Param("messages") List<Message> bodies);
}
