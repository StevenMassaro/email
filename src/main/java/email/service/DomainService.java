package email.service;

import email.mapper.DomainMapper;
import email.model.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DomainService {

    @Autowired
    private DomainMapper domainMapper;

    public long insert(Domain domain) {
        return domainMapper.insert(domain.getHostname(), domain.getPort(), domain.getAuthentication());
    }
}
