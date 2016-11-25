package knulp.siteconnectioncheck;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import httprequest.RequestsToServer;

public class MainActivity extends AppCompatActivity {
    Context mContext;
    boolean stopCheck = false;

    private static final String TAG = "MainActivity";

    TimerTask connectTask;
    Timer timer;

    EditText editUrl;

    Toast showToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editUrl = (EditText) findViewById(R.id.edit_url);

        findViewById(R.id.btn_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCheck();
            }
        });
    }

    /**
     * 작업 시작!
     */
    public void startCheck() {
        stopCheck = false;
        if (editUrl.getText().toString().trim().equals("")) {
            showToast("URL을 입력해주세요.");
        } else if (!editUrl.getText().toString().startsWith("http://") && !editUrl.getText().toString().startsWith("https://")) {
            editUrl.setText("http://" + editUrl.getText().toString());
            startCheck();
        } else {
            showToast("작업이 시작됨!");
            if (connectTask != null) {
                connectTask.cancel();
            }

            connectTask = new TimerTask() {
                public void run() {
                    if (!stopCheck) {
                        new checkConnection(mContext, editUrl.getText().toString()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            };
            timer = new Timer();
            timer.schedule(connectTask, 0, 10000); // 0초후 첫실행, 10초마다 계속실행
        }
    }


    /**
     * 서버 접속되는지 체크하는 AsyncTask
     */
    public class checkConnection extends AsyncTask<Void, Void, Void> {
        Context mContext;
        Map<String, String> resultData = null;
        String url;

        public checkConnection(Context mContext, String url) {
            this.mContext = mContext;
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                resultData = new RequestsToServer().getURLCheck(mContext, url);
            } catch (JSONException | IOException | NullPointerException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            try {
                if (resultData != null) {
                    //UnknownHostException
                    if (Integer.parseInt(resultData.get("httpCode")) == -1) {
                        stopCheck = true;
                        if (connectTask != null) {
                            connectTask.cancel();
                            connectTask = null;
                        }
                        timer = null;
                        vibrate(500);
                        if (showToast != null) {
                            showToast.cancel();
                        }
                        showToast("정상적인 URL이 아닌 것 같습니다.\n체크 작업을 취소하였습니다.");
                    } else if (Integer.parseInt(resultData.get("httpCode")) < 400) {
                        Log.e(TAG, "접속 성공!");
                        if (!stopCheck) {
                            showNotification(url);
                        }
                        stopCheck = true;

                        if (connectTask != null) {
                            connectTask.cancel();
                            connectTask = null;
                        }
                        timer = null;

                        //성공

                    } else {
                        Log.e(TAG, "결과는 오는데 접속안됨");
                    }
                } else {
                    Log.e(TAG, "접속안됨");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 기존 토스트 취소하고 새 토스트 보여줌
     *
     * @param msg 내용
     */
    void showToast(String msg) {
        if (showToast != null) {
            showToast.cancel();
        }
        showToast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
        showToast.show();
    }

    /**
     * 로컬 노티보여줌.
     */
    void showNotification(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        long when = System.currentTimeMillis();
        int icon = R.mipmap.ic_launcher;
        Notification notification;

        long[] pattern = {4000,4000};
        notification = new Notification.Builder(getApplicationContext()).setPriority(Notification.PRIORITY_HIGH).setContentTitle("알람형아").setContentText("사이트 접속 가능! 나를눌러!").setSmallIcon(icon).setWhen
                (when).setVibrate(pattern).build();

        if (pendingIntent != null) {
            notification.contentIntent = pendingIntent;
        }
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(234234, notification);
    }

    /**
     * 진동
     */
    void vibrate(int length) {
        Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mVibrator.vibrate(length);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
