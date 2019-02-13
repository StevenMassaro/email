package email.mapper;

import email.model.Message;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface MessageMapper {

    Message getByUid(@Param("uid") long uid);

    Message get(@Param("id") long id);

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
    long insertMessage(@Param("uid") long uid,
                       @Param("accountId") long accountId,
                       @Param("subject") String subject,
                       @Param("dateReceived") Date dateReceived,
                       @Param("readInd") boolean readInd,
                       @Param("recipientId") long recipientId,
                       @Param("fromId") long fromId);

    long count(@Param("accountId") long accountId, @Param("subject") String subject, @Param("dateReceived") Date dateReceived);

    List<Message> listAll();

    List<Message> list(@Param("accountId") long accountId);

    long deleteById(@Param("id") long id);

    void setReadIndicator(@Param("id") long id, @Param("readInd") boolean readInd);
}
