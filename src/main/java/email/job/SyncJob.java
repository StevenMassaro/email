package email.job;

import email.model.ExecStatusEnum;
import email.model.SyncProgress;
import email.model.SyncStatusResult;
import email.model.bitwarden.Item;
import email.service.BitwardenService;
import email.service.SyncService;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
public class SyncJob {

    private final BitwardenService bitwardenService;
    private final SyncService syncService;
    private final List<SyncStatusResult> results = new ArrayList<>();
    private final AtomicBoolean inProgress = new AtomicBoolean(false);
    private SyncProgress syncProgress;
    private int numberOfAccounts;

    public SyncJob(BitwardenService bitwardenService, SyncService syncService) {
        this.bitwardenService = bitwardenService;
        this.syncService = syncService;
    }

    public synchronized void startSync(String bitwardenMasterPassword) throws Exception {
        if (inProgress.getAndSet(true)) {
            throw new Exception("Sync is already in progress and cannot be started again.");
        }
        results.clear();
        syncProgress = new SyncProgress();
        new Thread(() -> {
            List<Item> items;
            try {
                items = bitwardenService.getItems(bitwardenMasterPassword);
            } catch (Exception e) {
                log.error("Failed to retrieve items from Bitwarden", e);
                results.add(new SyncStatusResult(0, 0, 0, ExecStatusEnum.RULE_END_ACCOUNT_FAILURE, "Bitwarden"));
                inProgress.set(false);
                return;
            }
            numberOfAccounts = items.size();

            List<Future<SyncStatusResult>> syncFutures = new ArrayList<>();
            for (Item item : items) {
                log.debug("{} - Submitting sync task", item.getId());
                syncFutures.add(syncService.sync(item, syncProgress));
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

    public int getNumberOfAccounts() {
        return numberOfAccounts;
    }

    public SyncProgress getSyncProgress() {
        return syncProgress;
    }
}
