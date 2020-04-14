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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import org.json.JSONArray;
import org.json.JSONObject;

public class ClientFragment extends Fragment {

    //variables
    private static final String TAG = "AndroidClarified ----";
    EditText editText2; // for searching files or folders
    LinearLayout goBackLinearLayout;
    private static String documentPath, rootPath;
    static String fullDirectory=""; //static - no need to instatiate this class obj
    String userReference, testDir;
    GridView gridView;
    TextView sortName;
    FirebaseFirestore db;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser firebaseUser;
    String userId;
    DocumentReference documentReference;
//    ArrayList<File> tempFileName;



    private ArrayList<File> fileList = new ArrayList<File>();
    private ClientAdapter clientAdapter;

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
        userReference = "/Users/" + userId;
        testDir = "/Users/" + userId + "/TreeNode/" + 1;
        documentReference = db.document(testDir);
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
                    loadDirectory();
                    searchFileFolder(s.toString());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        clientAdapter = new ClientAdapter(getView().getContext(),fileList);
        gridView.setAdapter(clientAdapter);
        loadCurrentDirOfDevice(); //load data to collection view
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                Log.d("Webrtcclient", "download request started");
                ((Menu) getActivity()).downloadData(fileList.get(arg2).getFileId());
                if (fileList.get(arg2).getFileName().contains("Folder")){ //arg2 is position
                    documentPath += "/" + fileList.get(arg2).getFileName() + "/" + userId;
                    updateCurrentDirOfDevice(documentPath); //update dir to firebase
                    loadCurrentDirectory();
                } else {
                    Log.d("Webrtcclient", "download request started");
                    ((Menu) getActivity()).downloadData(fileList.get(arg2).getFileId());
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
                loadCurrentDirectory();
            }
        });

//        testingFunction();
//        getDataTesting();

    }
    public void addFile(File addedFile) {
        fileList.add(addedFile);
    }
    public void refreshAdapter() {
        clientAdapter.notifyDataSetChanged();
//        clientAdapter = new ClientAdapter(getView().getContext(), fileList.toArray(new File[fileList.size()]),image.toArray(new Integer[image.size()]));
//        gridView.setAdapter(clientAdapter);

    }

    public void loadDirectory() {
        fileList.clear();
        String[] files = fullDirectory.split(",");
        for (int i = 1; i < files.length; i++) {
//            fileName.add(files[i]);
            if (files[i].contains("Folder")){
                fileList.add(new File(files[i],true));
//                image.add(folderImage);
            } else {
                fileList.add(new File(files[i],false));
//                image.add(fileImage);
            }
        }
//        tempFileName = (ArrayList<String>) fileList.clone(); //get a copy of it
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshAdapter();
            }
        });
    }

    private void loadCurrentDirectory(){
        if (documentPath.equals(rootPath)){ //hide go back button
            goBackLinearLayout.setVisibility(View.INVISIBLE);
        } else {
            goBackLinearLayout.setVisibility(View.VISIBLE); //make the view visible
        }
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
                    documentReference = db.document(documentPath);
                    Map<String, Object> user = new HashMap<>();
                    user.put("dir", ",");

                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "dir created" );

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure" + e.getMessage());
                        }
                    });
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
        updateCurrentDirOfDevice(documentPath); //update dir to firebase
    } // end of removeCurrentDirectory
    private void searchFileFolder(String fileFolderName){ //searching files or folders
        fileList.removeIf(f -> !f.getFileName().toLowerCase().contains(fileFolderName.toLowerCase()));
        refreshAdapter();
    } //end of searchFileFolder

    //put current directory of device and send to firebase
    //will be used so that it updated curr dir when logged in to another device
    private void loadCurrentDirOfDevice(){
        documentReference = db.document(userReference);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    String dirOfDevice = documentSnapshot.getString("currDirOfDevice");
                    if(dirOfDevice != null) {
                        documentPath = dirOfDevice;
                        loadCurrentDirectory();
                    } else { //if it is null, create and send to firebase
                        Toast.makeText(getContext(), "create dir", Toast.LENGTH_SHORT).show();
                        documentReference.update("currDirOfDevice", rootPath).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Directory of device updated" );
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure" + e.getMessage());
                            }
                        });
                        loadCurrentDirectory();
                    }

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "FAILURE " + e.getMessage());
            }
        });
    }

    //update dir of device to firebase
    //this is for to a new folder or going back
    private void updateCurrentDirOfDevice(String currentDir){
        documentReference = db.document(userReference);
        documentReference.update("currDirOfDevice", currentDir).addOnSuccessListener(new OnSuccessListener<Void>() {
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
    //test
//    private void testingFunction(){
//        File file = new File("ddasd.txt", "text");
//        File file2 = new File("ddasd.txt", "folder");
//        ArrayList<File> arrFile = new ArrayList<>();
//        arrFile.add(file); arrFile.add(file2);
//
//        Map<String, Object> docData = new HashMap<>();
//        docData.put("testingFile", arrFile);
//        documentReference = db.document(testDir);
//        documentReference.set(docData).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Log.d(TAG, "Directory updated" );
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.d(TAG, "onFailure" + e.getMessage());
//            }
//        });
//    }
//    //test
//    private void getDataTesting(){
//        documentReference = db.document(testDir);
//        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                if(documentSnapshot.exists()){
//                    List<Map<String, Object>> users = (List<Map<String, Object>>) documentSnapshot.get("testingFile");
//                    Log.d(TAG, users.get(0).toString() + " " + users.get(1).toString() + " " + users.size());
//                    for(int i = 0; i < users.size(); i++){
//                        String name = users.get(i).get("name").toString();
//                        String type = users.get(i).get("type").toString();
//                        Log.d(TAG, name + " " + type);
//                    }
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.d(TAG, "FAILURE " + e.getMessage());
//            }
//        });
//    }
    //getters
    public String getCurrentDocumentPath(){
        return documentPath;
    }
    public String getFullDirectory(){
        return fullDirectory;
    }
}
