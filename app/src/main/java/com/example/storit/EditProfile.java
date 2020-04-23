package com.example.storit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfile extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //variables
    private static final String TAG = "AndroidClarified ----";
    public static final int PICK_IMAGE = 1;
    private TextView changePhoto, planIdText, planStorageText, planCopiesText, planCostText, planRenewalDateText;
    private EditText editName, editUsername, editEmail, editBirthdate;
    private ImageView circularImage;
    private Toolbar toolbar;
    private DatePickerDialog datePickerDialog;
    private static Calendar mCalendarDate;
    private FirebaseFirestore db;
    private DocumentReference documentReference;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private String userId;
    private Uri imageUri;
    private Bitmap bitmap;
    private LinearLayout regionsLayout;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = mFirebaseAuth.getCurrentUser();
        userId = firebaseUser.getUid();
        documentReference = db.collection("Users").document(userId);

        mCalendarDate = Calendar.getInstance();
        circularImage = (ImageView) findViewById(R.id.circular_image);
        changePhoto = (TextView) findViewById(R.id.changePhoto);
        editName = (EditText) findViewById(R.id.editName);
        editUsername = (EditText) findViewById(R.id.editUsername);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editBirthdate = (EditText) findViewById(R.id.editBirthdate);
        planIdText = (TextView) findViewById(R.id.planId);
        planCopiesText = (TextView) findViewById(R.id.planCopies);
        planStorageText = (TextView) findViewById(R.id.planStorage);
        planRenewalDateText = (TextView) findViewById(R.id.planRenewalDate);
        planCostText = (TextView) findViewById(R.id.planCost);
        regionsLayout = (LinearLayout)findViewById(R.id.regionsLayout);


        //setting profile photo from firebase
        if(firebaseUser.getPhotoUrl() != null){
            //dependency used
            Glide.with(this)
                    .load(firebaseUser.getPhotoUrl())
                    .into(circularImage);
        }

        //get attributes from profile page
        Intent i = getIntent();
        String username = i.getStringExtra("Username");
        String email = i.getStringExtra("Email");
        String name = i.getStringExtra("Name");
        String birthdate = i.getStringExtra("Birthdate");
        String region = i.getStringExtra("Region");
        int planId = i.getIntExtra("planId", 1);
        ArrayList<String> planRegions = i.getStringArrayListExtra("planRegions");
        String planRenewalDate = i.getStringExtra("planRenewalDate");
        int planCopies = i.getIntExtra("planCopies", -1);
        int planStorage = i.getIntExtra("planStorage", -1);
        int planCost = i.getIntExtra("planCost", -1);
        String dates[] = planRenewalDate.split("/");
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Integer.parseInt(dates[2]),Integer.parseInt(dates[1]), Integer.parseInt(dates[0]));
        Plan plan = new Plan(planId, planRegions, new Date(mCalendar.getTimeInMillis()));
        dates = birthdate.split("/");
        mCalendar.set(Integer.parseInt(dates[2]),Integer.parseInt(dates[1]), Integer.parseInt(dates[0]));
        currentUser = new User(username, email, name, new Date(mCalendar.getTimeInMillis()), region, plan);
        editName.setText(name);
        editUsername.setText(username);
        editBirthdate.setText(birthdate);
        editEmail.setText(email);
        planIdText.setText("Plan " + planId);
        planCopiesText.setText(planCopies + " copies");
        planStorageText.setText(planStorage + " GB");
        planRenewalDateText.setText("RenewalDate: " +planRenewalDate);
        planCostText.setText("$ " + planCost);


        //datepicker
        editBirthdate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(EditProfile.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mCalendarDate.set(year, month, dayOfMonth);
                        editBirthdate.setText(dayOfMonth+"/"+(month+1)+"/"+year);
                    }
                }, year, month, day);
                datePickerDialog.show(); //show date picker
            }
        });

        //Toolbar for this page
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("CANCEL");
        toolbarTitle.setText("Edit Profile");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        //onclick cancel
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //change profile picture
        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });
        regionsLayout.setWeightSum(planRegions.size() * 2);
        for(int j = 0; j < planRegions.size();j++) {
            TextView regionHeaderText = new TextView(this);
            regionHeaderText.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            regionHeaderText.setGravity(0);
            regionHeaderText.setText("Region " + (j + 1));
            regionHeaderText.setTextSize(20);
            Spinner regionSpinner = new Spinner(this);
            regionSpinner.setId(j);
            regionSpinner.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            //add spinner for region picker
            ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(this, R.array.region, android.R.layout.simple_spinner_item);
            regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            regionSpinner.setAdapter(regionAdapter);
            regionSpinner.setOnItemSelectedListener(this); //enable click
            int value = regionAdapter.getPosition(currentUser.getPlan().getPlanRegions().get(j));
            regionSpinner.setSelection(value);
            regionsLayout.addView(regionHeaderText);
            regionsLayout.addView(regionSpinner);
        }

    }

    //add save button in toolbar
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_edit_profile, menu);
        return true;
    }

    //add function on save button
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_button:
                final Map<String, Object> user = new HashMap<>();
                //user.put("Category", newItem.getCategory());
                user.put("Email", editEmail.getText().toString());
                user.put("Name", editName.getText().toString());
                user.put("Username", editUsername.getText().toString());
                user.put("Birthdate", mCalendarDate.getTime());
                user.put("Region", currentUser.getRegion());
                Map<String, Object> planData = new HashMap<>();
                planData.put("planId", currentUser.getPlan().getPlanId());
                planData.put("planStorage", currentUser.getPlan().getPlanCopies());
                planData.put("planCopies", currentUser.getPlan().getPlanCopies());
                planData.put("planRegions", currentUser.getPlan().getPlanRegions());
                planData.put("planRenewalDate", currentUser.getPlan().getRenewalDate());
                planData.put("planCost", currentUser.getPlan().getPlanCost());
                user.put("plan", planData);
//                //check if email from database is not edited
//                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        if(documentSnapshot.exists()){
//
//                            String email = documentSnapshot.getString("Email");
//
//                            if (email.equals(editEmail.getText().toString())){
//                                //update database
//                                documentReference.update(user);
//                            } else {
//                                editEmail.setError("Email shouldn't be edited");
//                            }
//
//                        }
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(TAG, "FAILURE " + e.getMessage());
//                    }
//                });
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
                });

                if(bitmap != null){
                    //send image to firebase storage
                    handleUpload(bitmap);
                }

                //set delay because it takes some time to update
                //to firebase storage
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 5s = 5000ms
                        startActivity(new Intent(EditProfile.this, Profile.class));
//                        finish();
                    }
                }, 4000);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {

            //convert URI to bitmap
            imageUri = data.getData();
            bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //set change image profile
            circularImage.setImageBitmap(bitmap);

        }
    }

    //upload profile pic to firebase storage
    private void handleUpload(Bitmap bitmap){

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        //get uid of user
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("profileImages")
                .child(uid + ".jpeg");

        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getDownloadUrl(reference);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure", e.getCause());
                    }
                });
    }

    private void getDownloadUrl(StorageReference reference){
        reference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "onSuccess" + uri);
                        setUserProfileUrl(uri);
                    }
                });
    }

    private void setUserProfileUrl(Uri uri){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditProfile.this, "Upload sucess", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, "Upload sucess", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //When spinner is selected
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        int regionId = parent.getId();
        ArrayList<String> regions = currentUser.getPlan().getPlanRegions();
        regions.set(regionId, text);
        currentUser.getPlan().setPlanRegions(regions);
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    //When spinner is not selected
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
