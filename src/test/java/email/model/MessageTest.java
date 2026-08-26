package email.model;

import org.junit.Test;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class MessageTest {

    private static class DelegatingMessage extends javax.mail.Message {
        private final javax.mail.Message delegate;

        public DelegatingMessage(javax.mail.Message delegate) {
            this.delegate = delegate;
        }

        @Override
        public Address[] getFrom() throws MessagingException {
            return delegate.getFrom();
        }

        @Override
        public void setFrom() throws MessagingException {
            delegate.setFrom();
        }

        @Override
        public void setFrom(Address address) throws MessagingException {
            delegate.setFrom(address);
        }

        @Override
        public void addFrom(Address[] addresses) throws MessagingException {
            delegate.addFrom(addresses);
        }

        @Override
        public Address[] getRecipients(RecipientType type) throws MessagingException {
            return delegate.getRecipients(type);
        }

        @Override
        public void setRecipients(RecipientType type, Address[] addresses) throws MessagingException {
            delegate.setRecipients(type, addresses);
        }

        @Override
        public void addRecipients(RecipientType type, Address[] addresses) throws MessagingException {
            delegate.addRecipients(type, addresses);
        }

        @Override
        public String getSubject() throws MessagingException {
            return delegate.getSubject();
        }

        @Override
        public void setSubject(String subject) throws MessagingException {
            delegate.setSubject(subject);
        }

        @Override
        public Date getSentDate() throws MessagingException {
            return delegate.getSentDate();
        }

        @Override
        public void setSentDate(Date date) throws MessagingException {
            delegate.setSentDate(date);
        }

        @Override
        public Date getReceivedDate() throws MessagingException {
            return delegate.getReceivedDate();
        }

        @Override
        public Flags getFlags() throws MessagingException {
            return delegate.getFlags();
        }

        @Override
        public void setFlags(Flags flag, boolean set) throws MessagingException {
            delegate.setFlags(flag, set);
        }

        @Override
        public javax.mail.Message reply(boolean replyToAll) throws MessagingException {
            return delegate.reply(replyToAll);
        }

        @Override
        public void saveChanges() throws MessagingException {
            delegate.saveChanges();
        }

        @Override
        public int getSize() throws MessagingException {
            return delegate.getSize();
        }

        @Override
        public int getLineCount() throws MessagingException {
            return delegate.getLineCount();
        }

        @Override
        public String getContentType() throws MessagingException {
            return delegate.getContentType();
        }

        @Override
        public boolean isMimeType(String mimeType) throws MessagingException {
            return delegate.isMimeType(mimeType);
        }

        @Override
        public String getDisposition() throws MessagingException {
            return delegate.getDisposition();
        }

        @Override
        public void setDisposition(String disposition) throws MessagingException {
            delegate.setDisposition(disposition);
        }

        @Override
        public String getDescription() throws MessagingException {
            return delegate.getDescription();
        }

        @Override
        public void setDescription(String description) throws MessagingException {
            delegate.setDescription(description);
        }

        @Override
        public String getFileName() throws MessagingException {
            return delegate.getFileName();
        }

        @Override
        public void setFileName(String filename) throws MessagingException {
            delegate.setFileName(filename);
        }

        @Override
        public InputStream getInputStream() throws IOException, MessagingException {
            return delegate.getInputStream();
        }

        @Override
        public DataHandler getDataHandler() throws MessagingException {
            return delegate.getDataHandler();
        }

        @Override
        public void setDataHandler(DataHandler dh) throws MessagingException {
            delegate.setDataHandler(dh);
        }

        @Override
        public Object getContent() throws IOException, MessagingException {
            return delegate.getContent();
        }

        @Override
        public void setContent(Object obj, String type) throws MessagingException {
            delegate.setContent(obj, type);
        }

        @Override
        public void setText(String text) throws MessagingException {
            delegate.setText(text);
        }

        @Override
        public void setContent(Multipart mp) throws MessagingException {
            delegate.setContent(mp);
        }

        @Override
        public void writeTo(OutputStream os) throws IOException, MessagingException {
            delegate.writeTo(os);
        }

        @Override
        public void setHeader(String header_name, String header_value) throws MessagingException {
            delegate.setHeader(header_name, header_value);
        }

        @Override
        public void addHeader(String header_name, String header_value) throws MessagingException {
            delegate.addHeader(header_name, header_value);
        }

        @Override
        public void removeHeader(String header_name) throws MessagingException {
            delegate.removeHeader(header_name);
        }

        @Override
        public Enumeration<Header> getAllHeaders() throws MessagingException {
            return delegate.getAllHeaders();
        }

        @Override
        public Enumeration<Header> getMatchingHeaders(String[] header_names) throws MessagingException {
            return delegate.getMatchingHeaders(header_names);
        }

        @Override
        public Enumeration<Header> getNonMatchingHeaders(String[] header_names) throws MessagingException {
            return delegate.getNonMatchingHeaders(header_names);
        }

        @Override
        public String[] getHeader(String header_name) throws MessagingException {
            return delegate.getHeader(header_name);
        }
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
                return new Flags();
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

            @Override
            public String[] getHeader(String header_name) throws MessagingException {
                if (header_name.equals("Date")) {
                    return new String[]{date};
                }
                return null;
            }
        };
    }

    @Test
    public void confirmDatesCanBeParsed() throws MessagingException {
        // AOL examples
        assertEquals(1643037015000L, Message.getReceivedDate(getMessage("Mon, 24 Jan 2022 08:10:15 -0700 (MST)")).getTime());
        assertEquals(1644041949000L, Message.getReceivedDate(getMessage("Fri, 4 Feb 2022 23:19:09 +0000 (MST)")).getTime());
        // Gmail examples
        assertEquals(1648363345000L, Message.getReceivedDate(getMessage("Sun, 27 Mar 2022 00:42:25 -0600")).getTime());
        assertEquals(1644016749000L, Message.getReceivedDate(getMessage("Fri, 4 Feb 2022 23:19:09 +0000")).getTime());
    }

    @Test
    public void testAmazonObfuscationSingleQuotePair() throws Exception {
        // Test with a single quote pair
        javax.mail.Message base = getMessage("Mon, 24 Jan 2022 08:10:15 -0700 (MST)");
        javax.mail.Message mockMessage = new DelegatingMessage(base) {
            @Override
            public String getSubject() throws MessagingException {
                return "\"Order #123\"";
            }

            @Override
            public Address[] getFrom() throws MessagingException {
                InternetAddress address = new InternetAddress("shipment-tracking@amazon.com");
                return new Address[]{address};
            }
        };

        Message message = new Message(mockMessage, 1L, false, "testuser", UUID.randomUUID(), true);
        assertEquals("\"*****\"", message.getSubject());
    }

    @Test
    public void testAmazonObfuscationMultipleQuotePairs() throws Exception {
        // Test with multiple quote pairs - should replace from first to last quote
        javax.mail.Message base = getMessage("Mon, 24 Jan 2022 08:10:15 -0700 (MST)");
        javax.mail.Message mockMessage = new DelegatingMessage(base) {
            @Override
            public String getSubject() throws MessagingException {
                return "\"Order #123\" shipped via \"Carrier XYZ\"";
            }

            @Override
            public Address[] getFrom() throws MessagingException {
                InternetAddress address = new InternetAddress("auto-confirm@amazon.com");
                return new Address[]{address};
            }
        };

        Message message = new Message(mockMessage, 1L, false, "testuser", UUID.randomUUID(), true);
        assertEquals("\"*****\"", message.getSubject());
    }

    @Test
    public void testAmazonObfuscationNoQuotes() throws Exception {
        // Test with no quotes - should remain unchanged
        javax.mail.Message base = getMessage("Mon, 24 Jan 2022 08:10:15 -0700 (MST)");
        javax.mail.Message mockMessage = new DelegatingMessage(base) {
            @Override
            public String getSubject() throws MessagingException {
                return "Order #123 shipped via Carrier XYZ";
            }

            @Override
            public Address[] getFrom() throws MessagingException {
                InternetAddress address = new InternetAddress("shipment-tracking@amazon.com");
                return new Address[]{address};
            }
        };

        Message message = new Message(mockMessage, 1L, false, "testuser", UUID.randomUUID(), true);
        assertEquals("Order #123 shipped via Carrier XYZ", message.getSubject());
    }

    @Test
    public void testAmazonObfuscationNonAmazonAddress() throws Exception {
        // Test with non-Amazon address - should not obfuscate
        javax.mail.Message base = getMessage("Mon, 24 Jan 2022 08:10:15 -0700 (MST)");
        javax.mail.Message mockMessage = new DelegatingMessage(base) {
            @Override
            public String getSubject() throws MessagingException {
                return "\"Order #123\" shipped via \"Carrier XYZ\"";
            }

            @Override
            public Address[] getFrom() throws MessagingException {
                InternetAddress address = new InternetAddress("sender@example.com");
                return new Address[]{address};
            }
        };

        Message message = new Message(mockMessage, 1L, false, "testuser", UUID.randomUUID(), true);
        assertEquals("\"Order #123\" shipped via \"Carrier XYZ\"", message.getSubject());
    }
}