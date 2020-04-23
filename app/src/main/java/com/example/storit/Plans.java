package com.example.storit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Plans extends AppCompatActivity {

    //Variables
    Toolbar toolbar;
    TextView toolbarTitle;
    LinearLayout linearLayout;
    int planId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);

        //Toolbar for this page
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Plans");
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
                startActivity(new Intent(Plans.this, Menu.class));
            }
        });

        Intent i = getIntent();
        planId = i.getIntExtra("planId", -1);
        switch(planId) {
            case 1:
                linearLayout = (LinearLayout) findViewById(R.id.linearLayout1);
                break;
            case 2:
                linearLayout = (LinearLayout) findViewById(R.id.linearLayout2);
                break;
            case 3:
                linearLayout = (LinearLayout) findViewById(R.id.linearLayout3);
                break;
        }
        linearLayout.setBackgroundColor(Color.argb(100, 53, 232, 65));

    }

    public void purchasePlanClicked(View v) {
        // pass the plan to the next activity
        Boolean isPurchased = false;
        int id = -1;
        if(v.getId() == R.id.linearLayout1) {
            id = 1;
        } else if(v.getId() == R.id.linearLayout2) {
            id = 2;
        } else if(v.getId() == R.id.linearLayout3) {
            id = 3;
        }
        if(id == planId) {
            isPurchased = true;
        }
        Intent i = new Intent(Plans.this, PurchasePlan.class);
        i.putExtra("isPurchased", isPurchased);
        i.putExtra("planId", id);
        startActivity(i);
    }
}
