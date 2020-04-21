package com.example.storit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Signup extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    //Variables
    private static Calendar mCalendarDate;
    private static final String TAG = "AndroidClarified ----";
    private EditText usernameText, firstNameText, lastNameText, emailText, birthdateText, passwordText, confirmPasswordText;
    private Button btnSignUp;
    FirebaseAuth mFirebaseAuth;
    DatePickerDialog datePickerDialog;
    ProgressDialog progressDialog;
    private FirebaseFirestore db;
    DocumentReference documentReference;
    String userId;
    Spinner spinner;
    String region = "EU";

    //onCreate function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //set variables
        progressDialog = new ProgressDialog(this);
        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCalendarDate = Calendar.getInstance();

        usernameText = (EditText)findViewById(R.id.username);
        firstNameText = (EditText)findViewById(R.id.firstName);
        lastNameText = (EditText)findViewById(R.id.lastName);
        emailText = (EditText)findViewById(R.id.email);
        passwordText = (EditText) findViewById(R.id.password);
        confirmPasswordText = (EditText) findViewById(R.id.confirmPassword);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        //Date picker (editText)
        birthdateText = (EditText) findViewById(R.id.birthdate);
        spinner = (Spinner) findViewById(R.id.spinner2);

        birthdateText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(Signup.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mCalendarDate.set(year, month, dayOfMonth);
                        birthdateText.setText(dayOfMonth+"/"+(month+1)+"/"+year);
                    }
                }, year, month, day);
                datePickerDialog.show(); //show date picker
            }
        });

        //Sign up user
        btnSignUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                final String username = usernameText.getText().toString();
                final String firstName = firstNameText.getText().toString();
                final String lastName = lastNameText.getText().toString();
                final String email = emailText.getText().toString();
                String birthDate = birthdateText.getText().toString();
                String password = passwordText.getText().toString();
                String confirmPassword = confirmPasswordText.getText().toString();
                User currentUser = new User(username, email, firstName, lastName, mCalendarDate.getTime(), region);
                Plan userPlan = new Plan(1);
                currentUser.setPlan(userPlan);
                if(username.isEmpty()){
                    usernameText.setError("Please enter Username");
                    usernameText.requestFocus();
                }else if(firstName.isEmpty()){
                    firstNameText.setError("Please enter First Name");
                    firstNameText.requestFocus();
                }else if(lastName.isEmpty()){
                    lastNameText.setError("Please enter Last Name");
                    lastNameText.requestFocus();
                }else if(email.isEmpty()){
                    emailText.setError("Please enter Email");
                    emailText.requestFocus();
                }else if(birthDate.isEmpty()){
                    birthdateText.setError("Please choose Birthdate");
                }else if(password.isEmpty()){
                    passwordText.setError("Please enter Password");
                    passwordText.requestFocus();
                }else if(confirmPassword.isEmpty()){
                    confirmPasswordText.setError("Confirm password");
                    confirmPasswordText.requestFocus();
                }else if(!(username.isEmpty() && firstName.isEmpty() && lastName.isEmpty() &&  birthDate.isEmpty()
                        && email.isEmpty() && password.isEmpty() && confirmPassword.isEmpty())){

                    //CHECK IF PASSWORD IS >= 6 characters
                    progressDialog.setMessage("Creating account...");
                    progressDialog.show();
                    if(password.equals(confirmPassword)){
                        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Signup.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if(task.isSuccessful()){
                                    Log.d(TAG, "SIGNUP SUCCESSFUL" );

                                    //Put user details in fireStore
                                    userId = Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid();

                                    documentReference = db.collection("Users").document(userId);

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("Username", currentUser.getUsername());
                                    user.put("Name", currentUser.getFirstName() + " " + currentUser.getLastName());
                                    user.put("Email", currentUser.getEmail());
                                    user.put("Birthdate", currentUser.getDateOfBirth());
                                    user.put("Region", currentUser.getRegion());
                                    Map<String, Object> planData = new HashMap<>();
                                    planData.put("planId", currentUser.getPlan().getPlanId());
                                    planData.put("planStorage", currentUser.getPlan().getPlanCopies());
                                    planData.put("planCopies", currentUser.getPlan().getPlanCopies());
                                    planData.put("planRegions", currentUser.getPlan().getPlanRegions());
                                    planData.put("planRenewalDate", currentUser.getPlan().getRenewalDate());
                                    user.put("plan", planData);

                                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "user Created" );

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure" + e.getMessage());
                                        }
                                    });

                                    }else{
                                        Log.d(TAG, "SIGNUP UNSUCCESSFUL" );
                                    }
                            }

                        });
//
                    }else{
                        progressDialog.dismiss();
                        Toast.makeText(Signup.this, "Password is not the same", Toast.LENGTH_LONG);
                    }
                }else{
                    progressDialog.dismiss();
                    Toast.makeText(Signup.this, "Error occured", Toast.LENGTH_SHORT);
                }
            }
        });

        //add spinner for region picker
        ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(this, R.array.region, android.R.layout.simple_spinner_item);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(regionAdapter);
        spinner.setOnItemSelectedListener(this); //enable click


    }

    public void goToLogin(View view){
        startActivity(new Intent (this, Login.class));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        region = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
