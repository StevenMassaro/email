package email.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EventLogMapper {

    void insert(@Param("accountid") long accountId, @Param("statusenum") long statusEnum,
                @Param("messageuid") long messageUid, @Param("message") String message);
}
