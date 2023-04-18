package email.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ProviderEnum {
    GMAIL(false, "EEE, d MMM yyyy HH:mm:ss Z"),
    AOL(true, "EEE, d MMM yyyy HH:mm:ss Z (ZZZ)");

    private final String[] dateFormats;
    private final boolean doImapOperationsSynchronously;

    ProviderEnum(boolean doImapOperationsSynchronously, String... dateFormats) {
        this.doImapOperationsSynchronously = doImapOperationsSynchronously;
        this.dateFormats = dateFormats;
    }

    public String[] getDateFormats() {
        return dateFormats;
    }

    public boolean isDoImapOperationsSynchronously() {
        return doImapOperationsSynchronously;
    }

    public static String[] getAllDateFormats() {
        List<String> dateFormats = new ArrayList<>();
        for (ProviderEnum value : ProviderEnum.values()) {
            dateFormats.addAll(Arrays.asList(value.getDateFormats()));
        }
        return dateFormats.toArray(new String[]{});
    }
}
