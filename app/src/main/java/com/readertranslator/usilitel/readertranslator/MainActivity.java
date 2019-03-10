package com.readertranslator.usilitel.readertranslator;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.Locale;
import java.util.Observable;


public class MainActivity extends AppCompatActivity  {

    private TextView definitionView;
    public TextView translatorView;
    private final Integer REQUEST_SAVE=1;
    private final Integer REQUEST_LOAD=2;
    private final Integer REQUEST_STARTSERVICE=3;
    private Spannable spans;
    private int prevSpanStart=0,prevSpanEnd=0; // координаты предыдущего span-а
    private int initTextBackColor;

    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private DictionaryCall dictionaryCall;

    private Intent intent;
    public static final int STATUS_INTERNET_NOT_ACCESSABLE=1;
    public static final int STATUS_INTERNET_ACCESSABLE=2;

    BroadcastReceiver broadcastReceiver;

    //FragmentTranslation fragmentTranslation;
    //FragmentTransaction fTrans;

//    @Override
//    protected void onRequestPermissionResult {}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        fragmentTranslation = new FragmentTranslation();
//        fTrans = getFragmentManager().beginTransaction();
//        fTrans.add(R.id.frgmCont, fragmentTranslation);
//        //fTrans.addToBackStack(null);
//        fTrans.commit();

        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void init() throws IOException {
        copyFileToSD();
        String text = " The year of 1998 was announced by UNESCO the Year of Pushkin. In this way the mankind paid tribute to the genius of the Russian literature in commemoration of his 200th anniversary, which was celebrated world wide in June 1999.\n" +
                "\n" +
                "A. Pushkin was a Russian poet, novelist, dramatist, and short-story writer. He is considered his country’s greatest poet and the founder of modern Russian literature. He is the author of «Yevgeny Onegin», «little Tragedies», «The Queen of Spades», «Boris Godunov» and wonderful verses and fairy tales.\n" +
                "\n" +
                "We have grown up with his learned cat, who walked round and round the oak-tree, singing songs as he circled right, and telling tales as he circled left. We were fond of his exiled Prince, who was turned into a bumble-bee so that he could fly to his father’s court and sting his wicked aunt on the nose. Later we experienced Onegin’s boredom, Tatiana’s unrequited love, Godunov’s uneasy conscience, Herman’s tension at the gaming table, Saliere’s jealousy of Mozart, and heard the dead steps of the Stone Guest and the thundering hooves of the Bronze Horseman.\n" +
                "\n" +
                "His life was no less exciting than his works. He was born in Moscow on the 6th of June 1799. His father came of an old boyar family. His mother was a granddaughter of Abram Hannibal, who, according to family tradition, was an Abyssinian princeling bought as a slave at Constantinople (Istanbul) and adopted by Peter the Great and became his comrade in arms. Pushkin immortalized him in an unfinished historical novel, Arap Petra Velikogo (The Negro of Peter the Great). Like many aristocratic families in early 19th century Russia, Pushkin’s parents adopted French culture. Alexander and his brother and sister learned to talk and to read in French. The children were left much to the care of their maternal grandmother, who told Alexander stories of his ancestors in Russian. From his old nurse Arina Rodionovna Yakovleva, a freed serf, he heard Russian folktales. During summers at his grandmother’s estate near Moscow he talked to the peasants and spent hours alone, living in the dream world of an imaginative child. He read a lot and gained stimulus from the literary guests who came to the house.\n" +
                "\n";
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        definitionView = (TextView) findViewById(R.id.definitionView);
        translatorView = (TextView) findViewById(R.id.translatorView);
        //Fragment frag1 = getFragmentManager().findFragmentById(R.id.fragment1);
        //translatorView = (TextView) fragmentTranslation.getView().findViewById(R.id.translatorView);
        initTextBackColor=definitionView.getDrawingCacheBackgroundColor();
        setTextSpannable(text);
        //initRetrofit();
        dictionaryCall = new DictionaryCall(this);

        // создаем PendingIntent для обратной связи с service-ом
//        PendingIntent pendingIntent = createPendingResult(0,new Intent(),0);
//        this.intent = new Intent(this, InternetCheckService.class)
//                .putExtra("timerIntervalMs", 1000)
//                .putExtra("pendingIntent",pendingIntent);
//        startService(intent);

        // создаем BroadcastReceiver для обратной связи с service-ом
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            // действие при получении сообщений
            public void onReceive(Context context, Intent intent) {
                int result = intent.getIntExtra("result",0);
                Log.d("MainActivity", "broadcastReceiver.onReceive");
                Toast.makeText(MainActivity.this,"INTERNET NOT ACCESSABLE", Toast.LENGTH_SHORT).show();
            }
        };
        IntentFilter intentFilter = new IntentFilter("broadcastAction");
        registerReceiver(broadcastReceiver, intentFilter);

        // запускаем InternetCheckService
        this.intent = new Intent(this, InternetCheckService.class)
                .putExtra("timerIntervalMs", 1000);
        startService(intent);


    }

    @Override
    public void onResume() {
        super.onResume();

        //Intent intent = new Intent();
//        PendingIntent pendingIntent = createPendingResult(0,new Intent(),0);
//        this.intent = new Intent(this, InternetCheckService.class)
//                .putExtra("timerIntervalMs", 1000)
//                .putExtra("pendingIntent",pendingIntent);
        //startService(intent);
    }

    @Override
    public void onPause() {
        //stopService(new Intent(this, InternetCheckService.class));
        stopService(intent);
        super.onPause();
    }
    @Override
    public void onStop() {
        //stopService(new Intent(this, InternetCheckService.class));
        stopService(intent);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stopService(new Intent(this, InternetCheckService.class));
        stopService(intent);
        unregisterReceiver(broadcastReceiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // обработка меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // закрываем приложение
        if (item.getItemId()==R.id.exit){
            dbHelper.close();
            this.finish();
        }
        // открываем форму для выбора файла
        if (item.getItemId()==R.id.open){
            Intent intent = new Intent(getBaseContext(), FileDialog.class);
            intent.putExtra(FileDialog.START_PATH, "/sdcard");
            intent.putExtra(FileDialog.CAN_SELECT_DIR, false); //can user select directories or not
            startActivityForResult(intent, REQUEST_LOAD);
        }
        // удаляем БД со словарем
        if (item.getItemId()==R.id.delete_database){
            this.deleteDatabase(DBHelper.DATABASE_NAME);
            dbHelper.close();
            dbHelper = new DBHelper(this);
            database = dbHelper.getWritableDatabase();
            //dictionaryCall = new DictionaryCall(translatorView, database);
        }
        // открываем словарь
        if (item.getItemId()==R.id.show_dictionary){
            Intent intent = new Intent(getBaseContext(), DictionaryActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    // считываем содержимое файла в String с помощью BufferedReader
    private static String readUsingBufferedReader(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader( new FileReader(fileName));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        while( ( line = reader.readLine() ) != null ) {
            stringBuilder.append( line );
            stringBuilder.append( ls );
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }


    // обрабатываем результат выбора файла
    public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {

        super.onActivityResult(requestCode,resultCode,data);

        // выбрали файл для загрузки
        if(requestCode==REQUEST_LOAD){
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == REQUEST_SAVE) {
                    System.out.println("Saving...");
                } else if (requestCode == REQUEST_LOAD) { // грузим выбранный файл в TextView
                    System.out.println("Loading...");
                    String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
                    System.out.println(filePath);
                    String content="";
                    try {
                        content=readUsingBufferedReader(filePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setTextSpannable(content);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.w("test","file not selected");
            }
        }

        // получаем результат от InternetCheckService (при работе через pendingIntent вызывается onResume())
//        if(requestCode==0){
//            // ловим сообщение о доступности интернета
//            if(resultCode==STATUS_INTERNET_NOT_ACCESSABLE){
//                int result = data.getIntExtra("result",0);
//                Toast.makeText(this,"INTERNET NOT ACCESSABLE", Toast.LENGTH_SHORT).show();
//            }
//        }

    }

    // грузим текст в TextView и разделяем его на span-ы
    private void setTextSpannable(String text) {
        definitionView.setMovementMethod(LinkMovementMethod.getInstance());
        definitionView.setText(text, TextView.BufferType.SPANNABLE);
        spans = (Spannable) definitionView.getText();
        BreakIterator iterator = BreakIterator.getWordInstance(Locale.US);
        iterator.setText(text);
        int start = iterator.first();

        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String possibleWord = text.substring(start, end);
            if (Character.isLetterOrDigit(possibleWord.charAt(0))) {
                ClickableSpan clickSpan = getClickableSpan(possibleWord);
                spans.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

    }



    // мой span
    private ClickableSpan getClickableSpan(final String word) {
        return new ClickableSpan() {
            final String mWord;
            {
                mWord = word;
            }

            @Override
            // обработка нажатия на span
            public void onClick(View widget) {
                Log.d("tapped on:", mWord);
                ClickableSpan[] clickableSpans = spans.getSpans(0, spans.length(), ClickableSpan.class);
                spans.setSpan(new BackgroundColorSpan(initTextBackColor), prevSpanStart, prevSpanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spans.setSpan(new BackgroundColorSpan(Color.RED), spans.getSpanStart(this), spans.getSpanEnd(this), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                prevSpanStart=spans.getSpanStart(this);
                prevSpanEnd=spans.getSpanEnd(this);
                dictionaryCall.callDictionary("en-ru", mWord, "en");
//                String[] params = {"en-ru", mWord, "en"}; //new String[3]("","","");
//                new SendDictionaryCall().execute(params);
            }

            // начальное состояние span-а
            public void updateDrawState(TextPaint ds) {
                //super.updateDrawState(ds);
                //ds.setColor(ds.linkColor);
                //ds.setUnderlineText(true);
                ds.bgColor=initTextBackColor;
            }
        };
    }

//    // класс для обновления информации в UI-потоке
//    class SendDictionaryCall extends AsyncTask<String, Void, String[]> {
//        @Override
//        protected String[] doInBackground(String[] dictionaryCallParams) {
//            String[] translation = dictionaryCall.callDictionary(dictionaryCallParams[0],dictionaryCallParams[1],dictionaryCallParams[2]);
//            return translation;
//        }
//
//        @Override
//        protected void onPostExecute(String... translation) {
//            super.onPostExecute(translation);
//            processDictionaryCallResult(translation);
//        }
//    }


    // обрабатываем результат запроса к словарю
    public void processDictionaryCallResult(String[] translation){
        // отображаем результат запроса
        translatorView.setText(translation[0] + " " + translation[1] + "\n" + translation[2]);
        // пишем в БД результат запроса
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_WORD, translation[0]);
        contentValues.put(DBHelper.KEY_TRANSCRIPTION, translation[1]);
        contentValues.put(DBHelper.KEY_TRANSLATION, translation[2]);
        database.insert(DBHelper.TABLE_DICTIONARY, null, contentValues);
    }






    // копируем файл на SD-карту
    private void copyFileToSD() throws IOException {
        int permissionStatusW = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionStatusR = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);

        if(permissionStatusR==PackageManager.PERMISSION_GRANTED && permissionStatusW==PackageManager.PERMISSION_GRANTED){}

        InputStream is = getResources().openRawResource(R.raw.text);

        File file = new File("/sdcard/text2.txt");

        byte[] buffer = new byte[1024];
        int length;

        try {
            file.createNewFile();
            FileOutputStream os = new FileOutputStream(file);
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        BufferedReader r = new BufferedReader(new FileReader("/sdcard/text2.txt"));
        StringBuilder total = new StringBuilder();
        String line;
        while((line = r.readLine()) != null) {
            total.append(line);
        }
        r.close();
    }
//    DictionaryCall dictionaryCall1 = new DictionaryCall(this);
//    DictionaryCall.Zzz zzz = new DictionaryCall.Zzz();

}
