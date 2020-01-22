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
import com.google.firebase.firestore.util.FileUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class AddNewBottomSheetDialog extends BottomSheetDialogFragment {

    //variables
    ImageView addFolder, addSecureUpload, addUpload;
    private static final int REQUEST_CODE = 11;
    WebRtcClient client;

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
                if(client == null)
                    client = new WebRtcClient("https://www.vrpacman.com", (Menu)(AddNewBottomSheetDialog.this.getActivity()));
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE);
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//
//                // Provide read access to files and sub-directories in the user-selected
//                // directory.
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                // Optionally, specify a URI for the directory that should be opened in
//                // the system file picker when it loads.
//                //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);
//
//                startActivityForResult(intent, REQUEST_CODE);
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
        Log.d("STORIT---", "GOT TO ACTIVITY RESULT");
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Log.d("STORIT---", "pASSED RESULT AND REQUEST CODE");
            //check if data is null
            if(data != null){
                Log.d("STORIT---", "DATA IS NOT NULLst");
                Uri uri = data.getData();

                String hi = "";
                char[] charArr = new char[1];
                Log.d("STORIT---", uri.toString());
                //using input stream
                try {
//                    File file = new File(uri.toString());
//                    int fileSize = (int) file.length();
//                    FileInputStream fin = new FileInputStream(getActivity().getContentResolver().openFileDescriptor(uri, 'r'));
                    FileInputStream fin = (FileInputStream) getActivity().getContentResolver().openInputStream(uri);
                    //int fileSize = fin.getChannel().size();
                    //InputStream in = getActivity().getContentResolver().openInputStream(uri);
                    int fileSize = (int) fin.getChannel().size();
                    Log.d("WebRtcClient", "sending file of size " + fileSize);
                    //FileInputStream fin = new FileInputStream(uri.toString());

                    //BufferedInputStream bin=new BufferedInputStream(fin);
                    byte[] bytesArray = new byte[fileSize];
                    fin.read(bytesArray);
                    Log.d("WebRtcClient", "sending file of size " + fileSize);
//                    int i;
//                    while((i=bin.read())!=-1){
//                        charArr[i] = (char)i;
//                    }
//
//                    bin.close();
                    client.sendImage(fileSize, bytesArray, 0);
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
