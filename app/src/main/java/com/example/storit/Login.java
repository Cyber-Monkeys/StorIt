package com.example.storit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    //CTRL + O = shortcut to see overload functions

    //variables
    private static final String TAG = "AndroidClarified";
    Intent i;
    private GoogleSignInClient googleSignInClient;
    private SignInButton googleSignInButton;
    int RC_SIGN_IN=0;
    private TextView noAccountSignUp, textForgotPass;
    private EditText emailText, passwordText;
    private Button btnLogIn;
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseFirestore db;
    DocumentReference documentReference;
    String userId;
    ProgressDialog progressDialog;

    //onCreate function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //set variables
        progressDialog = new ProgressDialog(this);
        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        noAccountSignUp = (TextView) findViewById(R.id.noAccountSignUp);
        textForgotPass = (TextView) findViewById(R.id.forgotPassword);
        emailText = (EditText) findViewById(R.id.username);
        passwordText = (EditText) findViewById(R.id.password);
        btnLogIn = (Button) findViewById(R.id.btnLogIn);
        googleSignInButton = findViewById(R.id.sign_in_button);
        googleSignInButton.setColorScheme(SignInButton.COLOR_DARK);

        TextView textView = (TextView) googleSignInButton.getChildAt(0);
        textView.setText("Sign in with Google");

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        //Auth State Listener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if(mFirebaseUser != null){
                    Toast.makeText(Login.this, "Logged In", Toast.LENGTH_SHORT);
                    Intent i = new Intent(Login.this, Menu.class);
                    startActivity(i);
                }else{
                    Toast.makeText(Login.this, "Please storit_logo in", Toast.LENGTH_SHORT);
                }
            }
        };

        //onClick for sign in using Google
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.sign_in_button:
                        signInGoogle();
                        break;
                    // ...
                }
            }
        });

        //onClick for using normal sign in
        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Logging in...");
                progressDialog.show();
                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();

                if(email.isEmpty()){
                    emailText.setError("Please enter Email");
                    emailText.requestFocus();
                }else if(password.isEmpty()){
                    passwordText.setError("Please enter Password");
                    passwordText.requestFocus();
                }else if(!(email.isEmpty() && password.isEmpty())){
                    mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                progressDialog.dismiss();
                                Toast.makeText(Login.this, "Please storit_logo in again", Toast.LENGTH_SHORT);
                            }else{
                                progressDialog.dismiss();
                                Intent i = new Intent(Login.this, Menu.class);
                                startActivity(i);
                            }
                        }
                    });
                }else{
                    progressDialog.dismiss();
                    Toast.makeText(Login.this, "Error occured", Toast.LENGTH_SHORT);
                }
            }
        });

        String text = "Don't have an account? SIGN UP";

        SpannableString ss = new SpannableString(text);

        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                goToSignUp(widget);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#A4DBAA"));
                ds.setUnderlineText(false);
                ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            }
        };

        ss.setSpan(clickableSpan1, 23, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        noAccountSignUp.setText(ss);
        noAccountSignUp.setMovementMethod(LinkMovementMethod.getInstance());

        textForgotPass.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showForgotPasswordDialog();
            }
        });

    }

    //onStart function
    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    //sign in Google
    private void signInGoogle(){
        Intent signIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Store email in database
                            Log.d(TAG, "signInWithCredential:success");
                            userId = mFirebaseAuth.getCurrentUser().getUid();
                            documentReference = db.collection("Users").document(userId);
                            final Map<String, Object> user = new HashMap<>();
                            user.put("Email", mFirebaseAuth.getCurrentUser().getEmail());
                            user.put("Username", "");
                            user.put("Name", "");
                            Date date = new Date(); //set date to null
                            user.put("Birthdate", date);

                            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                    if(documentSnapshot.exists()){
                                        //check if email exists or not
                                        //if yes, add it
                                        if (!documentSnapshot.contains("Email")){
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
                                        }

                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "FAILURE " + e.getMessage());
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }

                    }
                });
    }

    //set dialog for forgot password
    private void showForgotPasswordDialog(){
        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");

        //set linear layout
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setFocusableInTouchMode(true);

        //views to set in dialog

        final EditText mEmailText = new EditText(this);
        mEmailText.setHint("Email");
        mEmailText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mEmailText.setMinEms(10);

        linearLayout.addView(mEmailText);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        //buttons
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = mEmailText.getText().toString().trim();

                //if email is null or empty send error
                if (email.isEmpty()){
                    Toast.makeText(Login.this, "Email is empty", Toast.LENGTH_SHORT).show();
                } else{
                    beginRecovery(email);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Cancel dialog
                dialog.dismiss();
            }
        });

        //show dialog
        builder.create().show();

    }

    //forgot password
    private void beginRecovery(String email){
        progressDialog.setMessage("Sending email...");
        progressDialog.show();
        mFirebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            Toast.makeText(Login.this, "Email Sent", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(Login.this, "Email Invalid", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //go To sign up activity
    public void goToSignUp(View v){
        i = new Intent(this, Signup.class);
        startActivity(i);
    }

}
