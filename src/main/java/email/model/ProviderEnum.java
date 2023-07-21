package email.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public enum ProviderEnum {
    GMAIL(false, "[Gmail]/Trash", "[Gmail]/All Mail", "EEE, d MMM yyyy HH:mm:ss Z"),
    AOL(true, "Trash", null, "EEE, d MMM yyyy HH:mm:ss Z (ZZZ)");

    private final String[] dateFormats;
    private final boolean doImapOperationsSynchronously;
    private final String trashFolderName;
    private final String archiveFolderName;

    ProviderEnum(boolean doImapOperationsSynchronously, String trashFolderName, String archiveFolderName, String... dateFormats) {
        this.doImapOperationsSynchronously = doImapOperationsSynchronously;
        this.trashFolderName = trashFolderName;
        this.archiveFolderName = archiveFolderName;
        this.dateFormats = dateFormats;
    }

    public static String[] getAllDateFormats() {
        List<String> dateFormats = new ArrayList<>();
        for (ProviderEnum value : ProviderEnum.values()) {
            dateFormats.addAll(Arrays.asList(value.getDateFormats()));
        }
        return dateFormats.toArray(new String[]{});
    }
}
