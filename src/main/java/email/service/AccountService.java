package email.service;

import email.mapper.AccountMapper;
import email.model.Account;
import email.model.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Component
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private DomainService domainService;

    public List<Account> list() {
        return accountMapper.list();
    }

    public void insert(Account account) {
        long domainId = domainService.insert(account.getDomain());
        String encryptedPassword = encryptionService.encrypt(account.getPassword());
        accountMapper.insert(domainId, account.getInboxName(), account.getUsername(), encryptedPassword);
    }
}
