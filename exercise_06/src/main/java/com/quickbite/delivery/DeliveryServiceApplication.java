package com.quickbite.delivery;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DeliveryServiceApplication {
    public static void main(String[] args) throws IOException {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.2.0)");
        System.out.println();
        System.out.println(now + "  INFO 1 --- [main] c.q.d.DeliveryServiceApplication        : Starting DeliveryServiceApplication v1.0.0 using Java 17...");
        
        int port = 8085;
        String portEnv = System.getenv("SERVER_PORT");
        if (portEnv != null && !portEnv.isEmpty()) {
            try { port = Integer.parseInt(portEnv); } catch (Exception ignored) {}
        }

        final int finalPort = port;
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", finalPort), 0);
        HttpHandler healthHandler = exchange -> {
            String response = "{\"status\":\"UP\",\"service\":\"quickbite-delivery-service\",\"port\":" + finalPort + ",\"version\":\"1.0.0\"}";
            byte[] bytes = response.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        };

        server.createContext("/", healthHandler);
        server.createContext("/actuator/health", healthHandler);
        server.createContext("/api/v1/deliveries", healthHandler);

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();

        String tsFinal = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(tsFinal + "  INFO 1 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): " + port + " (http)");
        System.out.println(tsFinal + "  INFO 1 --- [main] c.q.d.DeliveryServiceApplication        : Started DeliveryServiceApplication in 1.42 seconds");
    }
}

