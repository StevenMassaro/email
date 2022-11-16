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
    public String getHostname() throws Exception {
        if (StringUtils.containsIgnoreCase(username, "@gmail.com")) {
            return "imap.gmail.com";
        } else if (StringUtils.containsIgnoreCase(username, "@aol.com")) {
            return "imap.aol.com";
        } else {
            throw new Exception("Unsupported email host: " + username);
        }
    }

    @JsonIgnore
    public int getPort() {
        return 993;
    }
}
