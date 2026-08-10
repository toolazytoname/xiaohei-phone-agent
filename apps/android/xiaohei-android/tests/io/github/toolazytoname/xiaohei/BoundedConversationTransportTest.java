package io.github.toolazytoname.xiaohei;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class BoundedConversationTransportTest {
    private static final BoundedConversationTransport.Decoder DECODER =
            new BoundedConversationTransport.Decoder() {
                @Override public String decodeSseData(String data) {
                    return content(data);
                }

                @Override public String decodeJsonBody(String body) {
                    return content(body);
                }

                private String content(String json) {
                    String marker = "\"content\":\"";
                    int start = json.indexOf(marker);
                    if (start < 0) throw new IllegalArgumentException("content");
                    start += marker.length();
                    int end = json.indexOf('"', start);
                    if (end < 0) throw new IllegalArgumentException("content");
                    return json.substring(start, end);
                }
            };

    private static final class Server implements AutoCloseable {
        final HttpServer server;
        final ExecutorService executor = Executors.newCachedThreadPool();

        Server(HttpHandler handler) throws IOException {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/v1/chat/completions", handler);
            server.setExecutor(executor);
            server.start();
        }

        String endpoint() { return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1"; }

        @Override public void close() {
            server.stop(0);
            executor.shutdownNow();
        }
    }

    private static final class Completion {
        final BoundedConversationTransport.Request request = new BoundedConversationTransport.Request();
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicReference<BoundedConversationTransport.Result> result = new AtomicReference<>();
        final AtomicInteger calls = new AtomicInteger();
        Thread thread;

        void start(BoundedConversationTransport.Config config) {
            thread = new Thread(() -> BoundedConversationTransport.execute(
                    config,
                    DECODER,
                    request,
                    value -> {
                        result.set(value);
                        calls.incrementAndGet();
                        done.countDown();
                    }
            ), "bounded-conversation-test");
            thread.start();
        }

        BoundedConversationTransport.Result await() throws Exception {
            check(done.await(3, TimeUnit.SECONDS), "callback timeout");
            thread.join(1000);
            check(calls.get() == 1, "callback must be delivered exactly once");
            return result.get();
        }
    }

    public static void main(String[] args) throws Exception {
        streamSuccessAndRequestBoundary();
        jsonFallback();
        truncatedStreamRejected();
        rateLimitClassified();
        redirectRejectedWithoutFollow();
        readTimeoutClassified();
        cancellationDisconnectsBlockedRead();
        oversizedResponseRejected();
        insecureExternalEndpointRejected();
        headerInjectionRejectedBeforeNetwork();
        ipv6LoopbackAcceptedByPolicy();
        System.out.println("PASS BoundedConversationTransportTest cases=11 stream/json/truncated/429/redirect/timeout/cancel/limit/endpoint/config/ipv6");
    }

    private static void streamSuccessAndRequestBoundary() throws Exception {
        AtomicBoolean requestValid = new AtomicBoolean();
        try (Server server = new Server(exchange -> {
            String requestBody = readUtf8(exchange.getRequestBody());
            requestValid.set("POST".equals(exchange.getRequestMethod()) &&
                    "Bearer test-token".equals(exchange.getRequestHeaders().getFirst("Authorization")) &&
                    requestBody.contains("\"stream\":true"));
            send(exchange, 200, "text/event-stream; charset=utf-8",
                    "data: {\"choices\":[{\"delta\":{\"content\":\"hello \"}}]}\n\n" +
                    ": heartbeat\n\n" +
                    "data: {\"choices\":[{\"delta\":{\"content\":\"world\"}}]}\n\n" +
                    "data: [DONE]\n\n");
        })) {
            BoundedConversationTransport.Result result = execute(config(server.endpoint(), 1000, 4096));
            check(result.code == BoundedConversationTransport.Code.OK, "stream success code");
            check("hello world".equals(result.text), "stream content");
            check(requestValid.get(), "request method/token/stream body");
        }
    }

    private static void jsonFallback() throws Exception {
        try (Server server = new Server(exchange -> send(
                exchange,
                200,
                "application/json",
                "{\"choices\":[{\"message\":{\"content\":\"fallback\"}}]}"
        ))) {
            BoundedConversationTransport.Result result = execute(config(server.endpoint(), 1000, 4096));
            check(result.code == BoundedConversationTransport.Code.OK, "json fallback code");
            check("fallback".equals(result.text), "json fallback content");
        }
    }

    private static void truncatedStreamRejected() throws Exception {
        try (Server server = new Server(exchange -> send(
                exchange,
                200,
                "text/event-stream",
                "data: {\"choices\":[{\"delta\":{\"content\":\"partial\"}}]}\n\n"
        ))) {
            BoundedConversationTransport.Result result = execute(config(server.endpoint(), 1000, 4096));
            check(result.code == BoundedConversationTransport.Code.STREAM_TRUNCATED, "truncated stream");
            check(result.text.isEmpty(), "partial stream must not escape");
        }
    }

    private static void rateLimitClassified() throws Exception {
        try (Server server = new Server(exchange -> send(exchange, 429, "application/json", "{}"))) {
            BoundedConversationTransport.Result result = execute(config(server.endpoint(), 1000, 4096));
            check(result.code == BoundedConversationTransport.Code.RATE_LIMITED, "429 classification");
            check(result.httpStatus == 429, "429 status");
        }
    }

    private static void redirectRejectedWithoutFollow() throws Exception {
        AtomicInteger redirected = new AtomicInteger();
        HttpServer raw = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        ExecutorService executor = Executors.newCachedThreadPool();
        raw.setExecutor(executor);
        raw.createContext("/v1/chat/completions", exchange -> {
            exchange.getResponseHeaders().set("Location", "/redirected");
            send(exchange, 302, "text/plain", "redirect");
        });
        raw.createContext("/redirected", exchange -> {
            redirected.incrementAndGet();
            send(exchange, 200, "application/json", "{}");
        });
        raw.start();
        try {
            String endpoint = "http://127.0.0.1:" + raw.getAddress().getPort() + "/v1";
            BoundedConversationTransport.Result result = execute(config(endpoint, 1000, 4096));
            check(result.code == BoundedConversationTransport.Code.REDIRECT_REJECTED, "redirect code");
            check(redirected.get() == 0, "redirect must not be followed");
        } finally {
            raw.stop(0);
            executor.shutdownNow();
        }
    }

    private static void readTimeoutClassified() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        try (Server server = new Server(exchange -> {
            exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, 0);
            try {
                release.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        })) {
            BoundedConversationTransport.Result result = execute(config(server.endpoint(), 75, 4096));
            check(result.code == BoundedConversationTransport.Code.TIMEOUT, "read timeout");
        } finally {
            release.countDown();
        }
    }

    private static void cancellationDisconnectsBlockedRead() throws Exception {
        CountDownLatch responseStarted = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try (Server server = new Server(exchange -> {
            exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, 0);
            OutputStream output = exchange.getResponseBody();
            output.write(": waiting\n\n".getBytes(StandardCharsets.UTF_8));
            output.flush();
            responseStarted.countDown();
            try {
                release.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
            } finally {
                output.close();
                exchange.close();
            }
        })) {
            Completion completion = new Completion();
            completion.start(config(server.endpoint(), 2000, 4096));
            check(responseStarted.await(1, TimeUnit.SECONDS), "cancel response start");
            completion.request.cancel();
            BoundedConversationTransport.Result result = completion.await();
            check(result.code == BoundedConversationTransport.Code.CANCELLED, "cancel classification");
        } finally {
            release.countDown();
        }
    }

    private static void oversizedResponseRejected() throws Exception {
        StringBuilder body = new StringBuilder("{\"choices\":[{\"message\":{\"content\":\"");
        for (int index = 0; index < 256; index++) body.append('x');
        body.append("\"}}]}");
        try (Server server = new Server(exchange -> send(exchange, 200, "application/json", body.toString()))) {
            BoundedConversationTransport.Result result = execute(config(server.endpoint(), 1000, 64));
            check(result.code == BoundedConversationTransport.Code.RESPONSE_TOO_LARGE, "response limit");
        }
    }

    private static void insecureExternalEndpointRejected() throws Exception {
        BoundedConversationTransport.Result result = execute(config("http://example.com/v1", 1000, 4096));
        check(result.code == BoundedConversationTransport.Code.ENDPOINT_REJECTED, "external cleartext endpoint");
    }

    private static void headerInjectionRejectedBeforeNetwork() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        try (Server server = new Server(exchange -> {
            requests.incrementAndGet();
            send(exchange, 200, "application/json", "{}");
        })) {
            BoundedConversationTransport.Config config = new BoundedConversationTransport.Config(
                    server.endpoint(),
                    "token\r\nInjected: value",
                    "{\"stream\":true}",
                    1000,
                    1000,
                    4096
            );
            BoundedConversationTransport.Result result = execute(config);
            check(result.code == BoundedConversationTransport.Code.CONFIG_REJECTED, "header injection config");
            check(requests.get() == 0, "invalid config must not reach network");
        }
    }

    private static void ipv6LoopbackAcceptedByPolicy() {
        check(BoundedConversationTransport.endpointUrl("http://[::1]:8080/v1") != null,
                "IPv6 loopback endpoint policy");
    }

    private static BoundedConversationTransport.Config config(String endpoint, int readTimeout, int maxBytes) {
        return new BoundedConversationTransport.Config(
                endpoint,
                "test-token",
                "{\"stream\":true}",
                1000,
                readTimeout,
                maxBytes
        );
    }

    private static BoundedConversationTransport.Result execute(BoundedConversationTransport.Config config)
            throws Exception {
        Completion completion = new Completion();
        completion.start(config);
        return completion.await();
    }

    private static void send(HttpExchange exchange, int status, String contentType, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static String readUtf8(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int amount;
        while ((amount = input.read(buffer)) != -1) output.write(buffer, 0, amount);
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
