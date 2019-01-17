package email.mapper;

import email.model.Account;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AccountMapper {
    List<Account> list();

    void insert(@Param("domainId") long domainId, @Param("inboxName") String inboxName,
                @Param("username") String username, @Param("password") String password);
}
