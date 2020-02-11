package com.example.storit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import io.feeeei.circleseekbar.CircleSeekBar;

public class EditServer extends AppCompatActivity {

    private static final String TAG = "-----------------------";
    Toolbar toolbar;
    TextView seekbarValue;
    CircleSeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_server);

        //Toolbar for this page
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("CANCEL");
        toolbarTitle.setText("Edit Server");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        //onclick on cancel
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditServer.this, ServerInformation.class));
            }
        });

        //initialize
        seekbarValue = findViewById(R.id.seekBarValue);
        seekBar = (CircleSeekBar) findViewById(R.id.circularSeekbar);
        seekbarValue.setText(seekBar.getCurProcess() + " MB");

        //change value of seekBar
        seekBar.setOnSeekBarChangeListener(new CircleSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onChanged(CircleSeekBar seekbar, int curValue) {
                seekbarValue.setText(curValue + " MB");
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

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
