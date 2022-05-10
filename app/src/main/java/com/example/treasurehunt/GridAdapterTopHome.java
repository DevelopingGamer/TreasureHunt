package com.example.treasurehunt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GridAdapterTopHome extends BaseAdapter {
    Context context;
    LayoutInflater inflater;

    //FirebaseFirestore db = FirebaseFirestore.getInstance();

    String [] huntNames;

    public GridAdapterTopHome(Context context, String [] huntNames) {
        this.context = context;
        this.huntNames = huntNames;
        inflater = (LayoutInflater.from(context)) ;
    }

    @Override
    public int getCount() {
        return huntNames.length;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView,  ViewGroup parent) {
        ViewHolder viewHolder;
        final View result;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.hunts_grid_tile, parent, false);
            //viewHolder.huntSelect = (Button) convertView.findViewById(R.id.huntSelect);
            viewHolder.huntName = (TextView) convertView.findViewById(R.id.huntNameGrid);
            result=convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.huntName.setText(huntNames[position]);
        return convertView;
    }

    private static class ViewHolder {
        //Button huntSelect;
        TextView huntName;
    }

}
