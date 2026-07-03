package com.patricia.board;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class BoardBackendApplicationTests {

    @Test
    void contextLoads() {
        // Simple assertion to verify the application context loads successfully
    }

    @Test
    void mainStartsApplication() {
        assertDoesNotThrow(() -> {
            BoardBackendApplication.main(new String[]{"--spring.main.web-application-type=none"});
        });
    }
}
