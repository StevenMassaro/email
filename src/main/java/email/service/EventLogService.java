package email.service;

import email.mapper.EventLogMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventLogService {

    @Autowired
    private EventLogMapper eventLogMapper;

    public void insert(long accountId, long statusEnum, long messageUid, String message) {
        eventLogMapper.insert(accountId, statusEnum, messageUid, message);
    }
}
