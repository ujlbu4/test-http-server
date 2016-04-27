package ru.inn.httpserver.system.transport;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import ru.inn.httpserver.system.Debugger;
import ru.inn.httpserver.system.NameValuePair;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class HttpRequestBuilder {
    private String serverLocation;
    private String appendURLString;
    private String proxyHost = null;
    private String proxyPort = null;
    
    private List<NameValuePair> parameters;
    private static Debugger debugger = Debugger.getInstance();
    
    private long requestId = -5;
    
    public HttpRequestBuilder(String serverLocation) {
        this.serverLocation = serverLocation;
        reset();
        this.appendURLString = "";
    }
    
    public HttpRequestBuilder(String serverLocation, String proxyHost, String proxyPort) {
        this(serverLocation);
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }
    
    /**
     * Get target server IP
     */
    public String getServerLocation() {
        return serverLocation;
    }

    /**
     * Set target server IP
     */
    public void setServerLocation(String serverLocation) {
        this.serverLocation = serverLocation;
    }
    
    /**
     * Get proxy server IP
     */
    public String getProxyHost() {
        return proxyHost;
    }
    
    /**
     * Set proxy server IP
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }
    
    /**
     * Get proxy server port
     */
    public String getProxyPort() {
        return proxyPort;
    }
    
    /**
     * Set proxy server port
     */
    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }
    
    /**
     * Reset request parameters
     */
    public void reset() {
        this.parameters = new ArrayList<NameValuePair>();
        this.appendURLString = "";
    }

    /**
     * Get parameters list
     *
     * @return result - List NameValuePair
     */
    public List<NameValuePair> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    /**
     * Get single parameter
     *
     * @param key - parameters key
     * @return result - (String) key value
     */
    public String getParameter(String key) {
        String result = "";
        for (NameValuePair param : parameters) {
            if (key.equals(param.getName())) {
                result = param.getValue().toString();
            }
        }
        return result;
    }

    /**
     * Edit single parameter
     *
     * @param key   - parameters key
     * @param value - new value for selected key
     */
    public void editParameter(String key, String newValue) {
        for (NameValuePair param : parameters) {
            if (key.equals(param.getName())) {
                param.setValue(newValue);
            }
        }
    }
    
    /**
     * Add single parameter to request
     *
     * @param name  = key (String)
     * @param value = value for key (String)
     */
    public void addParameter(String name, String value) {
        parameters.add(new NameValuePair(name, value));
    }
    
    /**
     * Add parameters to request as list
     *
     * @param parameters - List NameValuePair
     */
    public void addParameters(List<NameValuePair> parameters) {
        for (NameValuePair param : parameters) {
            this.parameters.add(param);
        }
    }

//    /**
//     * Add parameters to request from request object
//     * @param requestDTO {@link RequestDTO} - request DTO
//     */
//    public void addParameters(RequestDTO requestDTO) {
//        addParameters(HttpBuilderUtils.processParameters(requestDTO));
//    }
    
    /**
     * Builder's GET method
     */
    public HttpGet getWcGetRequest() {
        URI uri;
        URIBuilder uriBuilder = new URIBuilder();
        String scheme = serverLocation.substring(0, serverLocation.indexOf("://"));
        String host = serverLocation.substring(serverLocation.indexOf("://") + 3);  //TODO: Remove magic number
        uriBuilder
            .setScheme(scheme)
            .setHost(host)
            .setPath(("".equals(appendURLString)) ? "" : "/" + appendURLString);
        
        List<org.apache.http.NameValuePair> nameValuePairs = new ArrayList<org.apache.http.NameValuePair>(1); //FIXME: rename f***ing NVP
        nameValuePairs.addAll(parameters);
        //Remove JSON tag parameter for GET request
        for (int i = 0; i < nameValuePairs.size(); i++) {
            if (((org.apache.http.NameValuePair) nameValuePairs.get(i)).getName().equals("jsonBodyTypeX")) {
                nameValuePairs.remove(i);
                break;
            }
        }
        
        uriBuilder.addParameters(nameValuePairs);
        
        try {
            uri = uriBuilder.build();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        HttpGet httpGet = new HttpGet(uri);
        
        return httpGet;
    }
    
    /**
     * Builder's POST method
     */
    public HttpPost getWcPostRequest() {
        HttpPost httpPost = new HttpPost(serverLocation + (("".equals(appendURLString)) ? "" : "/" + appendURLString));
        
        List<org.apache.http.NameValuePair> nameValuePairs = new ArrayList<org.apache.http.NameValuePair>(1); //FIXME: rename f***ing NVP
        nameValuePairs.addAll(parameters);
        
        String jsonBody = detectAndGetRequestBodyType();
        boolean isJSONBodyRequest = jsonBody == null ? false : true;
        
        try {
            if (isJSONBodyRequest) {  //API json body support!
                httpPost.setEntity(new ByteArrayEntity(jsonBody.getBytes("UTF-8")));
            } else {  //x-www-form-urlencoded version
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        return httpPost;
    }
    
    /**
     * Builder's PUT method //TODO: Unify POST PUT PATCH ETC
     */
    public HttpPut getWcPutRequest() {
        HttpPut httpPut = new HttpPut(serverLocation + (("".equals(appendURLString)) ? "" : "/" + appendURLString));
        
        List<org.apache.http.NameValuePair> nameValuePairs = new ArrayList<org.apache.http.NameValuePair>(1); //FIXME: rename f***ing NVP
        nameValuePairs.addAll(parameters);
        
        String jsonBody = detectAndGetRequestBodyType();
        boolean isJSONBodyRequest = jsonBody == null ? false : true;
        
        try {
            if (isJSONBodyRequest) {  //API json body support!
                httpPut.setEntity(new ByteArrayEntity(jsonBody.getBytes("UTF-8")));
            } else {  //x-www-form-urlencoded version
                httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        return httpPut;
    }
    
    /**
     * Builder's PATCH method //TODO: Unify POST PUT PATCH ETC
     */
    public HttpPatch getWcPatchRequest() {
        HttpPatch httpPatch = new HttpPatch(serverLocation + (("".equals(appendURLString)) ? "" : "/" + appendURLString));
        
        List<org.apache.http.NameValuePair> nameValuePairs = new ArrayList<org.apache.http.NameValuePair>(1); //FIXME: rename f***ing NVP
        nameValuePairs.addAll(parameters);
        
        String jsonBody = detectAndGetRequestBodyType();
        boolean isJSONBodyRequest = jsonBody == null ? false : true;
        
        try {
            if (isJSONBodyRequest) {  //API json body support!
                httpPatch.setEntity(new ByteArrayEntity(jsonBody.getBytes("UTF-8")));
            } else {  //x-www-form-urlencoded version
                httpPatch.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        return httpPatch;
    }
    
    /**
     * Builder's DELETE method //TODO: Unify POST PUT PATCH ETC
     */
    public HttpDelete getWcDeleteRequest() {
        HttpDelete httpDelete = new HttpDelete(serverLocation + (("".equals(appendURLString)) ? "" : "/" + appendURLString));
        
        return httpDelete;
    }
    
    /**
     * Builder's OPTIONS method  //TODO: Unify with GET
     */
    public HttpOptions getWcOptionsRequest() {
        URI uri;
        URIBuilder uriBuilder = new URIBuilder();
        String scheme = serverLocation.substring(0, serverLocation.indexOf("://"));
        String host = serverLocation.substring(serverLocation.indexOf("://") + 3);  //TODO: Remove magic number
        uriBuilder
            .setScheme(scheme)
            .setHost(host)
            .setPath(("".equals(appendURLString)) ? "" : "/" + appendURLString);
        
        List<org.apache.http.NameValuePair> nameValuePairs = new ArrayList<org.apache.http.NameValuePair>(1); //FIXME: rename f***ing NVP
        nameValuePairs.addAll(parameters);
        //Remove JSON tag parameter for GET request
        for (int i = 0; i < nameValuePairs.size(); i++) {
            if (((org.apache.http.NameValuePair) nameValuePairs.get(i)).getName().equals("jsonBodyTypeX")) {
                nameValuePairs.remove(i);
                break;
            }
        }
        
        uriBuilder.addParameters(nameValuePairs);
        
        try {
            uri = uriBuilder.build();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        HttpOptions httpOptions = new HttpOptions(uri);
        
        return httpOptions;
    }
    
    /**
     * Add sub-page to URL
     *
     * @param appendString - additional page
     */
    public void appendURL(String appendString) {
        if (appendString != null) {
            this.appendURLString += appendString;
            if (appendURLString.startsWith("/"))
                appendURLString = appendURLString.substring(1);
        }
    }
    
    /**
     * Get sub-page to URL
     *
     * @return appendString - additional page
     */
    public String getAppendURL() {
        return appendURLString;
    }
    
    /**
     * Reset sub-page to URL
     *
     * @param appendString - additional page
     */
    public void resetAppendURL(String appendString) {
        this.appendURLString = "";
    }
    
    private String detectAndGetRequestBodyType() {
        String result = null;
        for (NameValuePair nvp : parameters) {  //exclude any technical parameters for json body case
            if ("jsonBodyTypeX".equals(nvp.getName())) {
                result = nvp.getValue();
                break;
            }
        }
        return result;
    }

    public long getRequestId() {
        return requestId;
    }
}
