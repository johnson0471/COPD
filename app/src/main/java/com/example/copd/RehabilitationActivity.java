package com.example.copd;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RehabilitationActivity extends AppCompatActivity {

    public static final String TAG = "RehabilitationActivity";

    private ListView listView;
    private ItemAdapter itemAdapter, itemAdapter_search;
    private Calendar calendar;
    private Button btn_startDate, btn_endDate, btn_search;
    private TextView txv_startDate, txv_endDate;

    private int int_startYear, int_startMonth, int_startDay;
    private int int_endYear, int_endMonth, int_endDay;

    //確認在搜尋期間內的筆數
    private int int_checkDate = 0, int_checkDate_past = 0;

    public List<Item> items_rehabilitation = new ArrayList<Item>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rehabilitation);

        val();

        //設置搜尋起始日期
        btn_startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(RehabilitationActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        txv_startDate.setText(year + "/" + (month + 1) + "/" + dayOfMonth);

                        int_startYear = year;
                        int_startMonth = month;
                        int_startDay = dayOfMonth;
                    }
                }, year, month, day);

                datePickerDialog.show();
            }
        });


        //設定搜尋截止日期
        btn_endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                final int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(RehabilitationActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                txv_endDate.setText(year + "/" + (month + 1) + "/" + dayOfMonth);

                                int_endYear = year;
                                int_endMonth = month;
                                int_endDay = dayOfMonth;
                            }
                        }, year, month, day);

                datePickerDialog.show();
            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int min_year, min_month, min_day;
                int max_year, max_month, max_day;

                int endNumber, startNumber;

                items_rehabilitation.clear();

                startNumber = int_startYear * 365 + (int_startMonth + 1) * 30 + int_startDay;
                endNumber = int_endYear * 365 + (int_endMonth + 1) * 30 + int_endDay;

                Log.e(TAG, "int_startYear: " + int_startYear + "  int_startMonth: " + (int_startMonth + 1) + "  int_startDay: " + int_startDay);
                Log.e(TAG, "int_endYear: " + int_endYear + "  int_endMonth: " + (int_endMonth + 1) + "  int_endDay: " + int_endDay);
                Log.e(TAG, "startNumber: " + startNumber + " endNumber: " + endNumber);

                //確認年、月、日
                if (startNumber < endNumber){
                    max_year = int_endYear;
                    max_month = int_endMonth + 1;
                    max_day = int_endDay;
                    min_year = int_startYear;
                    min_month = int_startMonth + 1;
                    min_day = int_startDay;
                }else{
                    max_year = int_startYear;
                    max_month = int_startMonth + 1;
                    max_day = int_startDay;
                    min_year = int_endYear;
                    min_month = int_endMonth + 1;
                    min_day = int_endDay;
                }







                Log.e(TAG, "max_year: " + max_year + "  max_month: " + max_month + "  max_day: " + max_day);
                Log.e(TAG, "min_year: " + min_year + "  min_month: " + min_month + "  min_day: " + min_day);
                Log.e(TAG, "MainActivity.items.size(): " + Main.items.size());

                for (int i = 0; i < Main.items.size(); i++){
                    int year = Integer.valueOf(Main.items.get(i).getDatetime().substring(0, 4));
                    int month = Integer.valueOf(Main.items.get(i).getDatetime().substring(5, 7));
                    int day = Integer.valueOf(Main.items.get(i).getDatetime().substring(8, 10));

                    int itemNumber;


                    Log.e(TAG, "year: " + year + " month: " + month + " day: " + day);
                    if (min_year < year && max_year > year){
                        items_rehabilitation.add(Main.items.get(i));
                        int_checkDate++;
                    }else if (min_year == year && max_year == year){
                        itemNumber = (month - min_month) * 31 + (day - min_day);
                        if (itemNumber >= 0){
                            itemNumber = (max_month - month) * 31 + (max_day - day);
                            if (itemNumber >= 0){
                            items_rehabilitation.add(Main.items.get(i));
                                int_checkDate++;
                            }
                        }
                    }else if (max_year == year){
                        itemNumber = (max_month - month) * 31 + (max_day - day);
                        if (itemNumber >= 0){
                            items_rehabilitation.add(Main.items.get(i));
                            int_checkDate++;
                        }
                    }else if (min_year == year){
                        itemNumber = (month - min_month) * 31 + (day - min_day);
                        if (itemNumber >= 0){
                            items_rehabilitation.add(Main.items.get(i));
                            int_checkDate++;
                        }
                    }


                    if (int_checkDate == int_checkDate_past){
                        itemAdapter_search.remove(Main.items.get(i));
                    }

                    int_checkDate_past = int_checkDate;
                }

                Toast.makeText(RehabilitationActivity.this, "Data : " + int_checkDate, Toast.LENGTH_LONG).show();
                Toast.makeText(RehabilitationActivity.this, "items_rehabilitation.size(): " + items_rehabilitation.size(), Toast.LENGTH_SHORT).show();
                int_checkDate = 0;

                listView.setAdapter(itemAdapter_search);
                itemAdapter_search.notifyDataSetChanged();
            }
        });

        itemAdapter = new ItemAdapter(this, R.layout.single_item, (List<Item>) Main.items);
        itemAdapter_search = new ItemAdapter(this, R.layout.single_item, items_rehabilitation);

        //將MainActivity裝好的所有資料(items)
        // 放進這個listView
        listView.setAdapter(itemAdapter);
    }

    private void val(){
        btn_search = (Button) findViewById(R.id.btn_search);
        btn_startDate = (Button) findViewById(R.id.btn_startDate);
        btn_endDate = (Button) findViewById(R.id.btn_endDate);

        txv_startDate = (TextView) findViewById(R.id.txv_startDate);
        txv_endDate = (TextView) findViewById(R.id.txv_endDate);

        listView = (ListView) findViewById(R.id.lv_rehabilitation);
    }

    @Override
    protected void onStart() {
        super.onStart();
        listView.setAdapter(itemAdapter);
        itemAdapter.notifyDataSetChanged();
    }


}
