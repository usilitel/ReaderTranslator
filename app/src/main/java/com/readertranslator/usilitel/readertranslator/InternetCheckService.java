package com.readertranslator.usilitel.readertranslator;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// service проверяющая в фоне подключение к интернету
public class InternetCheckService extends Service {

    final String LOG_TAG = "InternetCheckService";
    // объект для запуска задачи в новом потоке
    private ExecutorService es;
    private int timerIntervalMs;
    private PendingIntent pendingIntent;

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        es=Executors.newFixedThreadPool(1);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        es.shutdown();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        timerIntervalMs=intent.getIntExtra("timerIntervalMs", 1000);
        pendingIntent = intent.getParcelableExtra("pendingIntent");
        es.execute(new InternetCheck()); // запуск потока через ExecutorService
        //new Thread(new InternetCheck()).start(); // запуск потока через Thread
        //new InternetCheck().execute(); // запуск потока через AsyncTask
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    //@androidx.annotation.Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // класс для проверки работы интернета
    class InternetCheck implements Runnable{
        @Override
        public void run() {
            for(int i=1;i<=100;i++){
            //while(true){
                Log.d(LOG_TAG, "InternetCheck.run()" + timerIntervalMs + " " + i);

                try {
                    TimeUnit.MILLISECONDS.sleep(timerIntervalMs);
                    boolean reachable = checkInternetConnection();
                    Log.d(LOG_TAG, "Host is reachable = " + reachable);
                    //System.out.println(reachable ? "Host is reachable" : "Host is NOT reachable");
                    if(!reachable){

                        // посылаем pendingIntent с результатом работы
                        // при работе через pendingIntent вызывается onResume())
                        //pendingIntent.send(MainActivity.STATUS_INTERNET_NOT_ACCESSABLE);
                        //Intent intent = new Intent().putExtra("result", 10);
                        //pendingIntent.send(InternetCheckService.this, MainActivity.STATUS_INTERNET_NOT_ACCESSABLE, intent);

                        // посылаем BroadCast с результатом работы
                        Intent intent = new Intent("broadcastAction").putExtra("result", 10);
                        sendBroadcast(intent);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            stopSelf();
        }
    }

    private boolean checkInternetConnection() {
        Boolean result = false;
        HttpURLConnection con = null;
        try {
            // todo: таймаут не работает. Попробовать на реальном устройстве.

            //InetAddress in = InetAddress.getByName("5.255.255.70");
            //System.out.println(in.isReachable(1000) ? "yandex.ru is reachable" : "yandex.ru is NOT reachable");
//                    Process proc = Runtime.getRuntime().exec("ping -n 1 https://ru.stackoverflow.com/");
//                    boolean reachable = (proc.waitFor()==0);
//                    System.out.println(reachable ? "Host is reachable" : "Host is NOT reachable");



            // HttpURLConnection.setFollowRedirects(false);
            // HttpURLConnection.setInstanceFollowRedirects(false)
//            con = (HttpURLConnection) new URL("https://ya.ru").openConnection();
//            con.setRequestMethod("HEAD");
//            con.setConnectTimeout(1000);
//            con.setReadTimeout(2000);
            //con.connect();
            //result = (con.getResponseCode() == HttpURLConnection.HTTP_OK);

//            Socket socket = new Socket();
//            InetSocketAddress socketAddress = new InetSocketAddress("ya.ru", 80);
//            socket.connect(socketAddress, 3000);

//            Process proc = Runtime.getRuntime().exec("ping -n 1 ya.ru");
//            proc.waitFor();
//            if(proc.exitValue() == 0) {
//                boolean reachable = true;
//            } else {
//                boolean reachable = false;
//            }
////            Process proc = Runtime.getRuntime().exec("ping -n 1 ya.ru");
//            boolean reachable = (proc.waitFor()==0);
//            System.out.println(reachable ? "Host is reachable" : "Host is NOT reachable");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //return result;
        return true;
    }

//    // класс для проверки работы интернета
//    class InternetCheck extends AsyncTask<Void, Void, Boolean> {
//        @Override
//        protected Boolean doInBackground(Void... voids) {
//            return true;
//        }
//        @Override
//        protected void onPostExecute(Boolean internetIsWorking) {
//            super.onPostExecute(internetIsWorking);
//            if(!internetIsWorking){
//
//            }
//        }
//    }

}
