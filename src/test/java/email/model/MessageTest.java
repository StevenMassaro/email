package email.model;

import org.junit.Test;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.Address;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;

import static org.junit.Assert.assertEquals;

public class MessageTest {

    @Test
    public void confirmDatesCanBeParsed() throws MessagingException {
        // AOL examples
        assertEquals(1643037015000L, Message.getReceivedDate(getMessage("Mon, 24 Jan 2022 08:10:15 -0700 (MST)")).getTime());
        assertEquals(1644016749000L, Message.getReceivedDate(getMessage("Fri, 4 Feb 2022 23:19:09 +0000 (UTC)")).getTime());
        // Gmail examples
        assertEquals(1648363345000L, Message.getReceivedDate(getMessage("Sun, 27 Mar 2022 00:42:25 -0600")).getTime());
        assertEquals(1644016749000L, Message.getReceivedDate(getMessage("Fri, 4 Feb 2022 23:19:09 +0000")).getTime());
    }

    private javax.mail.Message getMessage(String date) {
        return new javax.mail.Message() {
            @Override
            public Address[] getFrom() throws MessagingException {
                return new Address[0];
            }

            @Override
            public void setFrom() throws MessagingException {

            }

            @Override
            public void setFrom(Address address) throws MessagingException {

            }

            @Override
            public void addFrom(Address[] addresses) throws MessagingException {

            }

            @Override
            public Address[] getRecipients(RecipientType type) throws MessagingException {
                return new Address[0];
            }

            @Override
            public void setRecipients(RecipientType type, Address[] addresses) throws MessagingException {

            }

            @Override
            public void addRecipients(RecipientType type, Address[] addresses) throws MessagingException {

            }

            @Override
            public String getSubject() throws MessagingException {
                return null;
            }

            @Override
            public void setSubject(String subject) throws MessagingException {

            }

            @Override
            public Date getSentDate() throws MessagingException {
                return null;
            }

            @Override
            public void setSentDate(Date date) throws MessagingException {

            }

            @Override
            public Date getReceivedDate() throws MessagingException {
                return null;
            }

            @Override
            public Flags getFlags() throws MessagingException {
                return null;
            }

            @Override
            public void setFlags(Flags flag, boolean set) throws MessagingException {

            }

            @Override
            public javax.mail.Message reply(boolean replyToAll) throws MessagingException {
                return null;
            }

            @Override
            public void saveChanges() throws MessagingException {

            }

            @Override
            public int getSize() throws MessagingException {
                return 0;
            }

            @Override
            public int getLineCount() throws MessagingException {
                return 0;
            }

            @Override
            public String getContentType() throws MessagingException {
                return null;
            }

            @Override
            public boolean isMimeType(String mimeType) throws MessagingException {
                return false;
            }

            @Override
            public String getDisposition() throws MessagingException {
                return null;
            }

            @Override
            public void setDisposition(String disposition) throws MessagingException {

            }

            @Override
            public String getDescription() throws MessagingException {
                return null;
            }

            @Override
            public void setDescription(String description) throws MessagingException {

            }

            @Override
            public String getFileName() throws MessagingException {
                return null;
            }

            @Override
            public void setFileName(String filename) throws MessagingException {

            }

            @Override
            public InputStream getInputStream() throws IOException, MessagingException {
                return null;
            }

            @Override
            public DataHandler getDataHandler() throws MessagingException {
                return null;
            }

            @Override
            public Object getContent() throws IOException, MessagingException {
                return null;
            }

            @Override
            public void setDataHandler(DataHandler dh) throws MessagingException {

            }

            @Override
            public void setContent(Object obj, String type) throws MessagingException {

            }

            @Override
            public void setText(String text) throws MessagingException {

            }

            @Override
            public void setContent(Multipart mp) throws MessagingException {

            }

            @Override
            public void writeTo(OutputStream os) throws IOException, MessagingException {

            }

            @Override
            public String[] getHeader(String header_name) throws MessagingException {
                if (header_name.equals("Date")) {
                    return new String[]{date};
                }
                return null;
            }

            @Override
            public void setHeader(String header_name, String header_value) throws MessagingException {

            }

            @Override
            public void addHeader(String header_name, String header_value) throws MessagingException {

            }

            @Override
            public void removeHeader(String header_name) throws MessagingException {

            }

            @Override
            public Enumeration<Header> getAllHeaders() throws MessagingException {
                return null;
            }

            @Override
            public Enumeration<Header> getMatchingHeaders(String[] header_names) throws MessagingException {
                return null;
            }

            @Override
            public Enumeration<Header> getNonMatchingHeaders(String[] header_names) throws MessagingException {
                return null;
            }
        };
    }
}
