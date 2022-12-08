package email;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FileEncodingTest {

    @Test
    public void testFileEncodingUtf8() {
        assertEquals("UTF-8", System.getProperty("file.encoding"));
    }
}
