package com.readertranslator.usilitel.readertranslator;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

// класс для работы с API dictionary.yandex.ru
public class DictionaryCall {

    private MessagesApi messagesApi;
    private String dictionaryKey = "dict.1.1.20190118T185645Z.a1e8dd51d0a4dca7.9667184064432b8d041f76df3eab95ee8d6bba34";
    //String[] translationResult;

    private MainActivity mainActivity;

//    public class Zzz{
//        public  void p1(){
////            mainActivity=new MainActivity();
////            MainActivity mainActivity2 = DictionaryCall.this.mainActivity;
////            mainActivity2 = new MainActivity();
//            p2(mainActivity);
//        }
//        public  void p2(MainActivity m){
//
//        }
//
//    }


    public DictionaryCall(MainActivity mainActivity){
        initRetrofit();
        this.mainActivity = mainActivity;
    }

    // задаем параметры Retrofit для доступа к API dictionary.yandex.net
    private void initRetrofit(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dictionary.yandex.net/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        messagesApi = retrofit.create(MessagesApi.class);
    }


    // посылаем запрос к yandex
    public void callDictionary(String lang, final String text, String ui){
//        String[] translation1;
        //translationResult=new String[0];
        Call<Message> messages = messagesApi.messages(dictionaryKey, lang, text, ui);
        Log.w("response_sent", "response sent");


        messages.enqueue(new Callback<Message>() {
            // получили ответ от yandex
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {

                if (response.isSuccessful()) {
                    Log.w("response_successful", "response " + response.body().def.length);
                    String[] translation = responseToString(text, response.body().def);
                    //translationResult = translation;

//                    // отображаем результат запроса (в UI-потоке)
//                    mainActivity.translatorView.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            mainActivity.translatorView.setText(translation[0] + " " + translation[1] + "\n" + translation[2]);
//                        }
//                    });
//                    mainActivity.processDictionaryCallResult(translation);



                    new UpdateUITask().execute(translation);
                } else {
                    Log.w("response_fail", "response code " + response.code());
                    try {
                        Log.w("response_fail", response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                Log.w("failure", "failure " + t);
            }
        });
        //return translationResult;
    }



    // класс для обновления информации в UI-потоке
    class UpdateUITask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String[] translation) {
            return translation;
        }

        @Override
        protected void onPostExecute(String... translation) {
            super.onPostExecute(translation);
            mainActivity.processDictionaryCallResult(translation);
        }
    }

    // классы для разбора ответа от yandex
    private class Message {
        //private Element[] head;
        private Element[] def;
    }
    private class Element {
        private String text;
        private String num;
        private String pos;
        private String gen;
        private Element[] syn;
        private Element[] mean;
        private Element[] ex;
        private String ts;
        private Element[] tr;
    }

    private interface MessagesApi {
        @FormUrlEncoded
        @POST("dicservice.json/lookup")
        Call<Message> messages(@Field("key") String key,
                               @Field("lang") String lang,
                               @Field("text") String text,
                               @Field("ui") String ui
        );
    }





    // преобразуем Element[] в массив строк
    // 0 - слово
    // 1 - транскрипция
    // 2 - перевод
    private String[] responseToString(String word, Element[] def){
        String[] resultArray=new String[3];
        resultArray[0]=word;
        String transcription="";
        String translation="";

        int i=1;
        // перебираем части речи
        for(Element e1: def){
            if((transcription.equals("")) && (e1.ts != null)){transcription="[" + e1.ts + "]";}
            translation=translation.concat("  " + e1.pos + ":\n");
            i=1;
            // перебираем варианты перевода
            if(e1.tr != null){
                for(Element e2: e1.tr){
                    translation=translation.concat(i + ". " + e2.text);
                    i++;
                    // перебираем синонимы
                    if (e2.syn != null){
                        for(Element e3: e2.syn){
                            translation=translation.concat(", " + e3.text);
                        }
                    }
                    translation=translation.concat("\n");
                }
                //translation=translation.concat("\n");
            }
        }

        if (translation != null && translation.length() > 0 && translation.charAt(translation.length() - 1) == '\n') {
            translation = translation.substring(0, translation.length() - 1);
        }

        resultArray[1]=transcription;
        resultArray[2]=translation;
        return resultArray;
    }

}
