package email.model.bitwarden;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Field {
    private String name;
    private String value;
}
