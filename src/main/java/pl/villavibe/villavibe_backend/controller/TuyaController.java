// TuyaController.java
package pl.villavibe.villavibe_backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import pl.villavibe.villavibe_backend.service.TuyaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tuya")
@RequiredArgsConstructor
public class TuyaController {

    private final TuyaService tuyaService;

    @PostMapping("/stream")
    public ResponseEntity<?> getStreamUrl(@RequestParam String deviceId,
                                          @RequestParam Long businessId) {
        try {
            JsonNode streamData = tuyaService.resolveStreamUrlFallback(deviceId, businessId);

            if (streamData != null && streamData.has("result") && streamData.get("result").has("url")) {
                String streamUrl = streamData.get("result").get("url").asText();
                String streamType = streamData.get("result").has("type")
                        ? streamData.get("result").get("type").asText()
                        : "unknown";

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("stream_type", streamType);
                response.put("stream_url", streamUrl);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Nie udało się uzyskać URL streamu",
                        "debug", streamData
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Błąd podczas pobierania streamu: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/proxy/stream")
    public void proxyStream(@RequestParam String url, HttpServletResponse response) {
        try {
            String tuyaAccessToken = tuyaService.getAccessToken();

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Authorization", "Bearer " + tuyaAccessToken);

            int status = conn.getResponseCode();
            if (status >= 400) {
                System.err.println("❌ Tuya proxy error: HTTP " + status);
                response.setStatus(status);
                return;
            }

            response.setContentType(conn.getContentType());
            try (InputStream in = conn.getInputStream()) {
                StreamUtils.copy(in, response.getOutputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
