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
    String userReference;
    GridView gridView;
    TextView sortName;
    FirebaseFirestore db;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser firebaseUser;
    String userId;
    DocumentReference documentReference;
//    ArrayList<File> tempFileName;



    private ArrayList<Node> nodeList = new ArrayList<Node>();
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
        documentPath = "/Users/" + userId + "/TreeNode/" + 1;
        rootPath = "/Users/" + userId + "/TreeNode/" + 1;
        userReference = "/Users/" + userId;
        //testDir = "/Users/" + userId + "/TreeNode/" + 1;
        documentReference = db.document(documentPath);
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
                refreshDirectory();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        clientAdapter = new ClientAdapter(getView().getContext(),nodeList);
        gridView.setAdapter(clientAdapter);

        loadCurrentDirectory();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                Log.d("Webrtcclient", "download request started");
                if (nodeList.get(arg2).getIsFolder()){ //arg2 is position
                    documentPath += "/" + nodeList.get(arg2).getNodeName() + "/" + userId;
                    loadCurrentDirectory();
                } else {
                    Log.d("Webrtcclient", "download request started");
                    ((Menu) getActivity()).downloadData(((File)nodeList.get(arg2)).getFileId());
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
        nodeList.add(addedFile);
//
//        String currentDocumentPath = getCurrentDocumentPath();
//        documentReference = db.document(currentDocumentPath);
//        //add folderName to dir string of current directory
//        String updatedFullDirectory = getFullDirectory() + "," + addedFile.getFileName();
//
//        documentReference.update("dir", updatedFullDirectory).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        //updateCurrentDirOfDevice(documentPath);
    }
    public void removeFile(String removeFile) {
        nodeList.removeIf(node -> (removeFile == node.getNodeName()));
    }
    public void refreshAdapter() {
        clientAdapter.notifyDataSetChanged();
//        clientAdapter = new ClientAdapter(getView().getContext(), fileList.toArray(new File[fileList.size()]),image.toArray(new Integer[image.size()]));
//        gridView.setAdapter(clientAdapter);

    }

    public void refreshDirectory() {
        //nodeList.clear();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshAdapter();
            }
        });
    }

    private void loadCurrentDirectory(){
        nodeList.clear();
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
                    List<Object> users = (List<Object>) documentSnapshot.get("directory");
                    //Log.d(TAG, users.get(0).toString() + " " + users.get(1).toString() + " " + users.size());
                    for(int i = 0; i < users.size(); i++){
                        Map<String, Object> user = (Map<String, Object>)users.get(i);
                        String name = user.get("nodeName").toString();
                        Boolean isFolder = (Boolean) user.get("isFolder");
                        if(isFolder) {
                            nodeList.add(new Folder(name));
                        } else {
                            String fileKey = user.get("fileKey").toString();
                            String type = user.get("fileType").toString();
                            int fileId = (int) user.get("fileId");
                            int fileSize = (int) user.get("fileSize");
                            nodeList.add(new File(fileId, name, fileSize, type, fileKey));
                        }
                    }

                    refreshDirectory();
                }  else{
                    Toast.makeText(getContext(), "not exists", Toast.LENGTH_SHORT).show();
                    documentReference = db.document(documentPath);
                    ArrayList<Node> arrFile = new ArrayList<Node>();
                    Map<String, Object> docData = new HashMap<>();
                    docData.put("directory", arrFile);

                    documentReference.set(docData).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        refreshDirectory();
    } // end of removeCurrentDirectory
    private void searchFileFolder(String fileFolderName){ //searching files or folders
        nodeList.removeIf(f -> !f.getNodeName().toLowerCase().contains(fileFolderName.toLowerCase()));
        refreshAdapter();
    } //end of searchFileFolder



    //getters
    public String getCurrentDocumentPath(){
        return documentPath;
    }
    public ArrayList<Node> getNodeList(){
        return nodeList;
    }

}
