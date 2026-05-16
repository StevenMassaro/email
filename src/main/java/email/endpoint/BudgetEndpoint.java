package email.endpoint;

import email.service.ActualBudgetService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/budget")
@Log4j2
public class BudgetEndpoint {

    private final ActualBudgetService actualBudgetService;

    public BudgetEndpoint(ActualBudgetService actualBudgetService) {
        this.actualBudgetService = actualBudgetService;
    }

    @GetMapping("/accounts")
    public List<Map<String, Object>> getAccounts() throws Exception {
        return actualBudgetService.getAccounts();
    }

    @GetMapping("/category-groups")
    public List<Map<String, Object>> getCategoryGroups() throws Exception {
        return actualBudgetService.getCategoryGroups();
    }

    @GetMapping("/payees")
    public List<Map<String, Object>> getPayees() throws Exception {
        return actualBudgetService.getPayees();
    }

    @PostMapping("/transactions")
    public ResponseEntity<Object> importTransaction(@RequestBody Map<String, Object> body) throws Exception {
        String accountId = (String) body.get("accountId");
        String date = (String) body.get("date");
        Object amountObj = body.get("amount");
        String payeeName = (String) body.get("payeeName");
        String categoryId = (String) body.get("categoryId");
        String notes = (String) body.get("notes");

        int amount;
        if (amountObj instanceof Number) {
            amount = ((Number) amountObj).intValue();
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "amount is required and must be a number"));
        }

        Object result = actualBudgetService.importTransaction(accountId, date, amount, payeeName, categoryId, notes);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/amounts")
    public List<Map<String, Object>> extractAmounts(@RequestParam("emailId") long emailId) {
        return actualBudgetService.extractAmounts(emailId);
    }
}
