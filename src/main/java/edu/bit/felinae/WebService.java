package edu.bit.felinae;

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class WebService {

    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
        HttpContext context = server.createContext("/ticket", WebService::handleGetTicket);
        server.start();
    }

    private static void handleGetTicket(HttpExchange exchange) throws IOException {
        System.out.println("Get ticket");
        String res = "hello world";
        exchange.sendResponseHeaders(200, res.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(res.getBytes());
        os.close();
    }
}
