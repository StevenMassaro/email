package email.model;

public class SyncStatusResult {
    private long insertedCount;
    private long deletedCount;
    private long changedReadIndCount;
    private ExecStatusEnum execStatusEnum;
    private String username;

    public SyncStatusResult(long insertedCount, long deletedCount, long changedReadIndCount, ExecStatusEnum execStatusEnum, String username) {
        this.insertedCount = insertedCount;
        this.deletedCount = deletedCount;
        this.changedReadIndCount = changedReadIndCount;
        this.execStatusEnum = execStatusEnum;
        this.username = username;
    }

    public long getInsertedCount() {
        return insertedCount;
    }

    public void setInsertedCount(long insertedCount) {
        this.insertedCount = insertedCount;
    }

    public long getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(long deletedCount) {
        this.deletedCount = deletedCount;
    }

    public long getChangedReadIndCount() {
        return changedReadIndCount;
    }

    public void setChangedReadIndCount(long changedReadIndCount) {
        this.changedReadIndCount = changedReadIndCount;
    }

    public ExecStatusEnum getExecStatusEnum() {
        return execStatusEnum;
    }

    public void setExecStatusEnum(ExecStatusEnum execStatusEnum) {
        this.execStatusEnum = execStatusEnum;
    }

    public long getTotalChanges() {
        return insertedCount + deletedCount + changedReadIndCount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
