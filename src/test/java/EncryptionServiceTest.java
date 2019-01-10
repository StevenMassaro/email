import email.service.EncryptionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EncryptionServiceTest {

    private EncryptionService encryptionService = new EncryptionService();

    @Before
    public void before() {
        encryptionService.setInitVector("fartfartfartfart");
        encryptionService.setKey("hellohellohelloh");
    }

    @Test
    public void testBeans() {
        assertNotNull(encryptionService);
    }

    @Test
    public void roundRobin() {
        String start = "start";
        String encrypted = encryptionService.encrypt(start);
        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals(start, decrypted);
    }
}
