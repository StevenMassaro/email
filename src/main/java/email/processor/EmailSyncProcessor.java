package email.processor;

import email.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
public class EmailSyncProcessor {

    @Autowired
    private SyncService syncService;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Scheduled(fixedDelay = 15 * 60 * 1000)
    public void recurringScheduler() throws ExecutionException, InterruptedException {
        // submit the task for execution
        Future future = threadPoolTaskExecutor.submit(() -> syncService.executeDifferentialSync());
        // only return from here once the task has finished executing, otherwise the task is scheduled too often
        future.get();
    }
}
