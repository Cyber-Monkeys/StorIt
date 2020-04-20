package com.example.storit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MoreOptionsBottomSheetDialog extends BottomSheetDialogFragment {
    private static final String TAG = "AndroidClarified ----";
    private ClientFragment clientFragment = new ClientFragment();
    String documentPath = clientFragment.getCurrentDocumentPath();
    String fullDirectory;
    ArrayList<String> fileFolderArr = new ArrayList<>();
    String strFileFolderName;
    ImageView fileFolderImage;
    TextView fileFolderName;
    LinearLayout shareButton, downloadButton, moveButton, duplicateButton, detailsButton, backupButton, removeButton;
    DocumentReference documentReference;
    private FirebaseFirestore db;
    FirebaseUser firebaseUser;
    FirebaseAuth mFirebaseAuth;
    String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        strFileFolderName = getArguments().getString("fileFolderName"); //get data from clientAdapter
        View view = inflater.inflate(R.layout.bottom_sheet_more_options, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = mFirebaseAuth.getCurrentUser();
        userId = firebaseUser.getUid();
        fileFolderImage = getView().findViewById(R.id.fileFolderImage);
        fileFolderName = getView().findViewById(R.id.fileFolderName);
        shareButton = getView().findViewById(R.id.shareButton);
        downloadButton = getView().findViewById(R.id.downloadButton);
        moveButton = getView().findViewById(R.id.moveButton);
        duplicateButton = getView().findViewById(R.id.duplicateButton);
        detailsButton = getView().findViewById(R.id.detailsButton);
        backupButton = getView().findViewById(R.id.backupButton);
        removeButton = getView().findViewById(R.id.removeButton);

        loadCurrentDirectory(); //load directory

        //set icon and file/folder name
        fileFolderName.setText(strFileFolderName);
        if (!strFileFolderName.contains("Folder")){
            fileFolderImage.setImageResource(R.drawable.file_transparent);
        }

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "share Button" + strFileFolderName, Toast.LENGTH_SHORT).show();
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "download Button", Toast.LENGTH_SHORT).show();
            }
        });

        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "move Button", Toast.LENGTH_SHORT).show();
            }
        });

        duplicateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "duplicate Button", Toast.LENGTH_SHORT).show();
            }
        });

        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "details Button", Toast.LENGTH_SHORT).show();
            }
        });

        backupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "backup Button", Toast.LENGTH_SHORT).show();
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "remove Button", Toast.LENGTH_SHORT).show();
                removeFileFolder();
                MoreOptionsBottomSheetDialog.this.dismiss(); //close dialog
                //getActivity().finish(); //refresh
                startActivity(getActivity().getIntent());
                getActivity().overridePendingTransition(0, 0); //remove
            }
        });

    }
    //get directory from firebase and call load directory
    private void loadCurrentDirectory(){
        documentReference = db.document(documentPath);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    String directory = documentSnapshot.getString("dir");
                    if(directory != null) {
                        fullDirectory = directory;
                    }
                    loadDirectory();
                } else{
                    Toast.makeText(getContext(), "not exists", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "FAILURE " + e.getMessage());
            }
        });
    }
    //put each file/folder in an array
    private void loadDirectory(){
        String[] files = fullDirectory.split(",");
        for (int i = 1; i < files.length; i++) {
            fileFolderArr.add(files[i]);
        }
    }
    //remove file from dir in firebase
    private void removeFileFolder(){
        fileFolderArr.remove(strFileFolderName);
        fullDirectory = "";
        for (String fileFolder : fileFolderArr){
            fullDirectory += "," + fileFolder;
        }
        documentReference = db.document(documentPath);
        documentReference.update("dir", fullDirectory).addOnSuccessListener(new OnSuccessListener<Void>() {
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
    }
}