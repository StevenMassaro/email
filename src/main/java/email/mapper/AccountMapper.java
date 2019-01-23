package email.mapper;

import email.model.Account;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AccountMapper {
    Account get(@Param("accountid") long accountid);

    List<Account> list();

    void insert(@Param("hostname") String hostname, @Param("port") long port, @Param("authentication") String authentication,
                @Param("inboxName") String inboxName, @Param("username") String username, @Param("password") String password);
}
