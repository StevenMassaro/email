package email.model;

import java.util.concurrent.atomic.AtomicInteger;

public class SyncProgress {
    private final AtomicInteger totalEmails = new AtomicInteger(0);
    private final AtomicInteger emailsSynced = new AtomicInteger(0);

    public void addTotalEmails(int count) {
        totalEmails.addAndGet(count);
    }

    public void incrementEmailsSynced() {
        emailsSynced.incrementAndGet();
    }

    public int getTotalEmails() {
        return totalEmails.get();
    }

    public int getEmailsSynced() {
        return emailsSynced.get();
    }
}
