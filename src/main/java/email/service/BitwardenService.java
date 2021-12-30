package email.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import email.exception.DetailedExecuteException;
import email.model.bitwarden.Item;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class BitwardenService {

    private final ObjectMapper jacksonObjectMapper;

    @Value("${bitwardenCliLocation}")
    private String bitwardenCliLocation;

    @Value("${bitwardenEmailFolderId}")
    private UUID bitwardenEmailFolderId;

    @Value("${BW_CLIENTSECRET}")
    private String bitwardenApiKey;

    private static final Cache<UUID, String> passwordCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(7, TimeUnit.DAYS)
            .build();

    public BitwardenService(ObjectMapper jacksonObjectMapper) {
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

    public synchronized String getPassword(UUID id, String bitwardenMasterPassword) throws InterruptedException, IOException, ExecutionException {
        return passwordCache.get(id, () -> {
            try {
                loginWithApiKey();
            } catch (DetailedExecuteException e) {
                if (e.getConsoleOutput().contains("You are already logged in as ")) {
                    log.trace("Already logged in");
                }
            }
            String sessionKey = unlock(bitwardenMasterPassword);
            String passwordListJson = listPasswordsFromCli(sessionKey);
            List<Item> items = deserializeBitwardenJson(passwordListJson);
            // put all of the passwords into the cache
            for (Item item : items) {
                passwordCache.put(item.getId(), item.getLogin().getPassword());
            }
            return passwordCache.getIfPresent(id);
        });
    }

    public List<Item> deserializeBitwardenJson(String json) throws IOException {
        return jacksonObjectMapper.readValue(json, new TypeReference<List<Item>>(){});
    }

    public String getPasswordFromCache(UUID id) {
        return passwordCache.getIfPresent(id);
    }

    private String unlock(String bitwardenMasterPassword) throws IOException {
        File f = File.createTempFile("bwpw",".txt");
        log.debug("Created temporary password file {}", f.getAbsolutePath());
        f.deleteOnExit();
        try {
            FileUtils.write(f, bitwardenMasterPassword, StandardCharsets.UTF_8);
            String output = runCommand(new String[]{bitwardenCliLocation, "unlock", "--passwordfile", f.getAbsolutePath()});
            return parseSessionKeyFromUnlockOutput(output);
        } finally {
            boolean deletedPasswordFileSuccessfully = f.delete();
            if (!deletedPasswordFileSuccessfully) {
                log.error("Failed to delete temporary password file");
            }
        }
    }

    /**
     * Given some output from the bw unlock command, parse the session key out of the output.
     * @param output the console output from the bw unlock command
     * @return the session key
     */
    public static String parseSessionKeyFromUnlockOutput(String output) {
        String startOfSessionKeyIdentifier = "export BW_SESSION=\"";
        int startOfSessionKey = output.indexOf(startOfSessionKeyIdentifier);
        String sessionKey = output.substring(startOfSessionKey + startOfSessionKeyIdentifier.length(), output.indexOf("\"", startOfSessionKey + startOfSessionKeyIdentifier.length()));
        log.trace("Identified session key {}", sessionKey);
        return sessionKey;
    }

    public boolean isCacheEmpty() {
        return passwordCache.size() == 0;
    }

    private String listPasswordsFromCli(String sessionKey) throws IOException {
        return runCommand(new String[]{bitwardenCliLocation, "list", "items", "--folderid", bitwardenEmailFolderId.toString(), "--session", sessionKey});
    }

    private void loginWithApiKey() throws IOException {
        runCommand(new String[]{bitwardenCliLocation, "login", "--apikey", bitwardenApiKey});
    }

    private String runCommand(String[] commands) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CommandLine commandline = CommandLine.parse(StringUtils.join(commands, " "));
        DefaultExecutor exec = new DefaultExecutor();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        exec.setStreamHandler(streamHandler);
        String output;
        try {
            exec.execute(commandline);
        } catch (ExecuteException e) {
            throw new DetailedExecuteException(e, outputStream.toString());
        } finally {
            output = outputStream.toString();
            log.trace(output);
        }
        return output;
    }
}
