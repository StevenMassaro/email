package email.service;

import email.model.SyncProgress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SyncWebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendSyncProgressUpdate(SyncProgress progress) {
        messagingTemplate.convertAndSend("/topic/sync/progress", new SyncProgressDto(
                progress.getEmailsSynced(),
                progress.getTotalEmails()
        ));
    }

    public void sendSyncComplete() {
        messagingTemplate.convertAndSend("/topic/sync/complete", "Sync completed");
    }

    public static class SyncProgressDto {
        private final int emailsSynced;
        private final int totalEmails;

        public SyncProgressDto(int emailsSynced, int totalEmails) {
            this.emailsSynced = emailsSynced;
            this.totalEmails = totalEmails;
        }

        public int getEmailsSynced() {
            return emailsSynced;
        }

        public int getTotalEmails() {
            return totalEmails;
        }
    }
}