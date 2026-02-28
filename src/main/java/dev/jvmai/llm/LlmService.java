package dev.jvmai.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LlmService {

    private final String model;
    private final String ollamaUrl;
    private final HttpClient client;
    private final ObjectMapper mapper;
    
    // We maintain a history array for interactive mode
    private final ArrayNode chatHistory;

    public LlmService(String model, int timeoutSeconds) {
        this.model = model;
        this.ollamaUrl = "http://localhost:11434/api/chat";
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        this.mapper = new ObjectMapper();
        this.chatHistory = mapper.createArrayNode();
    }

    public String analyze(String jsonDiagnosticsContext) throws Exception {
        String systemPrompt = "You are an expert JVM performance engineer. Analyze the following JVM diagnostic data. " +
                "Provide a short root cause analysis, confidence level, severity, and recommended actions. " +
                "Output ONLY valid JSON.";

        addMessage("system", systemPrompt);
        addMessage("user", "Here is the diagnostic data: " + jsonDiagnosticsContext);

        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("stream", false);
        requestBody.put("format", "json");
        requestBody.set("messages", chatHistory);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ollamaUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("LLM API returned status " + response.statusCode() + ": " + response.body());
        }

        JsonNode responseNode = mapper.readTree(response.body());
        String content = responseNode.path("message").path("content").asText();
        
        // Save the assistant's reply
        addMessage("assistant", content);
        
        return content;
    }
    
    public String askQuestion(String question) throws Exception {
        addMessage("user", question);
        
        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("stream", false);
        requestBody.set("messages", chatHistory);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ollamaUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("LLM API returned status " + response.statusCode() + ": " + response.body());
        }

        JsonNode responseNode = mapper.readTree(response.body());
        String content = responseNode.path("message").path("content").asText();
        addMessage("assistant", content);
        
        return content;
    }
    
    private void addMessage(String role, String content) {
        ObjectNode msg = mapper.createObjectNode();
        msg.put("role", role);
        msg.put("content", content);
        chatHistory.add(msg);
    }
}
