package email.model.bitwarden;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class Item {
    private UUID id;
    private Login login;
}
