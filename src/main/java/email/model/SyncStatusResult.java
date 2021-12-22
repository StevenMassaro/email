package email.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SyncStatusResult {
    private long insertedCount;
    private long deletedCount;
    private long changedReadIndCount;
    private ExecStatusEnum execStatusEnum;
    private String username;
}
