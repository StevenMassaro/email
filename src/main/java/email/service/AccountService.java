package email.service;

import email.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AccountService {

    @Value("#{${accounts}}")
    private List<String> accounts;

    @Autowired
    private EncryptionService encryptionService;

    public List<Account> list() {
        List<Account> accountList = new ArrayList<>(accounts.size());
        for (String accountString : accounts) {
            String[] accountStringSplit = accountString.split("\\|");

            Account account = new Account();
            account.setId(Long.parseLong(accountStringSplit[0]));
            account.setUsername(accountStringSplit[2]);
            account.setPassword(accountStringSplit[3]);
            account.setAuthentication("SSL");
            account.setHostname(accountStringSplit[1]);
            account.setInboxName("Inbox");
            account.setPort(993);
            accountList.add(account);
        }

        return accountList;
    }

    public Account getDecrypted(long accountid) {
        Account account = list().stream().filter(a -> a.getId() == accountid).findFirst().get();
        account.setPassword(encryptionService.decrypt(account.getPassword()));
        return account;
    }
}
