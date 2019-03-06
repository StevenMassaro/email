package email.model;

public class SyncStatusResult {
    private long insertedCount;
    private long deletedCount;
    private long changedReadIndCount;
    private ExecStatusEnum execStatusEnum;

    public SyncStatusResult(long insertedCount, long deletedCount, long changedReadIndCount, ExecStatusEnum execStatusEnum) {
        this.insertedCount = insertedCount;
        this.deletedCount = deletedCount;
        this.changedReadIndCount = changedReadIndCount;
        this.execStatusEnum = execStatusEnum;
    }

    public long getInsertedCount() {
        return insertedCount;
    }

    public String getInsertedCountMessage() {
        return String.format("Inserted %s new messages.", insertedCount);
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
}
