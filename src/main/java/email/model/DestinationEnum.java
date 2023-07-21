package email.model;

import lombok.Getter;

import java.util.function.Function;

@Getter
public enum DestinationEnum {
    trash("Delete", ProviderEnum::getTrashFolderName),
    archive("Archive", ProviderEnum::getArchiveFolderName);

    private final String displayName;
    private final Function<ProviderEnum, String> folderNameGetterCallback;

    DestinationEnum(String displayName, Function<ProviderEnum, String> folderNameGetterCallback) {
        this.displayName = displayName;
        this.folderNameGetterCallback = folderNameGetterCallback;
    }

    public String getFolderName(ProviderEnum provider) {
        return folderNameGetterCallback.apply(provider);
    }
}
