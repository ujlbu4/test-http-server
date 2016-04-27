package ru.inn.httpserver.system.transport;


import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import ru.inn.httpserver.system.Debugger;
import ru.inn.httpserver.system.NameValuePair;
import ru.inn.httpserver.system.config.Options;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class HttpClientEndpoint {
    private final CloseableHttpClient httpClient;
    private HttpClientContext defaultContext;
    private static Debugger debug = Debugger.getInstance();


    public HttpClientEndpoint(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
        this.defaultContext = HttpClientContext.create();
    }

    public void executeRequest(HttpTypes method, String urlExtension, List<NameValuePair> headers, List<NameValuePair> body) {
        if (method == null) {
            throw new RuntimeException("Request without http method was send to http client.");
        }

        HttpRequestBuilder httpBuilder = new HttpRequestBuilder(Options.getInstance().getPartnerEndPoint());

        httpBuilder.appendURL(urlExtension);

        if (body != null) {
            httpBuilder.addParameters(body);
        }

        HttpRequestBase httpRequest;

        switch (method) {
            case GET:
                httpRequest = httpBuilder.getWcGetRequest();
                break;
            case POST:
                httpRequest = httpBuilder.getWcPostRequest();
                break;
            case PUT:
                httpRequest = httpBuilder.getWcPutRequest();
                break;
            case DELETE:
                httpRequest = httpBuilder.getWcDeleteRequest();
                break;
            case PATCH:
                httpRequest = httpBuilder.getWcPatchRequest();
                break;
            case OPTIONS:
                httpRequest = httpBuilder.getWcOptionsRequest();
                break;
            default:
                throw new RuntimeException("Unsupported http method: " + method.getString());
        }
        if (httpBuilder.getProxyHost() != null && httpBuilder.getProxyPort() != null) {
            HttpHost proxy = new HttpHost(httpBuilder.getProxyHost(), Integer.parseInt(httpBuilder.getProxyPort()));
            RequestConfig config = RequestConfig.custom()
                .setProxy(proxy)
                .build();
            httpRequest.setConfig(config);
        }

        if (headers != null && !headers.isEmpty()) {
            for (NameValuePair nvp : headers) {
                httpRequest.setHeader(nvp.getName(), nvp.getValue());
            }
        }
        debug.println("Building " + method.getString() + " Request[id: " + httpBuilder.getRequestId()
                          + "] : " + httpRequest.getURI() + ", Headers:" + headers +
                          (method.equals(HttpTypes.GET) ? "" : ", Body:" + body));


        executeRequest(httpRequest, method, httpBuilder.getRequestId());
    }

    //Main request method
    public void executeRequest(HttpRequestBase httpRequest, HttpTypes method, long requestId) {
        CloseableHttpResponse closeableHttpResponse = null;

        try {
            closeableHttpResponse = httpClient.execute((HttpRequestBase) httpRequest, defaultContext);
            StringBuffer output = new StringBuffer();
            if (closeableHttpResponse.getEntity() != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
                String line = "";
                while ((line = br.readLine()) != null) {
                    output.append(line);  // line termination symbols ignored
                }
            }

            String responseBody = output.toString();

            debug.println("Receiving " + method.getString() + " Response[id: " + requestId
                              + "] : " + (responseBody.isEmpty() ? " " : responseBody + ", ") +
                              "Status code: " + closeableHttpResponse.getStatusLine().getStatusCode() +
                              ", Headers:" + closeableHttpResponse.getAllHeaders().toString());

        } catch (IOException ioe) {
            throw new RuntimeException("Transport system I/O exception: cannot connect to " + httpRequest.getURI() + ", exception reason: " + ioe.getMessage(), ioe);
        } catch (Exception ex) {
            throw new RuntimeException("Transport system exception: " + ex.getMessage(), ex);
        } finally {
            try {
                if (closeableHttpResponse != null)
                    closeableHttpResponse.close();
            } catch (IOException e) {
                throw new RuntimeException("Transport system resources closing exception: " + e.getMessage(), e);
            }
        }

    }
}
