package com.example.storit;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.fragment.app.FragmentActivity;

public class ClientAdapter extends BaseAdapter {

    //variables
    ImageView moreOptions;
    private Context context;
    private LayoutInflater inflater = null;
    private ArrayList<Node> nodeList = new ArrayList<Node>();

    //constructor
    public ClientAdapter(Context context, ArrayList<Node> nodeList){
        this.context = context;
        this.nodeList = nodeList;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return nodeList.size();
    }

    @Override
    public Object getItem(int position) {
        return nodeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null){
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (convertView == null){
            convertView = inflater.inflate(R.layout.client_list, null);
        }

        ImageView image = convertView.findViewById(R.id.image);
        moreOptions = convertView.findViewById(R.id.moreOptions);
        TextView fileName = convertView.findViewById(R.id.fileName);


        if(nodeList.get(position).getIsFolder()) {
            image.setImageResource(R.drawable.folder_transparent2);
            moreOptions.setVisibility(View.INVISIBLE);
        } else {
            image.setImageResource(R.drawable.file_transparent);
        }
        fileName.setText(nodeList.get(position).getNodeName());

        //open more options
        moreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send data to more options fragment
                Bundle bundle = new Bundle();
                bundle.putParcelable("Node",nodeList.get(position));
                // set Arguments
                MoreOptionsBottomSheetDialog bottomSheetDialog = new MoreOptionsBottomSheetDialog();
                bottomSheetDialog.setArguments(bundle);
                bottomSheetDialog.show(((FragmentActivity)context).getSupportFragmentManager(), "AddNewBottomSheet");
            }
        });

        return convertView;
    }
}
