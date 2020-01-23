package com.example.storit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    //variables
    private static final String TAG = "AndroidClarified ----";
    public static final int PICK_IMAGE = 1;
    private TextView changePhoto;
    private EditText editName, editUsername, editEmail, editBirthdate;
    private Toolbar toolbar;
    private DatePickerDialog datePickerDialog;
    private static Calendar mCalendarDate;
    private FirebaseFirestore db;
    private DocumentReference documentReference;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private String userId;

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
        changePhoto = (TextView) findViewById(R.id.changePhoto);
        editName = (EditText) findViewById(R.id.editName);
        editUsername = (EditText) findViewById(R.id.editUsername);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editBirthdate = (EditText) findViewById(R.id.editBirthdate);

        //get attributes from profile page
        Intent i = getIntent();
        String username = i.getStringExtra("Username");
        String email = i.getStringExtra("Email");
        String name = i.getStringExtra("Name");
        String birthdate = i.getStringExtra("Birthdate");

        editName.setText(name);
        editUsername.setText(username);
        editBirthdate.setText(birthdate);
        editEmail.setText(email);

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
        getSupportActionBar().setTitle("Edit Profile");
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
                finish();
            }
        });


        //change profile picture
        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
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

                //check if email from database is not edited
                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){

                            String email = documentSnapshot.getString("Email");

                            if (email.equals(editEmail.getText().toString())){
                                //update database
                                documentReference.update(user);
                                startActivity(new Intent(EditProfile.this, Profile.class));
                                finish();
                            } else {
                                editEmail.setError("Email shouldn't be edited");
                            }

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "FAILURE " + e.getMessage());
                    }
                });

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
