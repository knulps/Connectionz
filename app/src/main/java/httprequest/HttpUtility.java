package httprequest;


import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;


/**
 * This class encapsulates methods for requesting a server via HTTP GET/POST and
 * provides methods for parsing response from the server.
 *
 * @author www.codejava.net
 */
public class HttpUtility {
    /**
     * Represents an HTTP connection
     */

    private static final String TAG = "HttpUtility";
    private HttpURLConnection httpConn;

    /**
     * Makes an HTTP request using GET method to the specified URL.
     *
     * @param requestURL the URL of the remote server
     * @return An HttpURLConnection object
     * @throws IOException thrown if any I/O error occurred
     */
    public HttpURLConnection sendGetRequest(String requestURL, Map<String, String> params, Map<String, String> headers)
            throws IOException {
        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setReadTimeout(15000);
        httpConn.setConnectTimeout(15000);
        httpConn.setDoInput(true); // true if we want to read server's response
//        httpConn.setDoOutput(false); // false indicates this is a GET request
        httpConn.setRequestMethod("GET");


        StringBuffer requestParams = new StringBuffer();

        if (headers != null && headers.size() > 0) {
            Iterator<String> headerIterator = headers.keySet().iterator();
            while (headerIterator.hasNext()) {
                String key = headerIterator.next();
                String value = headers.get(key);
                httpConn.setRequestProperty(key, value);
                Log.d(TAG, "key :" + key + " value :" + value);
            }

        }

        if (params != null && params.size() > 0) {
            httpConn.setDoOutput(true); // true indicates POST request

            // creates the params string, encode them using URLEncoder
            Iterator<String> paramIterator = params.keySet().iterator();
            while (paramIterator.hasNext()) {
                String key = paramIterator.next();
                String value = params.get(key);
                requestParams.append(URLEncoder.encode(key, "UTF-8"));
                requestParams.append("=").append(URLEncoder.encode(value, "UTF-8"));
                requestParams.append("&");
            }

            // sends POST data
            OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
            writer.write(requestParams.toString());
            writer.flush();

        }

        return httpConn;
    }

    /**
     * Closes the connection if opened
     */
    public void disconnect() {
        if (httpConn != null) {
            httpConn.disconnect();
        }
    }
}