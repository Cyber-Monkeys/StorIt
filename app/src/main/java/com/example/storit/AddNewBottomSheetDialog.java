package com.example.storit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.util.FileUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

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
    public void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("STORIT---", "GOT TO ACTIVITY RESULT");
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Log.d("STORIT---", "pASSED RESULT AND REQUEST CODE");
            //check if data is null
            if(data != null){
                Log.d("STORIT---", "DATA IS NOT NULLst");
                Uri uri = data.getData();

                final String filePath = getFileName(uri);

                Log.d("STORIT---", "file path = " + filePath);
                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                String uniqueID = UUID.randomUUID().toString();
                mUser.getIdToken(true)
                        .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                if (task.isSuccessful()) {
                                    String idToken = task.getResult().getToken();
                                    ((Menu) getActivity()).uploadData(idToken, data, filePath);

                                } else {
                                    // Handle error -> task.getException();
                                    Log.d("res3", "no token verified");
                                }
                            }
                        });

            }

        }

    }
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = this.getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}



//
//    Uri uri = data.getData();
//
//    String hi = "";
//    char[] charArr = new char[1];
//                Log.d("STORIT---", uri.toString());
//                        //using input stream
//                        try {
////                    File file = new File(uri.toString());
////                    int fileSize = (int) file.length();
////                    FileInputStream fin = new FileInputStream(getActivity().getContentResolver().openFileDescriptor(uri, 'r'));
//                        // Let's say this bitmap is 300 x 600 pixels
//
//                        InputStream in = getActivity().getContentResolver().openInputStream(uri);
//                        Bitmap originalBm = BitmapFactory.decodeStream(in);
////                    Bitmap originalBm = BitmapFactory.decodeFile(uri.toString());
//                        Bitmap bm1 = Bitmap.createBitmap(originalBm, 0, 0, originalBm.getWidth(), (originalBm.getHeight() / 2));
//                        Bitmap bm2 = Bitmap.createBitmap(originalBm, 0, (originalBm.getHeight() / 2), originalBm.getWidth(), (originalBm.getHeight() / 2));
//                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                        bm1.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                        byte[] byteArray1 = stream.toByteArray();
//                        bm1.recycle();
//                        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
//                        bm2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
//                        byte[] byteArray2 = stream2.toByteArray();
//                        bm2.recycle();
//                        stream.close();
//                        stream2.close();
////                    FileInputStream fin = (FileInputStream) getActivity().getContentResolver().openInputStream(uri);
////                    //int fileSize = fin.getChannel().size();
////                    //InputStream in = getActivity().getContentResolver().openInputStream(uri);
////                    int fileSize = (int) fin.getChannel().size();
////                    Log.d("WebRtcClient", "sending file of size " + fileSize);
////                    //FileInputStream fin = new FileInputStream(uri.toString());
////
////                    //BufferedInputStream bin=new BufferedInputStream(fin);
////                    int size1 = fileSize / 2;
////                    int size2 = fileSize / 2;
////                    if(fileSize %2 == 1) {
////                        size2 += 1;
////                    }
////                    //size2 += 8;
////                    //byte[] bytesArray = new byte[fileSize];
////                    byte[] bytesArray1 = new byte[size1];
////                    byte[] bytesArray2 = new byte[size2 + 5000];
//////                    fin.read(bytesArray);
////                    fin.read(bytesArray1);
////
////                    for(int i = 0; i <5000;i++) {
////                        bytesArray2[i] = bytesArray1[i];
////                    }
////                    fin.read(bytesArray2);
////
//////                    fin.read(bytesArray2);
//////                    fin.read(bytesArray1, 0, size1);
//////                    fin.read(bytesArray2, 0, size2);
////
////                    Log.d("WebRtcClient", "sending file of size " + fileSize);
//////                    int i;
//////                    while((i=bin.read())!=-1){
//////                        charArr[i] = (char)i;
//////                    }
//////
//////                    bin.close();
////                    //client.sendImage(fileSize, bytesArray, 0);
//                        client.sendImage(byteArray1.length, byteArray1, 0);
//                        client.sendImage(byteArray2.length, byteArray2, 1);
////                    fin.close();
//                        in.close();
//
//
//                        } catch (Exception e) {
//                        e.printStackTrace();
//                        }