package com.askey.ROMtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.util.TimeUtils;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ROMTest";

    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    private Timer timer,timer2;
    private TimerTask timerTask,timerTask2;
    private TextView Time;

    private static final int BUFF_SIZE = 1024 * 10 * 1024;
    private byte[] buffer = null;

    private static int N=0;
    private static int T=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startLogcat();

        Time = (TextView) findViewById(R.id.time);
        Time.setText("00:00:00");

        timer2 = new Timer();
        timerTask2 = new TimerTask() {
            @Override
            public void run() {
                T=T+1;
                runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         Time.setText(getTime(T*1000));
                     }
                 });
                Log.d(TAG, "TotalTime = " + getTime(T*1000));
            }
        };
        /**
         * 第一個參數：任務
         * 第二個參數：初始啓動等待時間
         * 第三個參數：時間間隔
         */
        timer2.schedule(timerTask2, 1000, 1000);


        powerManager = (PowerManager)getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
        wakeLock.setReferenceCounted(false);
        if (null != wakeLock) {
            wakeLock.acquire();
            Log.d(TAG, "null != wakeLock");
        }
        Log.d(TAG, "isSdCardExist():" + isSdCardExist());
        initData();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                N = N+1;
                if(isSdCardExist()){
                    WriteData();
                }
                delay(1000);
                ReadData();
            }
        };
        /**
         * 第一個參數：任務
         * 第二個參數：初始啓動等待時間
         * 第三個參數：時間間隔
         */
        timer.schedule(timerTask, 0, 5000);

    }

    public static String getTime(int second) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        String hms = formatter.format(second);

        return hms;
    }

    private void initData() {
        byte[] a = new byte[] {'a', 'b', 'c', 'd', 'e',
                'f', 'g', 'h', 'i', 'j' };
        if (buffer == null) {
            buffer = new byte[BUFF_SIZE];
            for (int i=0; i<BUFF_SIZE; i++) {
                buffer[i] = a[i % 10];
            }
        }
    }

    private void delay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void WriteData() {

        try {
//            File file = new File(this.getExternalFilesDir(null).getAbsolutePath(),
//                    "test.txt");
            File file = new File(this.getFilesDir().getAbsolutePath(),
                    "ROMTest.txt");
            FileOutputStream fos = new FileOutputStream(file);
            //String info = "Test";
            fos.write(buffer);
            fos.close();
            Log.d(TAG, "寫入:" + N + "次");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ReadData() {
        try {
//            File file = new File(this.getExternalFilesDir(null).getAbsolutePath(),
//                    "test.txt");
            File file = new File(this.getFilesDir().getAbsolutePath(),
                    "ROMTest.txt");
            FileInputStream is = new FileInputStream(file);
            byte[] b = new byte[is.available()];
            is.read(b);
            String result = new String(b);
            Log.d(TAG, "讀取："+result);
            Log.d(TAG, "讀取:" + N + "次");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isSdCardExist() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    private void startLogcat() {
        //start logcat
        Intent intent = new Intent(MainActivity.this, LogService.class);
        startService(intent);

    }

    private void stopLogcat() {
        Intent it = new Intent(MainActivity.this, LogService.class);
        stopService(it);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy ");
        if (null != wakeLock) {
            if ( wakeLock.isHeld() ) {
                wakeLock.release();
            }
            Log.d(TAG, "onDestroy: PowerManager.WakeLock= " + wakeLock);
            wakeLock = null;
        }
        timer.cancel();
        // 一定設置爲null，否則定時器不會被回收
        timer = null;

        timer2.cancel();
        // 一定設置爲null，否則定時器不會被回收
        timer2 = null;
        stopLogcat();
    }
}