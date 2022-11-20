package email.model.bitwarden;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@NoArgsConstructor
public class Item {
    private UUID id;
    private Login login;
    private List<Field> fields;

    @JsonIgnore
    public String getHostname() throws Exception {
        if (!CollectionUtils.isEmpty(fields)) {
            Optional<Field> hostnameOpt = fields.stream().filter(f -> f.getName().equalsIgnoreCase("hostname")).findFirst();
            if (hostnameOpt.isPresent()) {
                return hostnameOpt.get().getValue();
            }
        }

        if (StringUtils.containsIgnoreCase(login.getUsername(), "@gmail.com")) {
            return "imap.gmail.com";
        } else if (StringUtils.containsIgnoreCase(login.getUsername(), "@aol.com")) {
            return "imap.aol.com";
        } else {
            throw new Exception("Unsupported email host: " + login.getUsername());
        }
    }
}
