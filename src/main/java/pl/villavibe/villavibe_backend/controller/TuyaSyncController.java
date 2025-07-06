package pl.villavibe.villavibe_backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.villavibe.villavibe_backend.service.TuyaService;

@RestController
@RequestMapping("/api/tuya")
@RequiredArgsConstructor
public class TuyaSyncController {

    private final TuyaService tuyaService;


}
