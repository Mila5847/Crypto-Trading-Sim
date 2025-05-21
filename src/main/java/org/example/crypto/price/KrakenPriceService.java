package org.example.crypto.price;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KrakenPriceService {

    private String[] pairs = new String[0];

    private final WebSocketClient client;
    private final Map<String, BigDecimal> prices = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${kraken.ws.url:wss://ws.kraken.com/v2}")
    private String krakenWsUrl;

    public KrakenPriceService(WebSocketClient client) {
        this.client = client;
    }

    @PostConstruct
    public void init() {
        try {
            pairs = fetchTop20Pairs();
            if (pairs.length == 0) {
                System.err.println("[ERROR] No valid pairs found. KrakenPriceService will not connect.");
                return;
            }

            client.doHandshake(new AbstractWebSocketHandler() {
                @Override
                public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                    try {
                        subscribe(session);
                        System.out.println("[INFO] Subscribed to Kraken ticker for: " + Arrays.toString(pairs));
                    } catch (Exception e) {
                        System.err.println("[ERROR] Failed to subscribe to Kraken ticker: " + e.getMessage());
                    }
                }

                @Override
                protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                    parseMessage(message.getPayload());
                }

                @Override
                public void handleTransportError(WebSocketSession session, Throwable exception) {
                    System.err.println("[ERROR] WebSocket transport error: " + exception.getMessage());
                }
            }, String.valueOf(URI.create(krakenWsUrl)));

        } catch (Exception e) {
            System.err.println("[ERROR] Kraken WebSocket init failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Send a _subscribe_ request to the ticker channel for all 20 pairs. */
    private void subscribe(WebSocketSession session) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", "ticker");
        params.put("symbol", pairs);

        Map<String, Object> payload = new HashMap<>();
        payload.put("method", "subscribe");
        payload.put("params", params);

        session.sendMessage(new TextMessage(mapper.writeValueAsString(payload)));
    }

    private String[] fetchTop20Pairs() {
        try {
            RestTemplate rest = new RestTemplate();

            String cgUrl = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=20&page=1";
            JsonNode cgRoot = mapper.readTree(rest.getForObject(cgUrl, String.class));

            Set<String> topSymbols = new HashSet<>();
            for (JsonNode coin : cgRoot) {
                String symbol = coin.path("symbol").asText().toUpperCase();
                if (symbol.equals("BTC")) symbol = "XBT";
                if (symbol.equals("IOTA")) symbol = "MIOTA";
                topSymbols.add(symbol);
            }

            String krakenUrl = "https://api.kraken.com/0/public/AssetPairs";
            JsonNode root = mapper.readTree(rest.getForObject(krakenUrl, String.class));

            List<String> result = new ArrayList<>();
            JsonNode pairsNode = root.path("result");
            Iterator<Map.Entry<String, JsonNode>> fields = pairsNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode pair = entry.getValue();
                if (!pair.has("wsname")) continue;

                String wsname = pair.get("wsname").asText();
                String[] parts = wsname.split("/");
                if (parts.length == 2) {
                    String base = parts[0];
                    String quote = parts[1];
                    if (quote.equals("USD") && topSymbols.contains(base)) {
                        result.add(wsname);
                    }
                }
            }

            if (result.isEmpty()) {
                System.err.println("[WARN] No matching Kraken pairs found for top 20 coins.");
            }

            return result.toArray(new String[0]);

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to fetch top 20 pairs: " + e.getMessage());
            e.printStackTrace();
            return new String[0];
        }
    }

    private void parseMessage(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            if (!root.has("channel") || !"ticker".equals(root.get("channel").asText())) return;

            JsonNode dataArr = root.path("data");
            if (!dataArr.isArray() || dataArr.isEmpty()) return;

            JsonNode ticker = dataArr.get(0);
            String symbol = ticker.get("symbol").asText();
            JsonNode lastNode = ticker.get("last");

            if (lastNode != null && lastNode.isNumber()) {
                prices.put(symbol, lastNode.decimalValue());
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to parse Kraken ticker message: " + e.getMessage());
        }
    }

    /** Latest price for all subscribed pairs. */
    public Map<String, BigDecimal> getCurrentPrices() {
        return new HashMap<>(prices);
    }

    /** Returns null until first snapshot/update has been received for the pair. */
    public BigDecimal getPrice(String symbol) {
        return prices.get(symbol);
    }
}