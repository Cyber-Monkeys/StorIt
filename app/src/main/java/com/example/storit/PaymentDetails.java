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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

public class PaymentDetails extends AppCompatActivity {

    //Variables
    private static final String TAG = "-----------------------";
    Toolbar toolbar;
    ListView listView;
    PaymentDetailsAdapter paymentDetailsAdapter;
    ArrayList<CreditCard> listOfCreditCard = new ArrayList<>();
    CollectionReference userPayments;
    FirebaseAuth firebaseAuth;
    String userId;
    FirebaseUser firebaseUser;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_details);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null){
            userId = firebaseUser.getUid();
            userPayments = db.collection("Users").document(userId).collection("Payments");
            getDatabase();
        }

        //Toolbar for this page
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Payment Details");
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
                startActivity(new Intent(PaymentDetails.this, Menu.class));
            }
        });

        listView = findViewById(R.id.listview);
        paymentDetailsAdapter = new PaymentDetailsAdapter(this, R.layout.payment_details_view,listOfCreditCard);
        listView.setAdapter(paymentDetailsAdapter);
        View footerView = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.payment_details_add_card, null, false);
        listView.addFooterView(footerView);

        Button goToAddCard = footerView.findViewById(R.id.button3);
        goToAddCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PaymentDetails.this, AddCardPage.class));
            }
        });
    }

    //Get payment from database
    public void getDatabase(){
        userPayments.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    for(DocumentSnapshot d : list){

                        String cardName = d.getString("Cardholder Name");
                        int cardNumber = d.getDouble("Credit Number").intValue();
                        int cvv = d.getDouble("CVV").intValue();
                        int month = d.getDouble("Month").intValue();
                        int year = d.getDouble("Year").intValue();

                        listOfCreditCard.add(new CreditCard(cardName, cardNumber, cvv, month, year));
                        paymentDetailsAdapter.notifyDataSetChanged();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "INSERTION OF DATA IS SUCCESS");
            }
        });
    }
}
