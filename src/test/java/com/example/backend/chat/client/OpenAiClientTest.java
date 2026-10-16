package com.example.backend.chat.client;

import org.junit.jupiter.api.Test;
import java.net.http.*;
import java.util.*;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class OpenAiClientTest {
    @Test
    void extractsMessageAfterReasoningAndDisablesRemoteStorage() throws Exception {
        HttpClient http = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(http.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("""
            {"status":"completed","output":[{"type":"reasoning"},{"type":"message","content":[{"type":"output_text","text":"Your recorded spending was 10 USD."}]}]}
            """);
        OpenAiClient client = new OpenAiClient("test-key", "test-model", http);
        assertEquals("Your recorded spending was 10 USD.", client.reply(List.of(Map.of("role", "user", "content", "Hi"))));
        var captor = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(http).send(captor.capture(), any());
        assertEquals("https://api.openai.com/v1/responses", captor.getValue().uri().toString());
        var bytes = new java.io.ByteArrayOutputStream();
        captor.getValue().bodyPublisher().orElseThrow().subscribe(new java.util.concurrent.Flow.Subscriber<java.nio.ByteBuffer>() {
            public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) { subscription.request(Long.MAX_VALUE); }
            public void onNext(java.nio.ByteBuffer buffer) { byte[] data = new byte[buffer.remaining()]; buffer.get(data); bytes.writeBytes(data); }
            public void onError(Throwable error) { throw new AssertionError(error); }
            public void onComplete() { }
        });
        var body = com.google.gson.JsonParser.parseString(bytes.toString(java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
        assertFalse(body.get("store").getAsBoolean());
        assertEquals("test-model", body.get("model").getAsString());
        assertFalse(body.has("tools"));
    }

    @Test
    void missingKeyMakesNoNetworkCall() {
        HttpClient http = mock(HttpClient.class);
        var error = assertThrows(ResponseStatusException.class, () -> new OpenAiClient("", "test", http).reply(List.of()));
        assertEquals(503, error.getStatusCode().value());
        verifyNoInteractions(http);
    }

    @Test
    void handlesQuotaAndIncompleteResponsesWithoutExposingProviderBody() throws Exception {
        HttpClient http = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(http.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        when(response.statusCode()).thenReturn(429);
        OpenAiClient client = new OpenAiClient("secret", "test", http);
        assertEquals(503, assertThrows(ResponseStatusException.class, () -> client.reply(List.of())).getStatusCode().value());
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"status\":\"incomplete\",\"output\":[]}");
        assertEquals(502, assertThrows(ResponseStatusException.class, () -> client.reply(List.of())).getStatusCode().value());
    }
    @Test
    void quotaErrorExplainsBillingWithoutExposingRawResponse() throws Exception {
        HttpClient http = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(http.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        when(response.statusCode()).thenReturn(429);
        when(response.body()).thenReturn("{\"error\":{\"code\":\"insufficient_quota\",\"message\":\"private-details\"}}");
        var error = assertThrows(ResponseStatusException.class,
            () -> new OpenAiClient("secret", "test", http).reply(List.of()));
        assertTrue(error.getReason().contains("billing"));
        assertFalse(error.getReason().contains("private-details"));
    }

}
