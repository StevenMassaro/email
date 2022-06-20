package email.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ResultsWrapper {

    private final List<SyncStatusResult> results;
    private final boolean complete;
}
