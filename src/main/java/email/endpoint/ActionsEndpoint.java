package email.endpoint;

import email.job.SyncJob;
import email.model.ResultsWrapper;
import email.model.SyncProgress;
import email.service.BitwardenService;
import email.service.SyncService;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/actions")
@Log4j2
public class ActionsEndpoint {

    private final BitwardenService bitwardenService;
    private final SyncJob syncJob;

    public ActionsEndpoint(SyncService syncService, BitwardenService bitwardenService) {
        this.bitwardenService = bitwardenService;
        syncJob = new SyncJob(bitwardenService, syncService);
    }

    @PostMapping("/sync")
    public synchronized void performSync(@RequestBody(required = false) String bitwardenMasterPassword) throws Exception {
        syncJob.startSync(bitwardenMasterPassword);
    }

    @GetMapping("/sync/results")
    public synchronized ResultsWrapper getSyncResults() {
        SyncProgress progress = syncJob.getSyncProgress();
        int emailsSynced = progress != null ? progress.getEmailsSynced() : 0;
        int totalEmails = progress != null ? progress.getTotalEmails() : 0;
        return new ResultsWrapper(syncJob.getResults(), syncJob.isComplete(), syncJob.getNumberOfAccounts(), emailsSynced, totalEmails);
    }

    @GetMapping("/requiresPassword")
    public boolean doesSyncRequirePassword() {
        return bitwardenService.isCacheEmpty();
    }

}
