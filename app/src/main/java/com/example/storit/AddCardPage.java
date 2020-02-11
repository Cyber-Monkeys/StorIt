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
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddCardPage extends AppCompatActivity {

    //Variables
    private static final String TAG = "-----------------------";
    Toolbar toolbar;
    EditText creditName, creditNum, cvv, month, year;
    Button addCard;
    private FirebaseFirestore db;
    CollectionReference userPayments;
    FirebaseAuth firebaseAuth;
    String userId;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card_page);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null){
            userId = firebaseUser.getUid();
            userPayments = db.collection("Users").document(userId).collection("Payments");
        }

        //Toolbar for this page
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Card");
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
                startActivity(new Intent(AddCardPage.this, PaymentDetails.class));
            }
        });

        creditName = findViewById(R.id.editCreditName);
        creditNum = findViewById(R.id.editCreditNum);
        cvv = findViewById(R.id.editCvv);
        month = findViewById(R.id.editMonth);
        year = findViewById(R.id.editYear);
        addCard = findViewById(R.id.button);

        addCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mName = creditName.getText().toString();
                int mNum = Integer.parseInt(creditNum.getText().toString());
                int mCvv = Integer.parseInt(cvv.getText().toString());
                int mMonth = Integer.parseInt(month.getText().toString());
                int mYear = Integer.parseInt(year.getText().toString());

                //store Classes in database
                Map<String, Object> user = new HashMap<>();
                user.put("Cardholder Name", mName);
                user.put("Credit Number", mNum);
                user.put("CVV", mCvv);
                user.put("Month", mMonth);
                user.put("Year", mYear);

                //add item to database
                userPayments.add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "INSERTION OF DATA IS SUCCESS");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "INSERTION OF DATA IS FAILED");
                    }
                });

                finish();
                startActivity(new Intent(AddCardPage.this, PaymentDetails.class));
            }
        });

    }
}
