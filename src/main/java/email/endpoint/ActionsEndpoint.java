package email.endpoint;

import email.model.Account;
import email.model.SyncStatusResult;
import email.service.AccountService;
import email.service.SyncService;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/actions")
@Log4j2
public class ActionsEndpoint {

    private final SyncService syncService;
    private final AccountService accountService;

    public ActionsEndpoint(SyncService syncService, AccountService accountService) {
        this.syncService = syncService;
        this.accountService = accountService;
    }

    @PostMapping("/sync")
    public synchronized List<SyncStatusResult> performSync(@RequestBody String bitwardenMasterPassword) throws ExecutionException, InterruptedException {
        List<Account> accounts = accountService.list();

        List<Future<SyncStatusResult>> syncFutures = new ArrayList<>();
        for (Account account : accounts) {
            log.debug("Submitting task for account {}", account.getUsername());
            syncFutures.add(syncService.sync(account, bitwardenMasterPassword));
        }

        List<SyncStatusResult> results = new ArrayList<>();
        for (Future<SyncStatusResult> future : syncFutures) {
            results.add(future.get());
        }

        return results;
    }

}
