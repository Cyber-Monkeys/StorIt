package com.example.storit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerListAdapter extends BaseAdapter {

    //variables
    private Context mContext;
    private List<Server> mItems = new ArrayList<Server>();

    //Constructor
    public ServerListAdapter(Context context) {
        mContext = context;
    }

    public void add(Server item) {
        mItems.add(item);
        notifyDataSetChanged();
    }

    //return the size of Servers
    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public String getServerName(int pos){
        return  mItems.get(pos).getServerName();
    }

    public Double getStorageAmount(int pos){
        return  mItems.get(pos).getStorageAmount();
    }

    //Attaching to listView
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Server serverItem = mItems.get(position);

        View itemLayout = convertView;
        if (itemLayout == null)
            itemLayout = LayoutInflater.from(mContext).inflate(R.layout.server_list, parent, false);

        String name = serverItem.getServerName();

        final TextView tvName = (TextView) itemLayout.findViewById(R.id.serverName);
        tvName.setText(name);

        return itemLayout;

    }

}
