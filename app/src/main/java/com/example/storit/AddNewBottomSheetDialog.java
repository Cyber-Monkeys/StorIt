package com.example.storit;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class AddNewBottomSheetDialog extends BottomSheetDialogFragment {

    //variables
    ImageView addFolder, addSecureUpload, addUpload;
    private static final int REQUEST_CODE = 11;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_new, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addFolder = (ImageView) getView().findViewById(R.id.circular_folder);
        addSecureUpload = (ImageView) getView().findViewById(R.id.circular_secure_upload);
        addUpload = (ImageView) getView().findViewById(R.id.circular_upload);

        addFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "FOLDER", Toast.LENGTH_SHORT).show();
            }
        });

        addSecureUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open document
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivity(intent);
            }
        });

        addUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open document
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){

            //check if data is null
            if(data != null){
                Uri uri = data.getData();

                String hi = "";
                char[] charArr = new char[1];
                Log.d("STORIT---", uri.toString());
                //using input stream
                try {
                    FileInputStream fin = new FileInputStream(uri.toString());
                    BufferedInputStream bin=new BufferedInputStream(fin);
                    int i;
                    while((i=bin.read())!=-1){
                        charArr[i] = (char)i;
                    }

                    bin.close();
                    fin.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String newCharString = new String(charArr);
                Log.d("STORIT---", newCharString);

            }

        }

    }
}
