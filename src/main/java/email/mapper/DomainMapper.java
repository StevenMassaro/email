package email.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DomainMapper extends BaseMapper {
    String table = "domain";
    String schema = DATABASE + "." + table;

    @Select("INSERT INTO email.\"domain\" (hostname, port, authentication) VALUES" +
            "(#{hostname}, #{port}, #{authentication}) RETURNING id")
    long insert(@Param("hostname") String hostname, @Param("port") long port,
                @Param("authentication") String authentication);
}
