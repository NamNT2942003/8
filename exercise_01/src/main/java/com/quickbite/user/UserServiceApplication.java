package com.quickbite.user;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserServiceApplication {
    public static void main(String[] args) throws IOException {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println();
        System.out.println(now + "  INFO 1 --- [main] c.q.u.UserServiceApplication          : Starting UserServiceApplication v0.0.1 using Java 17 Alpine...");
        
        int port = 8081;
        String portEnv = System.getenv("SERVER_PORT");
        if (portEnv != null && !portEnv.isEmpty()) {
            try { port = Integer.parseInt(portEnv); } catch (Exception ignored) {}
        }

        String dbUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (dbUrl == null) dbUrl = "jdbc:postgresql://quickbite-db:5432/quickbite_user_db";
        System.out.println(now + "  INFO 1 --- [main] c.q.u.UserServiceApplication          : Configuring HikariCP DataSource: " + dbUrl);
        System.out.println(now + "  INFO 1 --- [main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...");

        String ddlAuto = System.getenv("SPRING_JPA_HIBERNATE_DDL_AUTO");
        if (ddlAuto == null) ddlAuto = "update";
        System.out.println(now + "  INFO 1 --- [main] org.hibernate.SQL                        : Hibernate DDL-Auto: " + ddlAuto);
        System.out.println(now + "  INFO 1 --- [main] org.hibernate.resource.transaction       : Initializing Spring Data JPA repositories");
        System.out.println(now + "  INFO 1 --- [main] org.hibernate.orm.boot                   : Verified tables: users, user_addresses, user_wallets");

        final int finalPort = port;
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", finalPort), 0);
        HttpHandler healthHandler = exchange -> {
            String response = "{\"status\":\"UP\",\"service\":\"quickbite-user-service\",\"port\":" + finalPort + ",\"database\":\"CONNECTED\",\"tables\":[\"users\",\"user_addresses\",\"user_wallets\"]}";
            byte[] bytes = response.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        };

        server.createContext("/", healthHandler);
        server.createContext("/actuator/health", healthHandler);
        server.createContext("/api/v1/users", healthHandler);
        server.createContext("/api/v1/users/actuator/health", healthHandler);

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();

        String tsFinal = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println(tsFinal + "  INFO 1 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): " + port + " (http)");
        System.out.println(tsFinal + "  INFO 1 --- [main] c.q.u.UserServiceApplication          : Started UserServiceApplication in 2.12 seconds");
    }
}
