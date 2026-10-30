package de.dfki.asr.ajan.e2e;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;

public final class Http {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private Http() {}

    /**
     * Asserts the Content-Type as a parsed HTTP value: media type and charset parameter
     * compared case-insensitively, whitespace around ';' ignored (optional per RFC 9110).
     * Strict otherwise: the header must exist, the media type must match, a charset
     * parameter must be present and equal, and there must be no other parameter. The
     * service's embedded Tomcat 11 emits "type;charset=X" (no space); the pre-migration
     * Undertow stack emitted "type; charset=X" - both forms are the same HTTP value.
     */
    public static void assertContentType(HttpResponse<String> r, String expectedMediaType,
            String expectedCharset) {
        String raw = r.headers().firstValue("Content-Type").orElse(null);
        if (raw == null) {
            throw new AssertionError("response has no Content-Type, expected "
                    + expectedMediaType + "; charset=" + expectedCharset);
        }
        String[] parts = raw.split(";", -1);
        String mediaType = parts[0].trim();
        if (!mediaType.equalsIgnoreCase(expectedMediaType)) {
            throw new AssertionError("Content-Type media type: expected <" + expectedMediaType
                    + "> but was <" + mediaType + "> (raw: " + raw + ")");
        }
        if (parts.length != 2) {
            throw new AssertionError("Content-Type must carry exactly one parameter (charset="
                    + expectedCharset + "), raw: " + raw);
        }
        String param = parts[1].trim();
        int eq = param.indexOf('=');
        String name = eq < 0 ? param : param.substring(0, eq).trim();
        String value = eq < 0 ? "" : param.substring(eq + 1).trim();
        if (!"charset".equals(name.toLowerCase(Locale.ROOT))
                || !value.equalsIgnoreCase(expectedCharset)) {
            throw new AssertionError("Content-Type charset: expected <" + expectedCharset
                    + "> but parameter was <" + param + "> (raw: " + raw + ")");
        }
    }

    public static HttpResponse<String> send(String method, String url, String contentType, String body)
            throws java.io.IOException, InterruptedException {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30));
        HttpRequest.BodyPublisher pub = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        if (contentType != null) {
            b.header("Content-Type", contentType);
        }
        return CLIENT.send(b.method(method, pub).build(), HttpResponse.BodyHandlers.ofString());
    }

    /** GET with an explicit Accept header, for content-negotiation characterization. */
    public static HttpResponse<String> getWithAccept(String url, String accept)
            throws java.io.IOException, InterruptedException {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET();
        if (accept != null) {
            b.header("Accept", accept);
        }
        return CLIENT.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Request with freely chosen headers. Redirects are NOT followed (the shared
     * client uses the default Redirect.NEVER), so 3xx responses can be inspected.
     * Origin and Access-Control-* are not restricted on the JDK HttpClient.
     */
    public static HttpResponse<String> sendWithHeaders(String method, String url,
            java.util.Map<String, String> headers, String body)
            throws java.io.IOException, InterruptedException {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30));
        HttpRequest.BodyPublisher pub = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        if (headers != null) {
            headers.forEach(b::header);
        }
        return CLIENT.send(b.method(method, pub).build(), HttpResponse.BodyHandlers.ofString());
    }

    /** Pollt bis 200 oder Timeout; wirft AssertionError mit letzter Antwort. */
    public static void awaitOk(String url, Duration timeout) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        String last = "no response";
        while (System.nanoTime() < deadline) {
            try {
                HttpResponse<String> r = send("GET", url, null, null);
                if (r.statusCode() == 200) {
                    return;
                }
                last = r.statusCode() + ": " + r.body();
            } catch (java.io.IOException e) {
                last = e.toString();
            }
            Thread.sleep(2000);
        }
        throw new AssertionError("Timeout waiting for 200 from " + url + "; last: " + last);
    }
}
