package email.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BodyMapper {

    @Select("INSERT INTO email.body (body) VALUES(#{body}) RETURNING id")
    long insert(@Param("body") String body);

    @Select("SELECT body FROM email.body WHERE id = #{bodyId}")
    String get(@Param("bodyId") long bodyId);
}
