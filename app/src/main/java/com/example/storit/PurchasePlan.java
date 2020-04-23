package com.example.storit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PurchasePlan extends AppCompatActivity {

    //Variables
    private static final String TAG = "-----------------------";
    Toolbar toolbar;
    CollectionReference userPayments;
    FirebaseAuth firebaseAuth;
    String userId;
    FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private TextView planIdText, planCostText, planStorageText, planCopiesText, planRegionsText;
    private Button checkoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_plan);

        planIdText = (TextView) findViewById(R.id.planId);
        planCostText = (TextView) findViewById(R.id.planCost);
        planStorageText = (TextView) findViewById(R.id.planStorage);
        planCopiesText = (TextView) findViewById(R.id.planCopies);
        planRegionsText = (TextView) findViewById(R.id.planRegions);
        checkoutButton = (Button) findViewById(R.id.checkoutBtn);


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
        getSupportActionBar().setTitle("Purchase Plan");
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
        Intent i = getIntent();
        Boolean isPurchased = i.getBooleanExtra("isPurchased", false);
        int planId = i.getIntExtra("planId", 1);
        switch (planId) {
            case 1:
                planIdText.setText("Plan 1");
                planCostText.setText("$5");
                planStorageText.setText("1 GB");
                planCopiesText.setText("2 copies");
                planRegionsText.setText("1 Region");
                break;
            case 2:
                planIdText.setText("Plan 2");
                planCostText.setText("$10");
                planStorageText.setText("5 GB");
                planCopiesText.setText("2 copies");
                planRegionsText.setText("2 Regions");
                break;
            case 3:
                planIdText.setText("Plan 3");
                planCostText.setText("$15");
                planStorageText.setText("5 GB");
                planCopiesText.setText("3 copies");
                planRegionsText.setText("2 Region");
                break;
        }



//        View footerView = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.purchase_plan_footer, null, false);
        if(isPurchased)
            checkoutButton.setVisibility(View.INVISIBLE);


    }
}
