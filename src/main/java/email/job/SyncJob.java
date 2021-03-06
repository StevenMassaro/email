package email.job;

import email.model.Account;
import email.model.SyncStatusResult;
import email.service.AccountService;
import email.service.SyncService;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
public class SyncJob {

    private final AccountService accountService;
    private final SyncService syncService;
    private final List<SyncStatusResult> results = new ArrayList<>();
    private final AtomicBoolean inProgress = new AtomicBoolean(false);

    public SyncJob(AccountService accountService, SyncService syncService) {
        this.accountService = accountService;
        this.syncService = syncService;
    }

    public synchronized void startSync(String bitwardenMasterPassword) throws ExecutionException, InterruptedException {
        inProgress.set(true);
        results.clear();
        new Thread(() -> {
            List<Account> accounts = accountService.list();

            List<Future<SyncStatusResult>> syncFutures = new ArrayList<>();
            for (Account account : accounts) {
                log.debug("Submitting task for account {}", account.getUsername());
                syncFutures.add(syncService.sync(account, bitwardenMasterPassword));
            }

            for (Future<SyncStatusResult> future : syncFutures) {
                try {
                    results.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Sync thread failed for one future", e);
                }
            }
            inProgress.set(false);
        }).start();
    }

    public boolean isComplete() {
        return !inProgress.get();
    }

    public List<SyncStatusResult> getResults() {
        return results;
    }
}
