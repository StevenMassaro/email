package email.service;

import email.mapper.AccountMapper;
import email.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private EncryptionService encryptionService;

    public List<Account> list() {
        return accountMapper.list();
    }

    public Account getDecrypted(long accountid) {
        Account account = accountMapper.get(accountid);
        account.setPassword(encryptionService.decrypt(account.getPassword()));
        return account;
    }

    public void insert(Account account) {
        String encryptedPassword = encryptionService.encrypt(account.getPassword());
        accountMapper.insert(account.getHostname(), account.getPort(), account.getAuthentication(), account.getInboxName(), account.getUsername(), encryptedPassword);
    }
}
