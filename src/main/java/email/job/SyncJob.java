package email.job;

import email.model.SyncStatusResult;
import email.service.AccountService;
import email.service.SyncService;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
public class SyncJob {

    private final AccountService accountService;
    private final SyncService syncService;
    private final List<SyncStatusResult> results = new ArrayList<>();
    private final AtomicBoolean inProgress = new AtomicBoolean(false);
    private int numberOfAccounts;

    public SyncJob(AccountService accountService, SyncService syncService) {
        this.accountService = accountService;
        this.syncService = syncService;
    }

    public synchronized void startSync(String bitwardenMasterPassword) throws Exception {
        if (inProgress.getAndSet(true)) {
            throw new Exception("Sync is already in progress and cannot be started again.");
        }
        results.clear();
        new Thread(() -> {
            List<UUID> accounts = accountService.list();
            numberOfAccounts = accounts.size();

            List<Future<SyncStatusResult>> syncFutures = new ArrayList<>();
            for (UUID accountBitwardenId : accounts) {
                log.debug("Submitting task for account {}", accountBitwardenId);
                syncFutures.add(syncService.sync(accountBitwardenId, bitwardenMasterPassword));
            }

            while(!syncFutures.isEmpty()) {
                Iterator<Future<SyncStatusResult>> iterator = syncFutures.iterator();
                while(iterator.hasNext()) {
                    Future<SyncStatusResult> future = iterator.next();
                    try {
                        if (future.isDone()) {
                            results.add(future.get());
                            iterator.remove();
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Sync thread failed for one future", e);
                    }
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

    public int getNumberOfAccounts() {
        return numberOfAccounts;
    }
}
