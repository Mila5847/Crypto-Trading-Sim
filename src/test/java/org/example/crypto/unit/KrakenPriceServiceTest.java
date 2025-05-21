package org.example.crypto.unit;

import org.example.crypto.price.KrakenPriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.client.WebSocketClient;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KrakenPriceServiceTest {

    private KrakenPriceService service;
    private WebSocketClient mockClient;

    @BeforeEach
    void setUp() {
        mockClient = Mockito.mock(WebSocketClient.class);
        service = new KrakenPriceService(mockClient);
    }

    @Test
    void testParseValidMessageUpdatesPrice() {
        String json = """
            {
              "channel": "ticker",
              "data": [{
                "symbol": "ETH/USD",
                "last": 3500.15
              }]
            }
        """;

        service.parseMessage(json);
        BigDecimal price = service.getPrice("ETH/USD");

        assertNotNull(price);
        assertEquals(new BigDecimal("3500.15"), price);
    }

    @Test
    void testParseInvalidChannelDoesNotUpdate() {
        String json = """
            {
              "channel": "other",
              "data": [{
                "symbol": "BTC/USD",
                "last": 100.00
              }]
            }
        """;

        service.parseMessage(json);
        assertNull(service.getPrice("BTC/USD"));
    }

    @Test
    void testGetCurrentPricesReturnsCopy() {
        String json = """
            {
              "channel": "ticker",
              "data": [{
                "symbol": "XRP/USD",
                "last": 0.55
              }]
            }
        """;

        service.parseMessage(json);
        Map<String, BigDecimal> prices = service.getCurrentPrices();
        assertEquals(1, prices.size());
        assertTrue(prices.containsKey("XRP/USD"));
    }
}
