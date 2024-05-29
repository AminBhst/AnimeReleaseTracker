package com.aminbhst.animereleasetracker.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Slf4j
public class HttpUtils {

    @SneakyThrows
    public static void get(String url, Function<CloseableHttpResponse, Void> func) {
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpClient client = buildClient()) {
            CloseableHttpResponse response = client.execute(httpGet);
            func.apply(response);
            response.close();
        } catch (Throwable t) {
            log.error("Failed to get {}", url, t);
        }
    }

    public static JsonNode getJson(String url, Map<String, String> headers) {
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpClient client = HttpUtils.buildClient()) {
            headers.forEach(httpGet::setHeader);
            CloseableHttpResponse response = client.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            if (status != 200) {
                log.error("Failed to send request! Code: {}", status);
                return JsonNodeFactory.instance.nullNode();
            }
            return new ObjectMapper().readTree(response.getEntity().getContent());
        } catch (Throwable t) {
            log.error("Failed to get {}", url, t);
        }
        return JsonNodeFactory.instance.nullNode();
    }

//    @SneakyThrows
//    public static HttpResponseContainer get(String url) {
//        HttpGet httpGet = new HttpGet(url);
//        CloseableHttpClient client = buildClient();
//        CloseableHttpResponse response = client.execute(httpGet);
//        return new HttpResponseContainer(url, client, response);
//    }

    public static void getResponseInputStream(String url, Function<InputStream, Void> func) {
        get(url, response -> {
            try {
                func.apply(response.getEntity().getContent());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    public static String getHtmlResponse(String url) {
        AtomicReference<String> html = new AtomicReference<>();
        get(url, response -> {
            try {
                html.set(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
        return html.get();
    }

    @SneakyThrows
    public static void post(String url, Function<CloseableHttpResponse, Void> func) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        try (CloseableHttpClient client = buildClient()) {
            CloseableHttpResponse response = client.execute(httpPost);
            func.apply(response);
            response.close();
        }
    }

    @SneakyThrows
    public static CloseableHttpClient buildClient() {
        return HttpClients.custom()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
    }
}
