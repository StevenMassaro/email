package email.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Address {

    private long id;
    private Boolean groupInd;
    private String groupList;
    private String groupName;
    private String address;
    private String encodedPersonal;
}
