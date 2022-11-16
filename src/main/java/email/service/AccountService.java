package email.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Log4j2
public class AccountService {

    @Value("${accounts}")
    private List<String> accountBitwardenIds;

    public List<UUID> list() {
        List<UUID> bitwardenIdList = new ArrayList<>(accountBitwardenIds.size());
        for (String accountBitwardenId : accountBitwardenIds) {
            bitwardenIdList.add(UUID.fromString(accountBitwardenId));
        }

        return bitwardenIdList;
    }
}
