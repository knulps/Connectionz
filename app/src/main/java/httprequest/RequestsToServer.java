package httprequest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class RequestsToServer {

    private static final String TAG = "RequestsToServer";

    /**
     * 공통적인 get request를 관리한다.
     *
     * @param url     get request 요청할 url
     * @param headers 헤더
     * @param params  파라미터
     * @return 일반적인 get request의 결과를 리턴<br>
     * 결과값, 서버 요청에 문제가 있을 경우 null을 리턴한다.
     */
    public Map<String, String> getRequestResult(Context mContext, String url, Map<String, String> headers, Map<String, String> params, String requestName) {
        Log.e(TAG, requestName + " 진입, url : " + url);
        if (!checkInternetConnection(mContext)) {
            return null;
        }
        int resultCode = 0;
        HttpUtility httpUtility = new HttpUtility();

        try {
            resultCode = httpUtility.sendGetRequest(url, params, headers).getResponseCode();

            Log.e(TAG, "server resultcode : " + resultCode);

            httpUtility.disconnect();
            Map<String, String> resultData = new HashMap<>();
            resultData.put("httpCode", String.valueOf(resultCode));
            return resultData;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            httpUtility.disconnect();
            Map<String, String> resultData = new HashMap<>();
            resultData.put("httpCode", "-1");
            return resultData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        httpUtility.disconnect();
        return null;
    }


    /**
     * ABC 마트 접속 요청
     *
     * @param mContext context
     * @return 요청 결과
     * @throws JSONException
     * @throws IOException
     */
    public Map<String, String> getURLCheck(Context mContext, String url) throws JSONException, IOException {
        return getRequestResult(mContext, url, null, null, "getURLCheck");
    }


    /**
     * 네트워크 연결 여부 확인
     *
     * @return 연결 여부
     */
    public static Boolean checkInternetConnection(Context context) {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            return (networkInfo != null && networkInfo.isConnected());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
