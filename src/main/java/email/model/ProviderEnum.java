package email.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ProviderEnum {
    GMAIL("EEE, d MMM yyyy HH:mm:ss Z"),
    AOL("EEE, d MMM yyyy HH:mm:ss Z (ZZZ)");

    private final String[] dateFormats;

    ProviderEnum(String... dateFormats) {
        this.dateFormats = dateFormats;
    }

    public String[] getDateFormats() {
        return dateFormats;
    }

    public static String[] getAllDateFormats() {
        List<String> dateFormats = new ArrayList<>();
        for (ProviderEnum value : ProviderEnum.values()) {
            dateFormats.addAll(Arrays.asList(value.getDateFormats()));
        }
        return dateFormats.toArray(new String[]{});
    }
}
