package com.example.storit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.feeeei.circleseekbar.CircleSeekBar;

public class ServerInformation extends AppCompatActivity {

    //Variables
    private static final String TAG = "-----------------------";
    Toolbar toolbar;
    TextView seekbarValue;
    CircleSeekBar seekBar;
    Button editServerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_information);

        //Toolbar for this page
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Server Information");
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
                startActivity(new Intent(ServerInformation.this, Menu.class));
            }
        });

        //initialize
        seekbarValue = findViewById(R.id.seekBarValue);
        seekBar = (CircleSeekBar) findViewById(R.id.circularSeekbar);
        editServerButton = findViewById(R.id.editServer);

        seekBar.setCurProcess(100);
        seekbarValue.setText(100 + " MB");

        //stop seekbar from moving
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        //onclick
        editServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ServerInformation.this, EditServer.class));
            }
        });
    }
}
