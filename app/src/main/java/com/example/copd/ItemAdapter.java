package com.example.copd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class ItemAdapter extends ArrayAdapter<Item> {

    // 畫面資源編號
    private int resource;
    // 包裝的記事資料
    private List<Item> items;

    public ItemAdapter(Context context, int resource, List<Item> items) {
        super(context, resource, items);
        this.resource = resource;
        this.items = items;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout itemView;
        // 讀取目前位置的記事物件
        final Item item = getItem(position);

        if (convertView == null) {
            // 建立項目畫面元件
            itemView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li = (LayoutInflater)
                    getContext().getSystemService(inflater);
            li.inflate(resource, itemView, true);
        }
        else {
            itemView = (LinearLayout) convertView;
        }

        // 讀取記事顏色、已選擇、標題與日期時間元件
        TextView txv_dateTime = (TextView) itemView.findViewById(R.id.item_txv_dateTime);
        TextView txv_stageTime = (TextView) itemView.findViewById(R.id.item_txv_stageTime);
        TextView txv_heartRate = (TextView) itemView.findViewById(R.id.item_txv_heartRate);
        TextView txv_stage = (TextView) itemView.findViewById(R.id.item_txv_stage);


        // 設定標題與日期時間
        txv_dateTime.setText(item.getDatetime());
        txv_stageTime.setText(item.getStageTime());
        txv_heartRate.setText(item.getHeartRate());
        txv_stage.setText(item.getStage());

        return itemView;
    }

    // 設定指定編號的記事資料
    public void set(int index, Item item) {
        if (index >= 0 && index < items.size()) {
            items.set(index, item);
            notifyDataSetChanged();
        }
    }

    // 讀取指定編號的記事資料
    public Item get(int index) {
        return items.get(index);
    }
}
