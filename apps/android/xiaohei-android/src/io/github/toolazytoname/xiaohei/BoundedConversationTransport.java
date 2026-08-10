package io.github.toolazytoname.xiaohei;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/** Pure-Java bounded HTTP/SSE transport. It has no Android or tool authority. */
final class BoundedConversationTransport {
    enum Code {
        OK,
        CANCELLED,
        ENDPOINT_REJECTED,
        CONFIG_REJECTED,
        REDIRECT_REJECTED,
        RATE_LIMITED,
        TIMEOUT,
        HTTP_ERROR,
        RESPONSE_TOO_LARGE,
        STREAM_TRUNCATED,
        EMPTY_RESPONSE,
        PARSE_ERROR,
        NETWORK_ERROR
    }

    interface Decoder {
        String decodeSseData(String data) throws Exception;
        String decodeJsonBody(String body) throws Exception;
    }

    interface Callback { void onResult(Result result); }

    static final class Config {
        final String endpoint;
        final String token;
        final String requestBody;
        final int connectTimeoutMs;
        final int readTimeoutMs;
        final int maxResponseBytes;

        Config(String endpoint, String token, String requestBody, int connectTimeoutMs,
               int readTimeoutMs, int maxResponseBytes) {
            this.endpoint = endpoint == null ? "" : endpoint.trim();
            this.token = token == null ? "" : token;
            this.requestBody = requestBody == null ? "" : requestBody;
            this.connectTimeoutMs = connectTimeoutMs;
            this.readTimeoutMs = readTimeoutMs;
            this.maxResponseBytes = maxResponseBytes;
        }
    }

    static final class Result {
        final Code code;
        final int httpStatus;
        final String text;

        Result(Code code, int httpStatus, String text) {
            this.code = code;
            this.httpStatus = httpStatus;
            this.text = text == null ? "" : text;
        }
    }

    static final class Request {
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private final AtomicBoolean completed = new AtomicBoolean();
        private volatile HttpURLConnection connection;

        void cancel() {
            if (completed.get()) return;
            cancelled.set(true);
            HttpURLConnection current = connection;
            if (current != null) current.disconnect();
        }

        boolean isCancelled() { return cancelled.get(); }

        private void bind(HttpURLConnection next) {
            connection = next;
            if (cancelled.get()) next.disconnect();
        }

        private void clear(HttpURLConnection expected) {
            if (connection == expected) connection = null;
        }

        private void complete(Callback callback, Result result) {
            if (completed.compareAndSet(false, true)) callback.onResult(result);
        }
    }

    private static final class ResponseTooLargeException extends IOException {}
    private static final int MAX_REQUEST_BYTES = 65536;
    private static final int MAX_CONNECT_TIMEOUT_MS = 30000;
    private static final int MAX_READ_TIMEOUT_MS = 60000;
    private static final int MAX_RESPONSE_LIMIT_BYTES = 1048576;

    private static final class LimitedInputStream extends FilterInputStream {
        private final int maximum;
        private int count;

        LimitedInputStream(InputStream input, int maximum) {
            super(input);
            this.maximum = maximum;
        }

        private void add(int amount) throws ResponseTooLargeException {
            if (amount <= 0) return;
            count += amount;
            if (count > maximum) throw new ResponseTooLargeException();
        }

        @Override public int read() throws IOException {
            int value = super.read();
            if (value >= 0) add(1);
            return value;
        }

        @Override public int read(byte[] buffer, int offset, int length) throws IOException {
            int amount = super.read(buffer, offset, length);
            add(amount);
            return amount;
        }
    }

    private BoundedConversationTransport() {}

    static void execute(Config config, Decoder decoder, Request request, Callback callback) {
        HttpURLConnection connection = null;
        Result result;
        try {
            if (request.isCancelled()) {
                result = result(Code.CANCELLED);
            } else {
                URL url = endpointUrl(config.endpoint);
                if (url == null) {
                    result = result(Code.ENDPOINT_REJECTED);
                } else if (!validConfig(config)) {
                    result = result(Code.CONFIG_REJECTED);
                } else {
                    connection = (HttpURLConnection) (isLoopback(url.getHost())
                            ? url.openConnection(Proxy.NO_PROXY)
                            : url.openConnection());
                    request.bind(connection);
                    result = exchange(connection, config, decoder, request);
                }
            }
        } catch (SocketTimeoutException error) {
            result = request.isCancelled() ? result(Code.CANCELLED) : result(Code.TIMEOUT);
        } catch (ResponseTooLargeException error) {
            result = request.isCancelled() ? result(Code.CANCELLED) : result(Code.RESPONSE_TOO_LARGE);
        } catch (IllegalArgumentException error) {
            result = request.isCancelled() ? result(Code.CANCELLED) : result(Code.ENDPOINT_REJECTED);
        } catch (Exception error) {
            result = request.isCancelled() ? result(Code.CANCELLED) : result(Code.NETWORK_ERROR);
        } finally {
            if (connection != null) {
                request.clear(connection);
                connection.disconnect();
            }
        }
        request.complete(callback, result);
    }

    private static Result exchange(HttpURLConnection connection, Config config, Decoder decoder,
                                   Request request) throws Exception {
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(config.connectTimeoutMs);
        connection.setReadTimeout(config.readTimeoutMs);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("Accept", "text/event-stream, application/json");
        connection.setRequestProperty("Accept-Encoding", "identity");
        if (!config.token.isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + config.token);
        }
        connection.setDoOutput(true);
        byte[] body = config.requestBody.getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(body.length);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(body);
        }

        if (request.isCancelled()) return result(Code.CANCELLED);
        int status = connection.getResponseCode();
        if (status >= 300 && status < 400) return new Result(Code.REDIRECT_REJECTED, status, "");
        if (status == 429) return new Result(Code.RATE_LIMITED, status, "");
        if (status < 200 || status >= 300) return new Result(Code.HTTP_ERROR, status, "");

        String contentType = connection.getContentType();
        try (InputStream limited = new LimitedInputStream(connection.getInputStream(), config.maxResponseBytes)) {
            if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("text/event-stream")) {
                return readSse(limited, decoder, request);
            }
            String decoded = decoder.decodeJsonBody(readUtf8(limited));
            return successOrEmpty(decoded);
        } catch (ResponseTooLargeException error) {
            throw error;
        } catch (SocketTimeoutException error) {
            throw error;
        } catch (Exception error) {
            if (request.isCancelled()) return result(Code.CANCELLED);
            return result(Code.PARSE_ERROR);
        }
    }

    private static Result readSse(InputStream input, Decoder decoder, Request request) throws Exception {
        StringBuilder answer = new StringBuilder();
        StringBuilder eventData = new StringBuilder();
        boolean done = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (request.isCancelled()) return result(Code.CANCELLED);
            if (line.isEmpty()) {
                if (eventData.length() > 0) {
                    appendDecoded(answer, decoder.decodeSseData(eventData.toString()));
                    eventData.setLength(0);
                }
                continue;
            }
            if (line.startsWith(":")) continue;
            if (!line.startsWith("data:")) continue;
            String data = line.substring(5);
            if (data.startsWith(" ")) data = data.substring(1);
            if ("[DONE]".equals(data)) {
                done = true;
                break;
            }
            if (eventData.length() > 0) eventData.append('\n');
            eventData.append(data);
        }
        if (request.isCancelled()) return result(Code.CANCELLED);
        if (!done) return result(Code.STREAM_TRUNCATED);
        return successOrEmpty(answer.toString());
    }

    private static void appendDecoded(StringBuilder answer, String decoded) {
        if (decoded != null && !decoded.isEmpty()) answer.append(decoded);
    }

    private static String readUtf8(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int amount;
        while ((amount = input.read(buffer)) != -1) output.write(buffer, 0, amount);
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    static URL endpointUrl(String endpoint) {
        try {
            URL base = new URL(endpoint);
            String protocol = base.getProtocol();
            String host = base.getHost();
            boolean loopback = isLoopback(host);
            if (!("https".equalsIgnoreCase(protocol) || ("http".equalsIgnoreCase(protocol) && loopback))) {
                return null;
            }
            if (base.getUserInfo() != null || base.getQuery() != null || base.getRef() != null) return null;
            return new URL(endpoint.replaceAll("/+$", "") + "/chat/completions");
        } catch (Exception error) {
            return null;
        }
    }

    private static boolean isLoopback(String host) {
        return "127.0.0.1".equals(host) || "::1".equals(host) || "[::1]".equals(host) ||
                "localhost".equalsIgnoreCase(host);
    }

    private static boolean validConfig(Config config) {
        return !config.requestBody.isEmpty() &&
                config.requestBody.getBytes(StandardCharsets.UTF_8).length <= MAX_REQUEST_BYTES &&
                config.token.indexOf('\r') < 0 && config.token.indexOf('\n') < 0 &&
                config.connectTimeoutMs >= 1 && config.connectTimeoutMs <= MAX_CONNECT_TIMEOUT_MS &&
                config.readTimeoutMs >= 1 && config.readTimeoutMs <= MAX_READ_TIMEOUT_MS &&
                config.maxResponseBytes >= 1 && config.maxResponseBytes <= MAX_RESPONSE_LIMIT_BYTES;
    }

    private static Result successOrEmpty(String text) {
        String normalized = text == null ? "" : text.trim();
        return normalized.isEmpty() ? result(Code.EMPTY_RESPONSE) : new Result(Code.OK, 200, normalized);
    }

    private static Result result(Code code) { return new Result(code, 0, ""); }
}
