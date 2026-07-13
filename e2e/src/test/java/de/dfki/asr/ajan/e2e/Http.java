package de.dfki.asr.ajan.e2e;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class Http {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private Http() {}

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
