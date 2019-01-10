package email.processor;

import org.springframework.scheduling.annotation.Scheduled;

public interface IProcessor {

    @Scheduled
    void run();
}
