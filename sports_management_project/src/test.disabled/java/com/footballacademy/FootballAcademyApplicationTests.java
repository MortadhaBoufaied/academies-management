package com.footballacademy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class FootballAcademyApplicationTests {
    @Test void contextLoads() {
        // This test verifies that the Spring application context loads successfully         assertNotNull(this);
    }
    @Test void applicationPropertiesAreLoaded() {
        // Verify that test properties are loaded
        String port = System.getProperty("server.port");
        assertNotNull(port);
    }
}
