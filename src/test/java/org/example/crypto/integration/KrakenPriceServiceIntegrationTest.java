package org.example.crypto.integration;

import org.example.crypto.price.KrakenPriceService;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

class KrakenPriceServiceIntegrationTest {

    @Test
    void testFetchTop20PairsWorks() {
        RestTemplate restTemplate = new RestTemplate();
        KrakenPriceService service = new KrakenPriceService(null);

        // force fetch without init() or websocket
        String[] pairs = service.fetchTop20Pairs();

        assertNotNull(pairs);
        assertTrue(pairs.length > 0, "Expected non-empty array of trading pairs");
    }
}
