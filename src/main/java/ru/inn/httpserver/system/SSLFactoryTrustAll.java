package ru.inn.httpserver.system;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class SSLFactoryTrustAll {

    public static SSLSocketFactory getSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
        //create trust-all manager
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }
        }};

        SSLContext sc;
        sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
//        sc = SSLContext.getInstance("TLS");
//        sc.init(null, trustAllCerts, null);

        return sc.getSocketFactory();
    }
}
