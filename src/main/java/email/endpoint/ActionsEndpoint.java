package email.endpoint;

import email.model.Account;
import email.model.ExecStatusEnum;
import email.model.SyncStatusResult;
import email.service.AccountService;
import email.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/actions")
public class ActionsEndpoint {

    private final Logger logger = LoggerFactory.getLogger(ActionsEndpoint.class);

    @Autowired
    private SyncService syncService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/sync")
    public List<SyncStatusResult> performSync() throws ExecutionException, InterruptedException {
//        logger.info(ExecStatusEnum.RULE_START.getMessage());
//        executionLogService.insert(ExecStatusEnum.RULE_START);
        List<Account> accounts = accountService.list();

        List<Future<SyncStatusResult>> syncFutures = new ArrayList<>();
        for (Account account : accounts) {
            logger.debug(String.format("Submitting task for account %s", account.getUsername()));
            syncFutures.add(syncService.sync(account));
        }

        List<SyncStatusResult> results = new ArrayList<>();
        for (Future<SyncStatusResult> future : syncFutures) {
            results.add(future.get());
        }

        return results;
    }

}
