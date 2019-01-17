package email.service;

import email.mapper.ExecutionLogMapper;
import email.model.ExecStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExecutionLogService {

    @Autowired
    private ExecutionLogMapper executionLogMapper;

    public void insert(ExecStatusEnum execStatusEnum) {
        executionLogMapper.insert(execStatusEnum.getId());
    }
}
