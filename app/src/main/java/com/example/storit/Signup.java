package com.example.storit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class Signup extends AppCompatActivity {

    //Variables
    private EditText emailText, birthdateText, passwordText, confirmPasswordText;
    private Button btnSignUp;
    FirebaseAuth mFirebaseAuth;
    DatePickerDialog datePickerDialog;

    //onCreate function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //set variables
        mFirebaseAuth = FirebaseAuth.getInstance();
        emailText = (EditText)findViewById(R.id.email);
        passwordText = (EditText) findViewById(R.id.password);
        confirmPasswordText = (EditText) findViewById(R.id.confirmPassword);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);

        //onClick
        btnSignUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();
                String confirmPassword = confirmPasswordText.getText().toString();

                if(email.isEmpty()){
                    emailText.setError("Please enter Email");
                    emailText.requestFocus();
                }else if(password.isEmpty()){
                    passwordText.setError("Please enter Password");
                    passwordText.requestFocus();
                }else if(confirmPassword.isEmpty()){
                    confirmPasswordText.setError("Confirm password");
                    confirmPasswordText.requestFocus();
                }else if(!(email.isEmpty() && password.isEmpty() && confirmPassword.isEmpty())){
                    //CHECK IF PASSWORD IS >= 6 characters
                    if(password.equals(confirmPassword)){
                        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Signup.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(!task.isSuccessful()){
                                    Toast.makeText(Signup.this, "Signup Unsuccessful", Toast.LENGTH_SHORT);
                                }else{
                                    Toast.makeText(Signup.this, "Signup Successful", Toast.LENGTH_LONG);
//                                    Intent i = new Intent(getApplicationContext(), Login.class);
//                                    startActivity(i);
                                }
                            }

                        });
                    }else{
                        Toast.makeText(Signup.this, "Password is not the same", Toast.LENGTH_LONG);
                    }
                }else{
                    Toast.makeText(Signup.this, "Error occured", Toast.LENGTH_SHORT);
                }
            }
        });

        //Date picker (editText)
        birthdateText = (EditText) findViewById(R.id.birthdate);
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
                        birthdateText.setText(dayOfMonth+"/"+(month+1)+"/"+year);
                    }
                }, year, month, day);
                datePickerDialog.show(); //show date picker
            }
        });
    }

}
