package email.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import email.model.BodyPart;
import email.model.ContentTypeEnum;
import email.model.Message;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Log4j2
public class ActualBudgetService {

    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final HttpClient httpClient;

    @Value("${actual.serverUrl:http://localhost:5007}")
    private String serverUrl;

    @Value("${actual.apiKey:}")
    private String apiKey;

    @Value("${actual.syncId:}")
    private String syncId;

    @Value("${actual.encryptionPassword:}")
    private String encryptionPassword;

    public ActualBudgetService(ObjectMapper objectMapper, MessageService messageService) {
        this.objectMapper = objectMapper;
        this.messageService = messageService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<Map<String, Object>> getAccounts() throws Exception {
        JsonNode data = get("/v1/budgets/" + syncId + "/accounts").path("data");
        List<Map<String, Object>> accounts = new ArrayList<>();
        if (data.isArray()) {
            for (JsonNode node : data) {
                Map<String, Object> account = new LinkedHashMap<>();
                account.put("id", node.path("id").asText());
                account.put("name", node.path("name").asText());
                account.put("offbudget", node.path("offbudget").asBoolean());
                account.put("closed", node.path("closed").asBoolean());
                accounts.add(account);
            }
        }
        return accounts;
    }

    public List<Map<String, Object>> getCategoryGroups() throws Exception {
        JsonNode data = get("/v1/budgets/" + syncId + "/categorygroups").path("data");
        List<Map<String, Object>> groups = new ArrayList<>();
        if (data.isArray()) {
            for (JsonNode groupNode : data) {
                Map<String, Object> group = new LinkedHashMap<>();
                group.put("id", groupNode.path("id").asText());
                group.put("name", groupNode.path("name").asText());
                group.put("is_income", nodeToBoolean(groupNode.path("is_income")));
                group.put("hidden", nodeToBoolean(groupNode.path("hidden")));

                List<Map<String, Object>> categories = new ArrayList<>();
                JsonNode catsNode = groupNode.path("categories");
                if (catsNode.isArray()) {
                    for (JsonNode catNode : catsNode) {
                        Map<String, Object> category = new LinkedHashMap<>();
                        category.put("id", catNode.path("id").asText());
                        category.put("name", catNode.path("name").asText());
                        category.put("is_income", nodeToBoolean(catNode.path("is_income")));
                        category.put("hidden", nodeToBoolean(catNode.path("hidden")));
                        category.put("group_id", catNode.path("group_id").asText());
                        categories.add(category);
                    }
                }
                group.put("categories", categories);
                groups.add(group);
            }
        }
        return groups;
    }

    public List<Map<String, Object>> getPayees() throws Exception {
        JsonNode data = get("/v1/budgets/" + syncId + "/payees").path("data");
        List<Map<String, Object>> payees = new ArrayList<>();
        if (data.isArray()) {
            for (JsonNode node : data) {
                Map<String, Object> payee = new LinkedHashMap<>();
                payee.put("id", node.path("id").asText());
                payee.put("name", node.path("name").asText());
                payees.add(payee);
            }
        }
        return payees;
    }

    public JsonNode importTransaction(String accountId, String date, int amount, String payeeName, String categoryId, String notes) throws Exception {
        Map<String, Object> transaction = new LinkedHashMap<>();
        transaction.put("account", accountId);
        transaction.put("date", date);
        transaction.put("amount", amount);
        if (StringUtils.isNotEmpty(payeeName)) {
            transaction.put("payee_name", payeeName);
        }
        if (StringUtils.isNotEmpty(categoryId)) {
            transaction.put("category", categoryId);
        }
        if (StringUtils.isNotEmpty(notes)) {
            transaction.put("notes", notes);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("transactions", List.of(transaction));

        return post("/v1/budgets/" + syncId + "/accounts/" + accountId + "/transactions/import", body);
    }

    public List<Map<String, Object>> extractAmounts(long emailId) {
        Message message = messageService.get(emailId);
        if (message == null) {
            return List.of();
        }

        String html = null;
        String plain = null;
        for (BodyPart bodyPart : message.getBodyParts()) {
            if (StringUtils.containsIgnoreCase(bodyPart.getContentType(), ContentTypeEnum.TEXT_HTML.getImapContentType())) {
                html = bodyPart.getContentAsString();
            } else if (StringUtils.containsIgnoreCase(bodyPart.getContentType(), ContentTypeEnum.TEXT_PLAIN.getImapContentType())) {
                plain = bodyPart.getContentAsString();
            }
        }

        String text;
        if (StringUtils.isNotEmpty(html)) {
            text = Jsoup.parse(html).text();
        } else if (StringUtils.isNotEmpty(plain)) {
            text = plain;
        } else {
            return List.of();
        }

        return findDollarAmounts(text);
    }

    List<Map<String, Object>> findDollarAmounts(String text) {
        Pattern pattern = Pattern.compile("(?:\\$|USD\\s?)(\\d{1,3}(?:,\\d{3})*\\.\\d{2})");
        Matcher matcher = pattern.matcher(text);

        Set<String> seen = new LinkedHashSet<>();
        List<Map<String, Object>> amounts = new ArrayList<>();

        while (matcher.find()) {
            String formatted = matcher.group(0).trim();
            if (seen.add(formatted)) {
                String numStr = matcher.group(1).replace(",", "");
                int cents = (int) Math.round(Double.parseDouble(numStr) * 100);
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("amount", cents);
                entry.put("formatted", formatted);
                amounts.add(entry);
            }
        }

        return amounts;
    }

    private JsonNode get(String path) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .GET();
        addAuthHeaders(builder);
        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("Actual Budget API error: " + response.statusCode() + " " + response.body());
        }
        return objectMapper.readTree(response.body());
    }

    private JsonNode post(String path, Map<String, Object> body) throws Exception {
        String json = objectMapper.writeValueAsString(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        addAuthHeaders(builder);
        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("Actual Budget API error: " + response.statusCode() + " " + response.body());
        }
        return objectMapper.readTree(response.body());
    }

    private void addAuthHeaders(HttpRequest.Builder builder) {
        if (StringUtils.isNotEmpty(apiKey)) {
            builder.header("x-api-key", apiKey);
        }
        if (StringUtils.isNotEmpty(encryptionPassword)) {
            builder.header("budget-encryption-password", encryptionPassword);
        }
    }

    private boolean nodeToBoolean(JsonNode node) {
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        return node.asInt() != 0;
    }
}
