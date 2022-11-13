package email.model.bitwarden;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Login {
    private String password;
    private String username;
}
