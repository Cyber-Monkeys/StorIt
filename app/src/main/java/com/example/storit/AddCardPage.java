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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddCardPage extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    //Variables
    private static final String TAG = "-----------------------";
    Toolbar toolbar;
    EditText creditName, creditNum, cvv;
    Button addCard;
    Spinner monthSpinner, yearSpinner;
    private FirebaseFirestore db;
    CollectionReference userPayments;
    FirebaseAuth firebaseAuth;
    String userId, month, year;
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
        creditNum = findViewById(R.id.editCreditName);
        cvv = findViewById(R.id.editCvv);
        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        addCard = findViewById(R.id.button);

        addCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mName = creditName.getText().toString();
                long mNum = Long.parseLong(creditNum.getText().toString());
                int mCvv = Integer.parseInt(cvv.getText().toString());
                int mMonth = Integer.parseInt(month);
                int mYear = Integer.parseInt(year);

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

        //add spinner for month picker
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this, R.array.months, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);
        monthSpinner.setOnItemSelectedListener(this); //enable click
        //add spinner for year picker
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(this, R.array.years, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);
        yearSpinner.setOnItemSelectedListener(this); //enable click

    }

    //When spinner is selected
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (parent.getId() == R.id.monthSpinner){
            if (parent.getItemAtPosition(position).equals("Month")){
                //do nothings
            }
            month = parent.getItemAtPosition(position).toString();
            Toast.makeText(parent.getContext(), month, Toast.LENGTH_SHORT).show();

        } else {
            if (parent.getItemAtPosition(position).equals("Year")){
                //do nothings
            }
            year = parent.getItemAtPosition(position).toString();
            Toast.makeText(parent.getContext(), year, Toast.LENGTH_SHORT).show();
        }
    }

    //When spinner is not selected
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
