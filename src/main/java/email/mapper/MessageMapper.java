package email.mapper;

import email.model.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper {

    String table = "message";
    String schema = DATABASE + "." + table;

    // I think this is right but it probably should be in an xml file
//    @Insert("INSERT INTO " + schema + "(accountid, subject, datereceived, recipientid, fromid, bodyid, datecreated) VALUES(" +
//            "<foreach collection=\"recipientid\"" +
//            "item=\"child\"" +
//            "index=\"index\"" +
//            "open=\"(\"" +
//            "close=\")\"" +
//            "separator=\") , (\">" +
//            "#{accountId}, #{subject}, #{dateReceived}, #{child}, #{fromId}, #{bodyId}, #{dateCreated}"+
//            "</foreach>)"
//    )
    @Insert("INSERT INTO " + schema + "(accountid, subject, datereceived, recipientid, fromid, bodyid, datecreated) VALUES(" +
            "#{accountId}, #{subject}, #{dateReceived}, #{recipientId}, #{fromId}, #{bodyId}, #{dateCreated})")
    void insertMessage(@Param("accountId") long accountId,
                       @Param("subject") String subject,
                       @Param("dateReceived") Date dateReceived,
                       @Param("recipientId") long recipientId,
                       @Param("fromId") long fromId,
                       @Param("bodyId") long bodyId,
                       @Param("dateCreated") Date dateCreated);

    @Select("SELECT count(*) FROM email.message WHERE accountId = #{accountId} AND subject = #{subject} AND dateReceived = #{dateReceived}")
    long count(@Param("accountId") long accountId, @Param("subject") String subject, @Param("dateReceived") Date dateReceived);
}
