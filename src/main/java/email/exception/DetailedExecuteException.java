package email.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.exec.ExecuteException;

import java.io.IOException;

@AllArgsConstructor
@Getter
public class DetailedExecuteException extends IOException {
    private final ExecuteException executeException;
    private final String consoleOutput;
}
