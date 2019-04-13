package edu.bit.felinae;

import com.google.common.hash.Hashing;
import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import redis.clients.jedis.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.*;

import com.alibaba.fastjson.JSON;


public class WebService {

    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
        server.createContext("/ticket", WebService::handleGetTicket);
        server.createContext("/status", WebService::handleQueryStatus);
        server.start();
    }

    public static Map<String, List<String>> splitQuery(URL url) throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        final String[] pairs = url.getQuery().split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.get(key).add(value);
        }
        return query_pairs;
    }

    private static void handleGetTicket(HttpExchange exchange) throws IOException {
        System.out.println("Get ticket");
        SecureRandom rand = new SecureRandom();
        Jedis jedis = new Jedis("localhost");
        String hashingRes;
        do {
            byte[] randBytes = new byte[30];
            rand.nextBytes(randBytes);
            hashingRes = Hashing.sha256().hashBytes(randBytes).toString();
        } while(jedis.get(hashingRes) != null);
        Session session = new Session();
        String session_json = JSON.toJSONString(session);
        jedis.set(hashingRes, session_json);
        exchange.sendResponseHeaders(200, hashingRes.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(hashingRes.getBytes());
        os.close();
    }

    private static void handleQueryStatus(HttpExchange exchange) throws IOException {
        System.out.println("query");
        URL queryURL = exchange.getRequestURI().toURL();
        String res = queryURL.toString();
        exchange.sendResponseHeaders(200, res.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(res.getBytes());
        os.close();

//        Map<String, List<String>> queryMap = splitQuery(queryURL);
//        String sessionId = queryMap.get("session").get(0);
//        System.out.println(sessionId);
//        Jedis jedis = new Jedis("localhost");
//        String sessionJson = jedis.get(sessionId);
//        System.out.println(sessionJson);
//        Session session = JSON.parseObject(sessionJson, Session.class);
//        String res = session.getStatus().toString();
//        exchange.sendResponseHeaders(200, res.getBytes().length);
//        OutputStream os = exchange.getResponseBody();
//        os.write(res.getBytes());
//        os.close();
    }
}
