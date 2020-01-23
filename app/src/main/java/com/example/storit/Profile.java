package com.example.storit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;

public class Profile extends AppCompatActivity {

    //Variables
    private static final String TAG = "AndroidClarified ----";
    Toolbar toolbar;
    TextView changePhoto, textName, textUsername, textEmail, textBirthdate, toolbarTitle;
    Button editProfile;
    Calendar mCalendarDate;
    DocumentReference documentReference;
    String userId;
    private FirebaseFirestore db;
    FirebaseUser firebaseUser;
    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //initialize
        mCalendarDate = Calendar.getInstance();
        changePhoto = (TextView) findViewById(R.id.changePhoto);
        textName = (TextView) findViewById(R.id.textName);
        textUsername = (TextView) findViewById(R.id.textUsername);
        textEmail = (TextView) findViewById(R.id.textEmail);
        textBirthdate = (TextView) findViewById(R.id.textBirthdate);
        editProfile = (Button) findViewById(R.id.editProfile);

        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = mFirebaseAuth.getCurrentUser();
        if(firebaseUser != null){
            userId = firebaseUser.getUid();
            documentReference = db.collection("Users").document(userId);
        }

        //Toolbar for this page
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        toolbarTitle.setText(toolbar.getTitle());
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        final Drawable goBack = getResources().getDrawable(R.drawable.ic_go_back);
        goBack.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(goBack);
        //go back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, Menu.class));
            }
        });

        //Setting attributes from database
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    String email = documentSnapshot.getString("Email");
                    String name = documentSnapshot.getString("Name");
                    String username = documentSnapshot.getString("Username");
                    Date birthdate = documentSnapshot.getDate("Birthdate");

                    textName.setText(name);
                    textUsername.setText(username);

                    //convert date to calendar to string
                    mCalendarDate.setTime(birthdate);
                    int year = mCalendarDate.get(Calendar.YEAR);
                    int month = mCalendarDate.get(Calendar.MONTH);
                    int day = mCalendarDate.get(Calendar.DAY_OF_MONTH);
                    textBirthdate.setText(day + "/" + (month+1) + "/" +year);

                    textEmail.setText(email);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "FAILURE " + e.getMessage());
            }
        });

        //go to Edit Profile page
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = textEmail.getText().toString();
                String name = textName.getText().toString();
                String birthdate = textBirthdate.getText().toString();
                String username  = textUsername.getText().toString();

                Intent i = new Intent(Profile.this, EditProfile.class);
                i.putExtra("Email", email);
                i.putExtra("Name", name);
                i.putExtra("Birthdate", birthdate);
                i.putExtra("Username", username);
                startActivity(i);
            }
        });

    }
}
