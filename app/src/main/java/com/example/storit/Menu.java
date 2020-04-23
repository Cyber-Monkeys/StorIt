package com.example.storit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.Math.toIntExact;

public class Menu extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //variables
    private static final int CLIENT = 0;
    private static final int SERVER = 1;
    int index;

    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    TextView headerEmail, headerName, toolbarTitle;
    ImageView headerImage;
    BottomNavigationView bottomNavigationMenu;
    FloatingActionButton fab;
    private static final String TAG = "AndroidClarified ----";
    FirebaseAuth mFirebaseAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleSignInClient mGoogleSignInClient;
    ServerFragment serverFragment;
    //Fragment serverFragment;

    DocumentReference documentReference;
    String userId;
    private FirebaseFirestore db;
    FirebaseUser firebaseUser;
    AlertDialog dialog;
    TextView seekBarText;
    SeekBar setSeekbar;


    WebRtcClient client = null;
    static  String deviceId;
    ClientFragment clientFragment;
    String fullDirectory = "";
    String fileUploading = "";
    AddNewBottomSheetDialog bottomSheetDialog;
    View layout;
    Handler myHandler;
    User currentUser;


    static String sharedPrefDbName = "STORITDB";

    //onCreate function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //set variables
        navigationView = findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);
        headerEmail = (TextView) hView.findViewById(R.id.headerEmail); //initialize headerEmail from navView
        headerName = (TextView) hView.findViewById(R.id.headerName); //initialize headerName from navView
        headerImage = (ImageView) hView.findViewById(R.id.circular_image); //initialize headerProfile from navView
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);


        setSupportActionBar(toolbar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = mFirebaseAuth.getCurrentUser();
        if(firebaseUser != null){
            userId = firebaseUser.getUid();
            documentReference = db.collection("Users").document(userId);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Auth State Listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mFirebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(Menu.this, Login.class));
                }
            }
        };
        createProfileInfo(); //check if profile info does not exist

        //header of nav drawer
        hView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Menu.this, Profile.class));
            }
        });
        headerImage.setOnClickListener(new View.OnClickListener() { // go to profile class
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Menu.this, Profile.class));
            }
        });
        //Setting header email and username from database
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    String username = documentSnapshot.getString("Username");
                    String name = documentSnapshot.getString("Name");
                    String email = documentSnapshot.getString("Email");
                    Date birthDate = documentSnapshot.getDate("Birthdate");
                    String region = documentSnapshot.getString("Region");
                    Map<String, Object> planData = (Map<String, Object>) documentSnapshot.get("plan");
                    int planId = toIntExact((long) planData.get("planId"));
                    ArrayList<String> planRegions = (ArrayList<String>) planData.get("planRegions");
                    Timestamp dateInTime = (Timestamp) planData.get("planRenewalDate");
                    Date planRenewalDate = dateInTime.toDate();
                    Plan plan = new Plan(planId, planRegions, planRenewalDate);
                    currentUser = new User(username, email, name, birthDate, region, plan);
//                    String directory = documentSnapshot.getString("directory");
//                    if(directory != null) {
//                        fullDirectory = directory;
//                    }
//                    loadDirectory();
                    if (username == null){
                        headerName.setText("Username");
                    }else{
                        headerName.setText(currentUser.getUsername());
                    }

                    headerEmail.setText(currentUser.getEmail());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "FAILURE " + e.getMessage());
            }
        });

        //setting profile photo from firebase
        if(firebaseUser.getPhotoUrl() != null){
            //dependency used
            Glide.with(this)
                    .load(firebaseUser.getPhotoUrl())
                    .into(headerImage);
        }

        //bottom navigation view
        bottomNavigationMenu = findViewById(R.id.bottom_navigation);
        bottomNavigationMenu.clearAnimation();
        bottomNavigationMenu.setOnNavigationItemSelectedListener(navListener);

        //nav drawer
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        //floating action bar = "+" button
        fab =(FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog = new AddNewBottomSheetDialog();
                if (index == CLIENT)
                    bottomSheetDialog.show(getSupportFragmentManager(), "AddNewBottomSheet");
                else if (index == SERVER)
                    addServerDialog();
            }
        });

        myHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Bitmap bmp = (Bitmap) msg.obj;
                showImage(bmp);
            }
        };
        //Set fragment to client first
        index = CLIENT;
        clientFragment = new ClientFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                clientFragment, "MY_FRAGMENT_CLIENT").commit();
        getSupportActionBar().setTitle("Client");
        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title); //set toolbar title
        toolbarTitle.setText(toolbar.getTitle());
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        navigationView.setCheckedItem(R.id.action_client);

    }

    //onStart function
    protected void onStart(){
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    public void uploadData(String idToken,Intent data, String fileName) {
        Context context = this;
        if(client == null)
            client = new WebRtcClient("https://www.vrpacman.com", (Menu) context, "client");
        client.emitUpload(idToken,"serverUpload", data);
        bottomSheetDialog.dismiss();
        this.fileUploading = fileName;
    }
    public  void downloadData(int fileId) {
        final Context context = this;
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        String uniqueID = UUID.randomUUID().toString();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();
                            Log.d("webrtcclient", "token verified and request begins");
                            if(client == null)
                                client = new WebRtcClient("https://www.vrpacman.com", (Menu) context, "client");
                            client.emitDownload(idToken,"serverDownload", fileId);

                        } else {
                            // Handle error -> task.getException();
                            Log.d("res3", "no token verified");
                        }
                    }
                });
    }


    //sign out account
    private void signOut(){
        mFirebaseAuth.signOut();
        mGoogleSignInClient.signOut();
    }

//    public void loadDirectory() {
//        String[] files = fullDirectory.split(",");
//        for (int i = 1; i < files.length; i++) {
//            File f = new File(0, 0, files[i], ".txt", false);
//            this.clientFragment.addFile(f);
//        }
//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                clientFragment.refreshAdapter();
//            }
//        });
//    }
    public void addFile(File addedFile) {
        Log.d(TAG, "adding new file");
        this.clientFragment.addFile(addedFile);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                clientFragment.addFile(addedFile);
                clientFragment.refreshAdapter();
            }
        });
    }
    public void removeFileFolder(String file) {
        Log.d(TAG, "adding new file");
        this.clientFragment.removeFile(file);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                clientFragment.addFile(addedFile);
                clientFragment.refreshAdapter();
            }
        });
    }
    public void doneUploading(int fileId, int fileSize, String fileType, String fileKey) {
        File addedFile = new File(fileId, fileUploading, fileSize, fileType, fileKey);
        addFile(addedFile);
    }

    //navigation drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_plan:
                // pass the plan
                Intent i = new Intent(this, Plans.class);
                i.putExtra("planId",currentUser.getPlan().getPlanId());
                startActivity(i);
                break;
            case R.id.nav_help:
                startActivity(new Intent(this, Help.class));
                break;
            case R.id.nav_terms_and_condition:
                startActivity(new Intent(this, TermsAndCondition.class));
                break;
            case R.id.nav_faqs:
                startActivity(new Intent(this, FAQs.class));
                break;
            case R.id.nav_logout:
                signOut();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //bottom navigation view
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    switch(menuItem.getItemId()){
                        case R.id.action_client:
                            index = CLIENT;
                            getSupportActionBar().setDisplayShowTitleEnabled(true);
                            getSupportActionBar().setTitle("Client");
                            toolbarTitle.setText(toolbar.getTitle());
                            getSupportActionBar().setDisplayShowTitleEnabled(false);
                            //show client page
                            if(getSupportFragmentManager().findFragmentByTag("MY_FRAGMENT_CLIENT") != null) {
                                //if the fragment exists, show it.
                                getSupportFragmentManager().beginTransaction().show(getSupportFragmentManager()
                                        .findFragmentByTag("MY_FRAGMENT_CLIENT")).commit();
                            }
                            //hide server page
                            if(getSupportFragmentManager().findFragmentByTag("MY_FRAGMENT_SERVER") != null){
                                //if the other fragment is visible, hide it.
                                getSupportFragmentManager().beginTransaction().hide(getSupportFragmentManager()
                                        .findFragmentByTag("MY_FRAGMENT_SERVER")).commit();
                            }
                            break;
                        case R.id.action_server:
                            index = SERVER;
                            getSupportActionBar().setDisplayShowTitleEnabled(true);
                            getSupportActionBar().setTitle("Server");
                            toolbarTitle.setText(toolbar.getTitle());
                            getSupportActionBar().setDisplayShowTitleEnabled(false);
                            //show server page
                            if(getSupportFragmentManager().findFragmentByTag("MY_FRAGMENT_SERVER") != null) {
                                //if the fragment exists, show it.
                                getSupportFragmentManager().beginTransaction().show(getSupportFragmentManager()
                                        .findFragmentByTag("MY_FRAGMENT_SERVER")).commit();
                            }else{
                                serverFragment = new ServerFragment();
                                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                                        serverFragment, "MY_FRAGMENT_SERVER").commit();
                            }
                            //hide client page
                            if(getSupportFragmentManager().findFragmentByTag("MY_FRAGMENT_CLIENT") != null){
                                //if the other fragment is visible, hide it.
                                getSupportFragmentManager().beginTransaction().hide(getSupportFragmentManager()
                                        .findFragmentByTag("MY_FRAGMENT_CLIENT")).commit();
                            }
                            break;
                    }

                    return true;
                }
            };

    private void addServerDialog() {
        //Create alertDialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Custom title
        TextView title = new TextView(this);
        title.setText("Add Server");
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);
        builder.setCustomTitle(title);

        //Create a custom layout for the dialog box
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_add_server_dialog, null);

        builder.setView(layout);

        //Set seekbar amount of storage
        setSeekbar = (SeekBar)layout.findViewById(R.id.seekBar);
        seekBarText = (TextView)layout.findViewById(R.id.textViewSeekBar);
        setSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress;
                seekBarText.setText(progress + " MB");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button closeButton = (Button)layout.findViewById(R.id.cancelButton);
        Button addButton = (Button)layout.findViewById(R.id.addServer);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

//        addButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "HELLO", Toast.LENGTH_SHORT).show();
//                // start server by connecting to sockets and emitting server event
//
//            }
//        });

        //show dialog
        dialog = builder.create();
        dialog.show();

    }

    public void showImage(Bitmap bmp) {
        //Create alertDialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Custom title
//        TextView title = new TextView(this);
//        title.setText("Add Server");
//        title.setPadding(10, 10, 10, 10);
//        title.setGravity(Gravity.CENTER);
//        title.setTextColor(Color.BLACK);
//        title.setTextSize(20);
//        builder.setCustomTitle(title);

        //Create a custom layout for the dialog box
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        layout = inflater.inflate(R.layout.custom_image_dialog, null);

        builder.setView(layout);

        ImageView imgView = (ImageView) layout.findViewById(R.id.dialogImage);
        imgView.setImageBitmap(bmp);


        Button closeButton = (Button)layout.findViewById(R.id.closeDialog);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

//        addButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "HELLO", Toast.LENGTH_SHORT).show();
//                // start server by connecting to sockets and emitting server event
//
//            }
//        });

        //show dialog
        dialog = builder.create();
        dialog.show();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (client != null) {
            client.onDestroy();
        }
    }
    public void addServer(View V) {
        // you need to change this later so that you verify if the current device is already running a server?
        final int storageSize = setSeekbar.getProgress();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        final Context context = this;
        final String uniqueID = getDeviceId();
        if(!isServiceRunning("com.example.storit.ServerService")) {
            serverFragment.addNewServer();
            // start service
            Intent intent = new Intent(this, ServerService.class);
            intent.putExtra("storageSize", storageSize);
            intent.putExtra("devId", uniqueID);

            startService(intent);
        } else {
            // stop service
            Intent intent = new Intent(this, ServerService.class);
            stopService(intent);
        }

        dialog.dismiss();
    }
    private boolean isServiceRunning(String serviceName){
        boolean serviceRunning = false;
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);
        Iterator<ActivityManager.RunningServiceInfo> i = l.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningServiceInfo runningServiceInfo = i
                    .next();
            Log.d(TAG, runningServiceInfo.service.getClassName());
            if(runningServiceInfo.service.getClassName().equals(serviceName)){
                serviceRunning = true;

                if(runningServiceInfo.foreground)
                {
                    //service run in foreground
                }
            }
        }
        return serviceRunning;
    }
    public String getDeviceId() {
        //if the deviceId is not null, return it
        if(deviceId != null){
            return deviceId;
        }//end

        //shared preferences
        SharedPreferences sharedPref = this.getSharedPreferences(sharedPrefDbName,this.MODE_PRIVATE);

        //lets get the device Id
        deviceId = sharedPref.getString("device_id",null);

        //if the saved device Id is null, lets create it and save it

        if(deviceId == null) {

            //generate new device id
            deviceId = UUID.randomUUID().toString();

            //Shared Preference editor
            SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();

            //save the device id
            sharedPrefEditor.putString("device_id",deviceId);

            //commit it
            sharedPrefEditor.commit();
        }//end if device id was null

        //return
        return deviceId;
    }

    public void createProfileInfo(){ //if user profile does not exist (for gmail users)
        documentReference = db.collection("Users").document(userId);
        //Setting attributes from database
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(!documentSnapshot.exists()){
                    User currentUser = new User(mFirebaseAuth.getCurrentUser().getDisplayName(),
                            mFirebaseAuth.getCurrentUser().getEmail(),
                            mFirebaseAuth.getCurrentUser().getDisplayName()
                            , new Date(), "EU");
                    Plan userPlan = new Plan(1);
                    currentUser.setPlan(userPlan);
                    Map<String, Object> user = new HashMap<>();
                    user.put("Username", currentUser.getUsername());
                    user.put("Name", currentUser.getName());
                    user.put("Email", currentUser.getEmail());
                    user.put("Birthdate", currentUser.getDateOfBirth());
                    user.put("Region", currentUser.getRegion());
                    Map<String, Object> planData = new HashMap<>();
                    planData.put("planId", currentUser.getPlan().getPlanId());
                    planData.put("planStorage", currentUser.getPlan().getPlanCopies());
                    planData.put("planCopies", currentUser.getPlan().getPlanCopies());
                    planData.put("planRegions", currentUser.getPlan().getPlanRegions());
                    planData.put("planRenewalDate", currentUser.getPlan().getRenewalDate());
                    planData.put("planCost", currentUser.getPlan().getPlanCost());
                    user.put("plan", planData);
                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "user profile created");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure" + e.getMessage());
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Void>() { // once completed update nav bar header
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()){
                                        String email = documentSnapshot.getString("Email");
                                        String username = documentSnapshot.getString("Username");
                                        if (username == null){
                                            headerName.setText("Username");
                                        }else{
                                            headerName.setText(username);
                                        }
                                        headerEmail.setText(email);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "FAILURE " + e.getMessage());
                                }
                            });
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
}
