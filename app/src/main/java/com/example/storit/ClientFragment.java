package com.example.storit;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ClientFragment extends Fragment {

    //variables
    private static final String TAG = "AndroidClarified ----";
    EditText editText2; // for searching files or folders
    LinearLayout goBackLinearLayout;
    private static String documentPath, rootPath;
    static String fullDirectory=""; //static - no need to instatiate this class obj
    GridView gridView;
    TextView sortName;
    DocumentReference documentReference;
    private FirebaseFirestore db;
    FirebaseUser firebaseUser;
    FirebaseAuth mFirebaseAuth;
    String userId;
    private ArrayList<String> fileName = new ArrayList<String>();
    private ArrayList<String> tempFileName = new ArrayList<String>();
    static int fileImage = R.drawable.file_transparent2;
    static int folderImage = R.drawable.folder_transparent2;
    private ArrayList<Integer> image = new ArrayList<Integer>();
    public ClientAdapter clientAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        goBackLinearLayout = getView().findViewById(R.id.goBack);
        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = mFirebaseAuth.getCurrentUser();
        userId = firebaseUser.getUid();
        documentPath = "/Users/" + userId + "/TreeNode/" + userId;
        rootPath = "/Users/" + userId + "/TreeNode/" + userId;
        editText2 = getView().findViewById(R.id.editText2);
        sortName = getView().findViewById(R.id.sortName);
        gridView = getView().findViewById(R.id.gridView);
        //listener for searching files/folders
        editText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")){
                    loadCurrentDirectory();
                } else {
                    searchFileFolder(s.toString());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        clientAdapter = new ClientAdapter(getView().getContext(),fileName.toArray(new String[fileName.size()]), image.toArray(new Integer[image.size()]));
        gridView.setAdapter(clientAdapter);
        loadCurrentDirectory();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                if (fileName.get(arg2).contains("Folder")){ //arg2 is position
                    goBackLinearLayout.setVisibility(View.VISIBLE); //make the view visible
                    documentPath += "/" + fileName.get(arg2) + "/" + userId;
                    Log.d(TAG, "FAILURE " + documentPath);
                    loadCurrentDirectory();
                } else {
                    Log.d("Webrtcclient", "download request started");
                    ((Menu) getActivity()).downloadData(fileName.get(arg2));
                } // else statement end line

            }

        });

        sortName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SortBottomSheetDialog bottomSheetDialog = new SortBottomSheetDialog();
                bottomSheetDialog.show(getFragmentManager(), "SortBottomSheet");
            }
        });
        //onclick for going back to previous directory
        goBackLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCurrentDirectory();
                if (documentPath.equals(rootPath)){ //hide go back button
                    goBackLinearLayout.setVisibility(View.INVISIBLE);
                }
                loadCurrentDirectory();
            }
        });

    }
    //add file to an array
    //check if file or folder and add image
    public void addFile(String name) {
        fileName.add(name);
        if (name.contains("Folder")){
            image.add(folderImage);
        } else {
            image.add(fileImage);
        }
    }
    public void refreshAdapter() {
        clientAdapter = new ClientAdapter(getView().getContext(), fileName.toArray(new String[fileName.size()]),image.toArray(new Integer[image.size()]));
        gridView.setAdapter(clientAdapter);

    }

    public void loadDirectory() {
        fileName.clear(); image.clear();
        String[] files = fullDirectory.split(",");
        for (int i = 1; i < files.length; i++) {
            fileName.add(files[i]);
            if (files[i].contains("Folder")){
                image.add(folderImage);
            } else {
                image.add(fileImage);
            }
        }
        tempFileName = (ArrayList<String>) fileName.clone(); //get a copy of it
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshAdapter();
            }
        });
    }

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
    //go back to previous directory
    private void removeCurrentDirectory(){
        int countForBackslash = 0;
        for(int i = documentPath.length(); i > 0 ; i--){
            if (countForBackslash < 2){
                if (documentPath.charAt(documentPath.length()-1) == '/'){
                    countForBackslash++;
                }
                documentPath = documentPath.substring(0, documentPath.length() - 1);
            }

        }

    } // end of removeCurrentDirectory

    private void searchFileFolder(String fileFolderName){ //searching files or folders
        for (String fileFolder : tempFileName){
            if (!fileFolder.toLowerCase().contains(fileFolderName.toLowerCase())) {
                fileName.remove(fileFolder);
                image.remove(fileFolder);
            }
        }

        refreshAdapter();
    } //end of searchFileFolder

    //getters
    public String getCurrentDocumentPath(){
        return documentPath;
    }
    public String getFullDirectory(){
        return fullDirectory;
    }
}
