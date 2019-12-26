package com.example.storit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Menu extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //variables
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
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
                Toast.makeText(getApplicationContext(), "Hello", Toast.LENGTH_SHORT).show();
            }
        });

        //Set fragment to client first
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
                Toast.makeText(getApplicationContext(), "payment", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_plan:
                Toast.makeText(getApplicationContext(), "plans", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_help:
                Toast.makeText(getApplicationContext(), "help", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_terms_and_condition:
                Toast.makeText(getApplicationContext(), "terms & comds", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_faqs:
                Toast.makeText(getApplicationContext(), "logout", Toast.LENGTH_SHORT).show();
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
}
