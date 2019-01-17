package email.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExecutionLogMapper {

    void insert(@Param("execStatusEnum") long execStatusEnum);
}
