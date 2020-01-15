package com.example.storit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Menu extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //variables
    private static final int CLIENT = 0;
    private static final int SERVER = 1;
    int index;

    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    TextView headerEmail, headerName;
    BottomNavigationView bottomNavigationMenu;
    FloatingActionButton fab;
    private static final String TAG = "AndroidClarified ----";
    FirebaseAuth mFirebaseAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleSignInClient mGoogleSignInClient;

    DocumentReference documentReference;
    String userId;
    private FirebaseFirestore db;
    FirebaseUser firebaseUser;

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

        //header of nav drawer
        hView.setOnClickListener(new View.OnClickListener() {
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
                    String email = documentSnapshot.getString("Email");
                    String name = documentSnapshot.getString("Name");

                    if (name == null){
                        headerName.setText("Username");
                    }else{
                        headerName.setText(name);
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
                if (index == CLIENT)
                    Toast.makeText(getApplicationContext(), "client", Toast.LENGTH_SHORT).show();
                else if (index == SERVER)
                    addServerDialog();
            }
        });

        //Set fragment to client first
        index = CLIENT;
        Fragment clientFragment = new ClientFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                clientFragment, "MY_FRAGMENT_CLIENT").commit();
        getSupportActionBar().setTitle("Client");
        navigationView.setCheckedItem(R.id.action_client);

    }

    //onStart function
    protected void onStart(){
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    //sign out account
    private void signOut(){
        mFirebaseAuth.signOut();
        mGoogleSignInClient.signOut();
    }

    //navigation drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_payment_details:
                startActivity(new Intent(this, PaymentDetails.class));
                break;
            case R.id.nav_plan:
                startActivity(new Intent(this, Plans.class));
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
                            getSupportActionBar().setTitle("Client");
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
                            getSupportActionBar().setTitle("Server");
                            //show server page
                            if(getSupportFragmentManager().findFragmentByTag("MY_FRAGMENT_SERVER") != null) {
                                //if the fragment exists, show it.
                                getSupportFragmentManager().beginTransaction().show(getSupportFragmentManager()
                                        .findFragmentByTag("MY_FRAGMENT_SERVER")).commit();
                            }else{
                                Fragment serverFragment = new ServerFragment();
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
        final AlertDialog dialog = builder.create();
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
        SeekBar setSeekbar = (SeekBar)layout.findViewById(R.id.seekBar);
        final TextView seekBarText = (TextView)layout.findViewById(R.id.textViewSeekBar);
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

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "HELLO", Toast.LENGTH_SHORT).show();
            }
        });

        //show dialog
        builder.create().show();
    }
}
