package com.example.copd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

// 參考 http://www.codedata.com.tw/mobile/android-tutorial-the-3rd-class-3-sqlite
public class ItemCOPD {

    public static final String KEY_ID = "_id";

    public static final String TABLE_NAME = "COPD";

    public static final String NAME_COLUMN = "name";
    public static final String DATETIME_COLUMN = "dateTime";
    public static final String HEART_RATE_COLUMN = "heartRate";
    public static final String STAGE_COLUMN = "stage";
    public static final String STAGE_TIME_COLUMN = "stageTime";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            NAME_COLUMN + " TEXT NOT NULL," +
            DATETIME_COLUMN + " TEXT NOT NULL," +
            STAGE_TIME_COLUMN + " TEXT NOT NULL," +
            HEART_RATE_COLUMN + " TEXT NOT NULL," +
            STAGE_COLUMN + " TEXT NOT NULL);";

    private SQLiteDatabase db;

    public ItemCOPD(Context context){
        db = MyDBHelper.getDatabase(context);
    }

    // 關閉資料庫，一般的應用都不需要修改
    public void close() {
        db.close();
    }

    public Item insert (Item item){
        // 建立準備修改資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的修改資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(NAME_COLUMN, item.getName());
        cv.put(DATETIME_COLUMN, item.getDatetime());
        cv.put(STAGE_TIME_COLUMN, item.getStageTime());
        cv.put(HEART_RATE_COLUMN, item.getHeartRate());
        cv.put(STAGE_COLUMN, item.getStage());

        // 新增一筆資料並取得編號
        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        long id = db.insert(TABLE_NAME, null, cv);

        // 設定編號
        item.setId(id);

        return  item;
    }

    public boolean update(Item item){
        ContentValues cv = new ContentValues();

        cv.put(NAME_COLUMN, item.getName());
        cv.put(DATETIME_COLUMN, item.getDatetime());
        cv.put(STAGE_TIME_COLUMN, item.getStageTime());
        cv.put(HEART_RATE_COLUMN, item.getHeartRate());
        cv.put(STAGE_COLUMN, item.getStage());

        // 設定修改資料的條件為編號
        // 格式為「欄位名稱＝資料」
        String where = KEY_ID + "=" + item.getId();

        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    // 刪除參數指定編號的資料
    public boolean delete(long id){
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_ID + "=" + id;
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where , null) > 0;
    }

    // 讀取所有記事資料
    public List<Item> getAll() {
        List<Item> result = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    // 取得指定編號的資料物件
    public Item get(long id) {
        // 準備回傳結果用的物件
        Item item = null;
        // 使用編號為查詢條件
        String where = KEY_ID + "=" + id;
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result);
        }

        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return item;
    }

    // 把Cursor目前的資料包裝為物件
    public Item getRecord(Cursor cursor) {
        // 準備回傳結果用的物件
        Item result = new Item();

        result.setId(cursor.getLong(0));
        result.setName(cursor.getString(1));
        result.setDatetime(cursor.getString(2));
        result.setStageTime(cursor.getString(3));
        result.setHeartRate(cursor.getString(4));
        result.setStage(cursor.getString(5));

        // 回傳結果
        return result;
    }

    // 取得資料數量
    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }

        return result;
    }

    public void sample() {
        Item item = new Item(0, "User", "dateTime", "stageTime", "HR", "stage");
        insert(item);
    }


}
