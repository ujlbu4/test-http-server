package ru.inn.httpserver.server;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import ru.inn.httpserver.system.Debugger;
import ru.inn.httpserver.system.NameValuePair;
import ru.inn.httpserver.system.enums.Routes;
import ru.inn.httpserver.system.storage.Storage;
import ru.inn.httpserver.system.transport.HttpClientEndpoint;
import ru.inn.httpserver.system.transport.HttpTypes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

public class AbstractMockHandler extends AbstractHandler {

    private static Debugger debug = Debugger.getInstance();
    private Random random = new Random((new Date()).getTime());

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String sessionId = String.valueOf(random.nextInt()).substring(1);

        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");

        String[] tokens = request.getRequestURI().split("\\/");
        Map<String, String[]> params = request.getParameterMap();

        //Debug section
        debug.println("[" + sessionId + "] Incoming request URL: " + request.getRequestURL().toString());
        for (String s : params.keySet()) {
            debug.println("[" + sessionId + "] Parameter in request: " + s + ", value : " + params.get(s)[0]);
        }
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].replaceAll("\\/", "");  //Remove slashes
            if (!tokens[i].isEmpty())
                debug.println("[" + sessionId + "] Route in request: " + tokens[i]);
        }


        Routes route = findRoute(tokens);

        //response.setContentType("application/json;charset=utf-8");
        switch (route) {
            case TRACKING:
                handleTrackings(baseRequest, request, response);
                break;

            default:
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().println("Unknown route");
                break;
        }

    }

    //Refactor this
    private Routes findRoute(String[] routes) { //No null here, first token always present
        Routes result = null;
        int startPosition = (routes[0].isEmpty()) ? 1 : 0; //FIXME: Simple workaround
        switch (Routes.fromString(routes[startPosition])) {
            case TRACKING:
                result = Routes.TRACKING;
                break;
            default:
                result = Routes.UNKNOWN;
        }
        return result;
    }

    private void handleTrackings(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String[]> params = request.getParameterMap();

        baseRequest.setHandled(true);

        switch (request.getMethod()) {
            case "GET":
                response.setStatus(HttpServletResponse.SC_OK);
                if (isParametersCorrect(request)) {
                    String result = Storage.getInstance().getByParameter(request.getParameter("user_id") + request.getParameter("event"));
                    response.getWriter().println(result);

                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("Wrong input parameters. Mandatory parameters are: event, user_id");
                }
                break;
            case "POST":
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().println("Operation success");

                if (isParametersCorrect(request)) {
                    Storage.getInstance().put(request.getParameter("user_id"),
                                              request.getParameter("event"),
                                              request.getParameter("value")
                    );


                    try {
                        sendPostback(request);
                    } catch (Exception ex) {
                        debug.println("Exception occur:" + ex);
                    }


                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("Wrong input parameters. Mandatory parameters are: event, user_id, value");
                }

                break;
            case "DELETE":
                if (isParametersCorrect(request)) {
                    Storage.getInstance().delete(request.getParameter("user_id"),
                                                 request.getParameter("event"));

                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("Wrong input parameters. Mandatory parameters are: event, user_id");
                }
                break;
            default:
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                response.getWriter().println("Wrong http method");
                break;
        }
    }

    public boolean isParametersCorrect(HttpServletRequest request) {
        boolean result = false;

        //if (request.getParameter("event") != null && request.getParameter("user_id") != null && request.getParameter("value") != null) {
        if (request.getParameter("event") != null && request.getParameter("user_id") != null) {
            result = true;
        }

        return result;
    }

    public void sendPostback(HttpServletRequest request) throws Exception {

        switch (request.getParameter("event")) {
            case "registration":
                List<NameValuePair> body = new ArrayList<>();
                body.add(new NameValuePair("event", request.getParameter("event")));
                body.add(new NameValuePair("user_id", request.getParameter("user_id")));
                body.add(new NameValuePair("value", request.getParameter("value")));

                executeRequest(null, body);
                break;
            case "levelup":
                break;
            default:
                break;
        }
    }

    public void executeRequest(List<NameValuePair> headers, List<NameValuePair> body) throws Exception {

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return true;
            }
        });
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("https", sslsf)
            .register("http", new PlainConnectionSocketFactory())
            .build();

        //overriding PoolingHttpClientConnectionManager due to TLS (http://stackoverflow.com/questions/19517538/ignoring-ssl-certificate-in-apache-httpclient-4-3)
        PoolingHttpClientConnectionManager pcm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        pcm.setMaxTotal(300);
        pcm.setDefaultMaxPerRoute(200);


        CloseableHttpClient httpClientApache = HttpClients.custom()
            .setSSLSocketFactory(sslsf)
            .setConnectionManager(pcm)
//            .setDefaultRequestConfig(requestConfig)
            .setRedirectStrategy(new DefaultRedirectStrategy() {
                @Override
                //FIXME: Override 302 redirect for s2s tests. Move this to configurable part
                public boolean isRedirected(final HttpRequest request, final HttpResponse response, final HttpContext context) throws ProtocolException {
                    boolean isRedirected = super.isRedirected(request, response, context);
                    if (isRedirected) {
                        int responseCode = response.getStatusLine().getStatusCode();
                        if (responseCode == 302) {
                            return false;
                        }
                    }
                    return isRedirected;
                }
            })
            .build();

        HttpClientEndpoint httpClient = new HttpClientEndpoint(httpClientApache);

        httpClient.executeRequest(HttpTypes.GET,
                                  "/postbacks",
                                  headers,
                                  body);

    }
}
