package email.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.UUID;

@Data
public class Account {
    private long id;
    private String hostname;
    private long port;
    private String authentication;
    private String inboxName;
    private String username;
    @JsonIgnore
    private UUID bitwardenItemId;
}
