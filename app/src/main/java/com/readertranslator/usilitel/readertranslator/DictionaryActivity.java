package com.readertranslator.usilitel.readertranslator;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Activity со словарем
public class DictionaryActivity extends AppCompatActivity {

    private ExpandableListView expandableListView;
    private ExpandableListView expandableListViewBottom;
    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private HidedExpandableListAdapter adapter;
    private HidedExpandableListAdapter adapterBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        expandableListView = (ExpandableListView) findViewById(R.id.listDictionary);
        expandableListViewBottom = (ExpandableListView) findViewById(R.id.listDictionaryBottom);
        dbHelper = new DBHelper(this);
        database = dbHelper.getReadableDatabase();

        fillListView();
    }

    // заполняем оба ExpandableListView данными из БД
    private void fillListView(){

        // коллекция для групп
        ArrayList<Map<String, String>> groupData;
        // коллекция для элементов одной группы
        ArrayList<Map<String, String>> childDataItem;
        // общая коллекция для коллекций элементов
        ArrayList<ArrayList<Map<String, String>>> childData;
        childData = new ArrayList<ArrayList<Map<String, String>>>();
        // в итоге получится childData = ArrayList<childDataItem>

        // список атрибутов группы или элемента
        Map<String, String> m;

        Cursor cursor = database.rawQuery(
                " SELECT " + DBHelper.KEY_WORD
                        + ", max(" + DBHelper.KEY_TRANSCRIPTION + ") as " + DBHelper.KEY_TRANSCRIPTION
                        + ", max(" + DBHelper.KEY_TRANSLATION + ") as " + DBHelper.KEY_TRANSLATION
                        + " FROM " + DBHelper.TABLE_DICTIONARY
                        + " GROUP BY " + DBHelper.KEY_WORD
                        + " ORDER BY lower(" + DBHelper.KEY_WORD + ")",
                null
        );

        groupData = new ArrayList<Map<String, String>>();
        // список атрибутов групп для чтения
        String groupFrom[] = new String[] {"groupName"};
        // список ID view-элементов, в которые будет помещены атрибуты групп
        int groupTo[] = new int[] {android.R.id.text1};
        // список атрибутов элементов для чтения
        String childFrom[] = new String[] {"translationText"};
        // список ID view-элементов, в которые будет помещены атрибуты элементов
        int childTo[] = new int[] {android.R.id.text1};

        // бежим курсором по выборке и заполняем нужные данные
        if(cursor.moveToFirst()){
            int indexWord = cursor.getColumnIndex(DBHelper.KEY_WORD);
            int indexTranscription = cursor.getColumnIndex(DBHelper.KEY_TRANSCRIPTION);
            int indexTranslation = cursor.getColumnIndex(DBHelper.KEY_TRANSLATION);
            do{
                m = new HashMap<String, String>();
                m.put("groupName", cursor.getString(indexWord));
                groupData.add(m); // заполняем коллекцию групп

                // создаем коллекцию элементов для первой группы
                childDataItem = new ArrayList<Map<String, String>>();
                String str = cursor.getString(indexWord) + " " + cursor.getString(indexTranscription) + "\n" + cursor.getString(indexTranslation);
                m = new HashMap<String, String>();
                m.put("translationText", str); // название телефона
                childDataItem.add(m);
                childData.add(childDataItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        dbHelper.close();

        // адаптер для верхнего ExpandableListView (новые слова)
        adapter = new HidedExpandableListAdapter(
                this,
                groupData,
                android.R.layout.simple_expandable_list_item_1,
                groupFrom,
                groupTo,
                childData,
                android.R.layout.simple_list_item_1,
                childFrom,
                childTo
                );

        // адаптер для нижнего ExpandableListView (выученные слова)
        adapterBottom = new HidedExpandableListAdapter(
                this,
                groupData,
                android.R.layout.simple_expandable_list_item_1,
                groupFrom,
                groupTo,
                childData,
                android.R.layout.simple_list_item_1,
                childFrom,
                childTo
                );

        adapter.setExpandableListView(expandableListView);
        adapter.setLinkedExpandableListView(expandableListViewBottom);
        adapterBottom.setExpandableListView(expandableListViewBottom);
        adapterBottom.setLinkedExpandableListView(expandableListView);

        expandableListView.setAdapter(adapter);
        expandableListViewBottom.setAdapter(adapterBottom);

        // изначально все слова считаем новыми
        adapterBottom.hideAllElements();

        // меняем высоту ExpandableListView в зависимости от скрытых элементов
        ListUtils.setDynamicHeight(expandableListView);
        ListUtils.setDynamicHeight(expandableListViewBottom);

    }
}
