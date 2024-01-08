package email.service;

import email.model.bitwarden.Item;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@SpringBootTest
public class BitwardenServiceTest {

    @Autowired
    private BitwardenService bitwardenService;

    @Value("classpath:BitwardenListItems.json")
    private Resource bitwardenListItemsJson;

    @Value("${testItemId}")
    private UUID testItemId;

    @Value("${testMasterPassword}")
    private String testMasterPassword;

    @Test
    public void parseSessionKeyFromUnlockOutput() {
        String unlockConsoleOutput = "Your vault is now unlocked!\n" +
                "\n" +
                "To unlock your vault, set your session key to the `BW_SESSION` environment variable. ex:\n" +
                "$ export BW_SESSION=\"cAQ0Wyv6WGURtRg0MgQl5mDWUAGFXKowuNUePMUITkM+HcKfIaz6OVvfV9/XBZ8qzG7a5PTqT4BqNxrqBfzumQ==\"\n" +
                "> $env:BW_SESSION=\"cAQ0Wyv6WGURtRg0MgQl5mDWUAGFXKowuNUePMUITkM+HcKfIaz6OVvfV9/XBZ8qzG7a5PTqT4BqNxrqBfzumQ==\"\n" +
                "\n" +
                "You can also pass the session key to any command with the `--session` option. ex:\n" +
                "$ bw list items --session cAQ0Wyv6WGURtRg0MgQl5mDWUAGFXKowuNUePMUITkM+HcKfIaz6OVvfV9/XBZ8qzG7a5PTqT4BqNxrqBfzumQ==";

        String sessionKey = BitwardenService.parseSessionKeyFromUnlockOutput(unlockConsoleOutput);
        assertEquals("cAQ0Wyv6WGURtRg0MgQl5mDWUAGFXKowuNUePMUITkM+HcKfIaz6OVvfV9/XBZ8qzG7a5PTqT4BqNxrqBfzumQ==", sessionKey);
    }

    @Test
    public void testLoadingSampleLoginFromVault() throws InterruptedException, ExecutionException, IOException {
        Item login = bitwardenService.getLogin(testItemId, testMasterPassword.trim());
        assertEquals(testItemId, login.getId());
        assertEquals("testuser", login.getLogin().getUsername());
        assertEquals("testpw", login.getLogin().getPassword());
        assertEquals(1, login.getFields().size());
        assertEquals("testfield", login.getFields().get(0).getName());
        assertEquals("testfieldvalue", login.getFields().get(0).getValue());
    }

    @Test
    public void testDeserializingBitwardenJson() throws IOException {
        String json = IOUtils.toString(bitwardenListItemsJson.getInputStream(), StandardCharsets.UTF_8);
        List<Item> items = bitwardenService.deserializeBitwardenJson(json);
        Item one = items.get(0);
        assertEquals(UUID.fromString("d0f3b7f6-8fc5-41fa-9b4e-acd190ce21d1"), one.getId());
        assertEquals("crappypassword", one.getLogin().getPassword());
        assertEquals("steveusername", one.getLogin().getUsername());
        Item two = items.get(1);
        assertEquals(UUID.fromString("8d607371-6e50-4802-845e-acd200ce21d1"), two.getId());
        assertEquals("mompassword", two.getLogin().getPassword());
        assertEquals("momusername", two.getLogin().getUsername());
        assertEquals(1, two.getFields().size());
        assertEquals("hostname", two.getFields().get(0).getName());
        assertEquals("imap.gmail.com", two.getFields().get(0).getValue());
    }
}
