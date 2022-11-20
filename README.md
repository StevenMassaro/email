# Email

An email inbox aggregator, supporting listing emails via IMAP from Gmail and AOL in one aggregated view. Passwords are loaded from Bitwarden vault.

![Email list](img/list.png)

![Single email](img/singleemail.png)

## Setup

Run the docker image with environment variables for:
```
accounts=<comma separated list of Bitwarden IDs>
bitwardenEmailFolderId=<get from Bitwarden CLI, bw list folders>
bitwardenCliLocation=<path to Bitwarden CLI>
BW_CLIENTID=<get from Bitwarden vault>
BW_CLIENTSECRET=<get from Bitwarden vault>
```
Note that if the account you have specified in Bitwarden does not end in "gmail.com" or "aol.com", but is actually one of those accounts, you can add a custom field called "hostname" and specify the IMAP server address, like "imap.gmail.com". This is useful for custom domains that are hosted using GSuite.

## Steps to get Bitwarden IDs

You must have the [Bitwarden CLI](https://github.com/bitwarden/clients) installed.

- login `bw login <email>`
- unlock `bw unlock`
- find the folder ID using `bw list folders`
- find the IDs of the accounts you wish to sync by doing `bw list items --folderid <folderId>`