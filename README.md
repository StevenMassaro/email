# Email

An email inbox aggregator, supporting listing emails via IMAP from Gmail and AOL in one aggregated view. Passwords are loaded from Bitwarden vault.

![Email list](img/list.png)

![Single email](img/singleemail.png)

## Setup

Run the docker image with environment variables for:
```
accounts={'id|imap_hostname|username|bitwarden','...'}
bitwardenEmailFolderId=<get from bitwarden CLI, bw list folders>
bitwardenCliLocation=<path to bitwarden CLI>
BW_CLIENTID=<get from bitwarden vault>
BW_CLIENTSECRET=<get from bitwarden vault>
```