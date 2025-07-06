// TuyaService.java
package pl.villavibe.villavibe_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;
import pl.villavibe.villavibe_backend.config.TuyaConfig;
import pl.villavibe.villavibe_backend.model.Business;
import pl.villavibe.villavibe_backend.model.Device;
import pl.villavibe.villavibe_backend.model.enums.DeviceCategory;
import pl.villavibe.villavibe_backend.repository.BusinessRepository;
import pl.villavibe.villavibe_backend.repository.DeviceRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TuyaService {

    private final TuyaConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient http = new OkHttpClient();
    private final DeviceRepository deviceRepository;
    private final BusinessRepository businessRepository;
    private String tuyaAccessToken;

    private String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA256 error", e);
        }
    }

    private String sign(String clientId, String accessToken, String t, String nonce, String stringToSign, String secret) {
        String baseString = clientId + accessToken + t + nonce + stringToSign;
        return hmacSha256(baseString, secret).toUpperCase();
    }

    private String hmacSha256(String data, String key) {
        try {
            javax.crypto.Mac sha256_HMAC = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secret_key = new javax.crypto.spec.SecretKeySpec(key.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return bytesToHex(sha256_HMAC.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("HMAC error", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private String createStringToSign(String method, String bodyHash, String urlPath) {
        return method + "\n" + bodyHash + "\n" + "\n" + urlPath;
    }

    public String getToken() {
        try {
            String t = String.valueOf(System.currentTimeMillis());
            String nonce = generateUUID();
            String path = "/v1.0/token?grant_type=1";
            String signStr = createStringToSign("GET", sha256(""), path);
            String sign = sign(config.getClientId(), "", t, nonce, signStr, config.getSecret());

            Request request = new Request.Builder()
                    .url("https://openapi.tuyaeu.com" + path)
                    .get()
                    .addHeader("client_id", config.getClientId())
                    .addHeader("sign", sign)
                    .addHeader("t", t)
                    .addHeader("sign_method", "HMAC-SHA256")
                    .addHeader("nonce", nonce)
                    .build();

            Response response = http.newCall(request).execute();
            JsonNode res = objectMapper.readTree(Objects.requireNonNull(response.body()).string());
            this.tuyaAccessToken = res.get("result").get("access_token").asText();
            return this.tuyaAccessToken;

        } catch (Exception e) {
            throw new RuntimeException("\u274C Failed to get Tuya token", e);
        }
    }

    public String getAccessToken() {
        return this.tuyaAccessToken;
    }

    public JsonNode getStreamUrl(String accessToken, String deviceId, String uid, String type) {
        try {
            String t = String.valueOf(System.currentTimeMillis());
            String nonce = generateUUID();
            String path = "/v1.0/users/" + uid + "/devices/" + deviceId + "/stream/actions/allocate";
            String body = "{\"type\":\"" + type + "\"}";
            String bodyHash = sha256(body);
            String signStr = createStringToSign("POST", bodyHash, path);
            String sign = sign(config.getClientId(), accessToken, t, nonce, signStr, config.getSecret());

            Request request = new Request.Builder()
                    .url("https://openapi.tuyaeu.com" + path)
                    .post(RequestBody.create(body, MediaType.parse("application/json")))
                    .addHeader("client_id", config.getClientId())
                    .addHeader("access_token", accessToken)
                    .addHeader("sign", sign)
                    .addHeader("t", t)
                    .addHeader("sign_method", "HMAC-SHA256")
                    .addHeader("nonce", nonce)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = http.newCall(request).execute();
            return objectMapper.readTree(Objects.requireNonNull(response.body()).string());

        } catch (Exception e) {
            throw new RuntimeException("\u274C Failed to get Tuya stream URL", e);
        }
    }

    public JsonNode resolveStreamUrlFallback(String deviceId, Long businessId) {
        try {
            Device device = deviceRepository.findByDeviceIdAndBusinessId(deviceId, businessId)
                    .orElseThrow(() -> new RuntimeException("Urządzenie nie istnieje w tej działalności"));

            String uid = config.getUid();
            String accessToken = getToken();

            JsonNode hls = getStreamUrl(accessToken, device.getDeviceId(), uid, "hls");
            if (hls != null && hls.has("success") && hls.get("success").asBoolean()
                    && hls.get("result").has("url")) {
                ((ObjectNode) hls.get("result")).put("type", "hls");
                return hls;
            }

            JsonNode rtsp = getStreamUrl(accessToken, device.getDeviceId(), uid, "rtsp");
            if (rtsp != null && rtsp.has("success") && rtsp.get("success").asBoolean()
                    && rtsp.get("result").has("url")) {
                ((ObjectNode) rtsp.get("result")).put("type", "rtsp");
                return rtsp;
            }

            return rtsp;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Błąd podczas uzyskiwania streamu: " + e.getMessage(), e);
        }
    }

    private DeviceCategory resolveCategory(String category, String iconUrl) {
        if (category.contains("dj") || iconUrl.contains("light")) return DeviceCategory.LIGHT;
        if (category.contains("cz") || iconUrl.contains("camera")) return DeviceCategory.CAMERA;
        if (category.contains("socket") || iconUrl.contains("plug")) return DeviceCategory.SOCKET;
        return DeviceCategory.OTHER;
    }
}
