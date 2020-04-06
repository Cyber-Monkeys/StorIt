package com.example.storit;

import android.content.Context;
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
    private Context context;
    private LayoutInflater inflater = null;
    private ArrayList<File> fileList = new ArrayList<File>();

    //constructor
    public ClientAdapter(Context context, ArrayList<File> fileName){
        this.context = context;
        this.fileList = fileName;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileList.get(position);
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
        ImageView moreOptions = convertView.findViewById(R.id.moreOptions);
        TextView fileName = convertView.findViewById(R.id.fileName);

        image.setImageResource(fileList.get(position).getFileImage());
        fileName.setText(fileList.get(position).getFileName());

        //open more options
        moreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoreOptionsBottomSheetDialog bottomSheetDialog = new MoreOptionsBottomSheetDialog();
                bottomSheetDialog.show(((FragmentActivity)context).getSupportFragmentManager(), "AddNewBottomSheet");
            }
        });

        return convertView;
    }
}
