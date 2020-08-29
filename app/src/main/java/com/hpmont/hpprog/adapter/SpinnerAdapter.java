package com.hpmont.hpprog.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.hpmont.hpprog.R;

import java.util.ArrayList;
import java.util.List;

public class SpinnerAdapter extends BaseAdapter {
    private Context context;
    private List<String> spinnerItem = new ArrayList<String>();

    public SpinnerAdapter(Context context, ArrayList<String> list){
        this.context = context;
        this.spinnerItem = list;
    }

    @Override
    public int getCount() {
        return spinnerItem.size();
    }

    @Override
    public Object getItem(int position) {
        return spinnerItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        holderView hView = null;
        if(convertView == null){
            hView = new holderView();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_spinner,null);
            hView.spinnerItem = (TextView)convertView.findViewById(R.id.item_spinner);
            convertView.setTag(hView);
        }
        else{
            hView = (holderView)convertView.getTag();
        }
        String spinner = spinnerItem.get(position);
        if(spinner != null && !spinner.isEmpty()){
            hView.spinnerItem.setText(spinner);
        }
        else{
            hView.spinnerItem.setText(R.string.download_null);
        }
        return convertView;
    }

    public class holderView {
        public TextView spinnerItem;
    }
}
