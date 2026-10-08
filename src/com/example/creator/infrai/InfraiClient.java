package com.example.creator.infrai;

import com.example.creator.config.ServiceConfig;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public final class InfraiClient {
    // Capability idiom: infrai.errors.capture
    private final ServiceConfig config;
    private final HttpClient http;
    public InfraiClient(ServiceConfig config) { this.config = config; this.http = HttpClient.newHttpClient(); }

    public String capture(String title, String message, String fingerprint, String exception) {
        String body = "{\"title\":\"" + esc(title) + "\",\"message\":\"" + esc(message) +
                "\",\"level\":\"error\",\"fingerprint\":[\"" + esc(fingerprint) +
                "\"],\"exception\":\"" + esc(exception) + "\"}";
        return call("POST", "/v1/errors/capture", body, fingerprint);
    }

    private String call(String method, String path, String body, String requestId) {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(config.baseUrl() + path))
                        .timeout(Duration.ofSeconds(15)).header("Authorization", "Bearer " + config.apiKey())
                        .header("Idempotency-Key", requestId);
                b.method(method, HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
                HttpResponse<String> response = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
                String json = response.body();
                if (!json.contains("\"ok\":true")) throw new InfraiException(extract(json, "code"), extract(json, "message"));
                return json;
            } catch (InfraiException e) { throw e; }
            catch (Exception e) {
                if (attempt == 2) throw new RuntimeException(e);
                try { Thread.sleep(100L * (1L << attempt)); } catch (InterruptedException x) { Thread.currentThread().interrupt(); throw new RuntimeException(x); }
            }
        }
        throw new IllegalStateException();
    }
    private static String extract(String json, String key) { String marker = "\"" + key + "\":\""; int i = json.indexOf(marker); if (i < 0) return "unknown"; int s = i + marker.length(), e = json.indexOf('"', s); return e < 0 ? "unknown" : json.substring(s, e); }
    private static String esc(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"); }
}
