package email.model;

public class Address {

    private long id;
    private Boolean groupInd;
    private String groupList;
    private String groupName;
    private String address;
    private String encodedPersonal;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Boolean getGroupInd() {
        return groupInd;
    }

    public void setGroupInd(Boolean groupInd) {
        this.groupInd = groupInd;
    }

    public String getGroupList() {
        return groupList;
    }

    public void setGroupList(String groupList) {
        this.groupList = groupList;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEncodedPersonal() {
        return encodedPersonal;
    }

    public void setEncodedPersonal(String encodedPersonal) {
        this.encodedPersonal = encodedPersonal;
    }
}
