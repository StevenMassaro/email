# Email

An email inbox aggregator, supporting listing emails from Gmail and AOL in one aggregated view.

![Email list](img/list.png)

![Single email](img/singleemail.png)

## Setup

Run the docker image with environment variables for:
```
accounts={'id|imap_hostname|username|encrypted_password','...'}
initVector=
key=
```

## Future plans
- Connect to Bitwarden to obtain passwords for clients on the fly