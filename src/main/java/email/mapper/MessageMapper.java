package email.mapper;

import email.model.Message;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper {

    Message get(@Param("uid") long uid);

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
    void insertMessage(@Param("uid") long uid,
                       @Param("accountId") long accountId,
                       @Param("subject") String subject,
                       @Param("dateReceived") Date dateReceived,
                       @Param("readInd") boolean readInd,
                       @Param("recipientId") long recipientId,
                       @Param("fromId") long fromId,
                       @Param("body") String body);

    long count(@Param("accountId") long accountId, @Param("subject") String subject, @Param("dateReceived") Date dateReceived);

    List<Message> listAll();

    List<Message> list(@Param("accountId") long accountId);

    long delete(@Param("messages") List<Message> messages);

    void setReadIndicator(@Param("messageId") long messageId, @Param("readInd") boolean readInd);
}
