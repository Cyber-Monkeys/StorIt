package com.example.storit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.toIntExact;

public class Profile extends AppCompatActivity {

    //Variables
    private static final String TAG = "AndroidClarified ----";
    private Toolbar toolbar;
    private TextView changePhoto, textName, textUsername, textEmail, textBirthdate, toolbarTitle, planIdText, planStorageText, planCopiesText, planCostText, planRenewalDateText;
    private ImageView circularImage;
    private Button editProfile;
    private Calendar mCalendarDate;
    private DocumentReference documentReference;
    private String userId;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private LinearLayout regionsLayout;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //initialize
        mCalendarDate = Calendar.getInstance();
        circularImage = (ImageView) findViewById(R.id.circular_image);
        changePhoto = (TextView) findViewById(R.id.changePhoto);
        textName = (TextView) findViewById(R.id.textName);
        textUsername = (TextView) findViewById(R.id.textUsername);
        textEmail = (TextView) findViewById(R.id.textEmail);
        textBirthdate = (TextView) findViewById(R.id.textBirthdate);
        editProfile = (Button) findViewById(R.id.editProfile);
        planIdText = (TextView) findViewById(R.id.planId);
        planCopiesText = (TextView) findViewById(R.id.planCopies);
        planStorageText = (TextView) findViewById(R.id.planStorage);
        planRenewalDateText = (TextView) findViewById(R.id.planRenewalDate);
        planCostText = (TextView) findViewById(R.id.planCost);
        regionsLayout = (LinearLayout)findViewById(R.id.regionsLayout);
        Context context = this;


        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = mFirebaseAuth.getCurrentUser();
        if(firebaseUser != null){
            userId = firebaseUser.getUid();
            documentReference = db.collection("Users").document(userId);
        }

        //setting profile photo from firebase
        if(firebaseUser.getPhotoUrl() != null){
            //dependency used
            Glide.with(this)
                    .load(firebaseUser.getPhotoUrl())
                    .into(circularImage);
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
                    currentUser = new User(username, name, email, birthDate, region, plan);

                    textName.setText(currentUser.getName());
                    textUsername.setText(currentUser.getUsername());
                    Calendar tempCalendar = Calendar.getInstance();
                    tempCalendar.setTime(currentUser.getDateOfBirth());
                    int year = tempCalendar.get(Calendar.YEAR);
                    int month = tempCalendar.get(Calendar.MONTH);
                    int day = tempCalendar.get(Calendar.DAY_OF_MONTH);
                    textBirthdate.setText(day+"/"+(month+1)+"/"+year);
                    textEmail.setText(currentUser.getEmail());
                    planIdText.setText("Plan " + currentUser.getPlan().getPlanId());
                    planCopiesText.setText(currentUser.getPlan().getPlanCopies() + " copies");
                    planStorageText.setText(currentUser.getPlan().getPlanStorage() + " GB");
                    Calendar renewalDate = Calendar.getInstance();
                    renewalDate.setTime(currentUser.getPlan().getRenewalDate());
                    year = tempCalendar.get(Calendar.YEAR);
                    month = tempCalendar.get(Calendar.MONTH);
                    day = tempCalendar.get(Calendar.DAY_OF_MONTH);
                    planRenewalDateText.setText("RenewalDate: " +day+"/"+(month+1)+"/"+year);
                    planCostText.setText("$ " + currentUser.getPlan().getPlanCost());
                    regionsLayout.setWeightSum(planRegions.size() * 2);
                    for(int j = 0; j < planRegions.size();j++) {
                        TextView regionHeaderText = new TextView(context);
                        regionHeaderText.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        regionHeaderText.setGravity(0);
                        regionHeaderText.setText("Region " + (j + 1));
                        regionHeaderText.setTextSize(20);
                        TextView regionText = new TextView(context);
                        regionText.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        regionText.setText(currentUser.getPlan().getPlanRegions().get(j));
                        regionsLayout.addView(regionHeaderText);
                        regionsLayout.addView(regionText);
                    }
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
                String birthdate = textBirthdate.getText().toString();

                Intent i = new Intent(Profile.this, EditProfile.class);
                i.putExtra("Email", currentUser.getEmail());
                i.putExtra("Name", currentUser.getName());
                i.putExtra("Birthdate", birthdate);
                i.putExtra("Username", currentUser.getUsername());
                i.putExtra("Region", currentUser.getRegion());
                i.putExtra("planId", currentUser.getPlan().getPlanId());
                i.putStringArrayListExtra("planRegions", currentUser.getPlan().getPlanRegions());
                Calendar planCalendar = Calendar.getInstance();
                planCalendar.setTime(currentUser.getPlan().getRenewalDate());
                int year = planCalendar.get(Calendar.YEAR);
                int month = planCalendar.get(Calendar.MONTH);
                int day = planCalendar.get(Calendar.DAY_OF_MONTH);
                String planRenewalDate = day + "/" + (month+1) + "/" +year;
                i.putExtra("planRenewalDate", planRenewalDate);
                i.putExtra("planCopies", currentUser.getPlan().getPlanCopies());
                i.putExtra("planStorage", currentUser.getPlan().getPlanStorage());
                i.putExtra("planCost", currentUser.getPlan().getPlanCost());
                startActivity(i);
            }
        });

    }

}
