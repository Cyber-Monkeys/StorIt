package com.example.storit;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.util.FileUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddNewBottomSheetDialog extends BottomSheetDialogFragment {

    //variables
    private static final String TAG = "AndroidClarified ----";
    DocumentReference documentReference;
    private FirebaseFirestore db;
    FirebaseUser firebaseUser;
    FirebaseAuth mFirebaseAuth;
    String userId;
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

        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = mFirebaseAuth.getCurrentUser();
        userId = firebaseUser.getUid();
        addFolder = (ImageView) getView().findViewById(R.id.circular_folder);
        addSecureUpload = (ImageView) getView().findViewById(R.id.circular_secure_upload);
        addUpload = (ImageView) getView().findViewById(R.id.circular_upload);

        addFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAddFolderDialog(); //display create folder dialog
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

    //set dialog add folder
    private void showAddFolderDialog(){
        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create a New Folder");

        //set linear layout
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setFocusableInTouchMode(true);

        //views to set in dialog

        final EditText mFolderText = new EditText(getContext());
        mFolderText.setHint("Folder Name");
        mFolderText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mFolderText.setMinEms(10);

        linearLayout.addView(mFolderText);
        linearLayout.setPadding(50,10,10,10);

        builder.setView(linearLayout);

        //buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //when you create a folder, set a field "dir"
                String folderName = mFolderText.getText().toString().trim();
                String currentDocumentPath = ((Menu)getActivity()).clientFragment.getCurrentDocumentPath();
                String newDocumentPath = currentDocumentPath + "/" + folderName + "/" + userId;
                documentReference = db.document(newDocumentPath);
                ArrayList<Node> arrFile = new ArrayList<Node>();
                Map<String, Object> docData = new HashMap<>();
                docData.put("directory", arrFile);

                documentReference.set(docData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Folder added" );

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure" + e.getMessage());
                    }
                });

                documentReference = db.document(currentDocumentPath);
                ArrayList<Node> nodeList = ((Menu)getActivity()).clientFragment.getNodeList();
                nodeList.add(new Folder(folderName));
                 documentReference.update("directory", nodeList).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Directory updated" );
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure" + e.getMessage());
                    }
                });
                ((Menu)getActivity()).clientFragment.refreshAdapter();
                AddNewBottomSheetDialog.this.dismiss(); //close dialog

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Cancel dialog
                dialog.dismiss();
            }
        });

        //show dialog
        builder.create().show();

    }
} // end of main class

