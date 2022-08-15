package email.endpoint;

import email.job.SyncJob;
import email.model.ResultsWrapper;
import email.service.AccountService;
import email.service.BitwardenService;
import email.service.SyncService;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/actions")
@Log4j2
public class ActionsEndpoint {

    private final BitwardenService bitwardenService;
    private final SyncJob syncJob;

    public ActionsEndpoint(SyncService syncService, AccountService accountService, BitwardenService bitwardenService) {
        this.bitwardenService = bitwardenService;
        syncJob = new SyncJob(accountService, syncService);
    }

    @PostMapping("/sync")
    public synchronized void performSync(@RequestBody(required = false) String bitwardenMasterPassword) throws ExecutionException, InterruptedException {
        syncJob.startSync(bitwardenMasterPassword);
    }

    @GetMapping("/sync/results")
    public synchronized ResultsWrapper getSyncResults() {
        return new ResultsWrapper(syncJob.getResults(), syncJob.isComplete(), syncJob.getNumberOfAccounts());
    }

    @GetMapping("/requiresPassword")
    public boolean doesSyncRequirePassword() {
        return bitwardenService.isCacheEmpty();
    }

}
