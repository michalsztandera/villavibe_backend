package pl.villavibe.villavibe_backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TuyaConfig {
    private final Dotenv dotenv = Dotenv.load();

    public String getClientId() {
        return dotenv.get("TUYA_CLIENT_ID");
    }

    public String getSecret() {
        return dotenv.get("TUYA_SECRET");
    }

    public String getUid() {
        return dotenv.get("TUYA_UID");
    }
}
