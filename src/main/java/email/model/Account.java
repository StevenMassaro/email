package email.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Account {
    private long id;
    private String hostname;
    private long port;
    private String authentication;
    private String inboxName;
    private String username;
    @JsonIgnore
    private String password;
    private Date dateCreated;
}
