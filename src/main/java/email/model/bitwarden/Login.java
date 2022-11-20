package email.model.bitwarden;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
public class Login {
    private String password;
    private String username;

    @JsonIgnore
    public int getPort() {
        return 993;
    }
}
