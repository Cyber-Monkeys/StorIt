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

import androidx.fragment.app.FragmentActivity;

public class ClientAdapter extends BaseAdapter {

    //variables
    private Context context;
    private LayoutInflater inflater;
    private String[] fileNameArr;
    private Integer[] imageArr;

    //constructor
    public ClientAdapter(Context context, String[] fileName, Integer[] image){
        this.context = context;
        this.fileNameArr = fileName;
        this.imageArr = image;
    }

    @Override
    public int getCount() {
        return fileNameArr.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
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

        image.setImageResource(imageArr[position]);
        fileName.setText(fileNameArr[position]);

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
