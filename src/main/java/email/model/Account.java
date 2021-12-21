package email.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public long getPort() {
        return port;
    }

    public void setPort(long port) {
        this.port = port;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public String getInboxName() {
        return inboxName;
    }

    public void setInboxName(String inboxName) {
        this.inboxName = inboxName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
