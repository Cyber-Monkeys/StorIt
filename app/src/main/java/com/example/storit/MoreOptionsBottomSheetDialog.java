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
    ImageView nodeImage;
    TextView nodeName;
    LinearLayout shareButton, downloadButton, moveButton, duplicateButton, detailsButton, backupButton, removeButton;
    DocumentReference documentReference;
    private FirebaseFirestore db;
    FirebaseUser firebaseUser;
    FirebaseAuth mFirebaseAuth;
    String userId;
    Node node;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_more_options, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        node = getArguments().getParcelable("Node");

        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = mFirebaseAuth.getCurrentUser();
        userId = firebaseUser.getUid();
        nodeImage = getView().findViewById(R.id.fileFolderImage);
        nodeName = getView().findViewById(R.id.fileFolderName);
        shareButton = getView().findViewById(R.id.shareButton);
        downloadButton = getView().findViewById(R.id.downloadButton);
        moveButton = getView().findViewById(R.id.moveButton);
        duplicateButton = getView().findViewById(R.id.duplicateButton);
        detailsButton = getView().findViewById(R.id.detailsButton);
        backupButton = getView().findViewById(R.id.backupButton);
        removeButton = getView().findViewById(R.id.removeButton);

        //set icon and file/folder name
        nodeName.setText(node.getNodeName());
        if (!node.getIsFolder()){
            nodeImage.setImageResource(R.drawable.file_transparent);
        }

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "share Button" + node.getNodeName(), Toast.LENGTH_SHORT).show();
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
                ((Menu)getActivity()).removeFileFolder(node.getNodeName());
                MoreOptionsBottomSheetDialog.this.dismiss(); //close dialog
            }
        });

    }
}