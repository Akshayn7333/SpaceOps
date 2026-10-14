package com.spaceops;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TelemetryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthReturnsUp() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void telemetryReturnsExplorer01() throws Exception {
        mockMvc.perform(get("/api/telemetry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spacecraft").value("Explorer-01"))
                .andExpect(jsonPath("$.battery").exists())
                .andExpect(jsonPath("$.temperature").exists())
                .andExpect(jsonPath("$.fuel").exists())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void statusIsWarningWhenBatteryLow() {
        org.junit.jupiter.api.Assertions.assertEquals("WARNING", TelemetryController.statusOf(10, 25.0, 80));
        org.junit.jupiter.api.Assertions.assertEquals("NORMAL", TelemetryController.statusOf(80, 25.0, 80));
    }
}
