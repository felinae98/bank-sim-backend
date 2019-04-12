package edu.bit.felinae;

import com.google.common.hash.Hashing;
import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import redis.clients.jedis.*;
import java.security.SecureRandom;

public class WebService {

    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
        HttpContext context = server.createContext("/ticket", WebService::handleGetTicket);
        server.start();
    }

    private static void handleGetTicket(HttpExchange exchange) throws IOException {
        System.out.println("Get ticket");
        SecureRandom rand = new SecureRandom();
        byte randBytes[] = new byte[30];
        rand.nextBytes(randBytes);
        String hashingRes = Hashing.sha256().hashBytes(randBytes).toString();
        Jedis jedis = new Jedis("localhost");
        jedis.set(hashingRes, "yes!");

        exchange.sendResponseHeaders(200, hashingRes.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(hashingRes.getBytes());
        os.close();
    }
}
