package email.endpoint;

import email.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/actions")
public class ActionsEndpoint {

    @Lazy
    @Autowired
    private SyncService syncService;

    @GetMapping("/sync/{type}")
    public String performSync(@PathVariable String type) {
        try {
            syncService.attemptToScheduleDifferentialSync();
            return "Inserted new sync job.";
        } catch (TaskRejectedException e) {
            return "Failed to insert new sync job.";
        }
    }
}
