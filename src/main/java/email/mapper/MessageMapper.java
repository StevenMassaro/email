package email.mapper;

import email.model.Message;
import org.apache.ibatis.annotations.*;

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
    @Insert("INSERT INTO " + schema + "(accountid, subject, datereceived, readind, recipientid, fromid, bodyid, datecreated) VALUES(" +
            "#{accountId}, #{subject}, #{dateReceived}, #{readInd}, #{recipientId}, #{fromId}, #{bodyId}, #{dateCreated})")
    void insertMessage(@Param("accountId") long accountId,
                       @Param("subject") String subject,
                       @Param("dateReceived") Date dateReceived,
                       @Param("readInd") boolean readInd,
                       @Param("recipientId") long recipientId,
                       @Param("fromId") long fromId,
                       @Param("bodyId") long bodyId,
                       @Param("dateCreated") Date dateCreated);

    @Select("SELECT count(*) FROM email.message WHERE accountId = #{accountId} AND subject = #{subject} AND dateReceived = #{dateReceived}")
    long count(@Param("accountId") long accountId, @Param("subject") String subject, @Param("dateReceived") Date dateReceived);

    List<Message> list(@Param("accountId") long accountId);

    long delete(@Param("messages") List<Message> messages);

    @Update("UPDATE " + schema + " SET readInd = #{readInd} WHERE id = #{messageId}")
    void setReadIndicator(long messageId, boolean readInd);
}
