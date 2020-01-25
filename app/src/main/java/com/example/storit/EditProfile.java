package com.example.storit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    //variables
    private static final String TAG = "AndroidClarified ----";
    public static final int PICK_IMAGE = 1;
    private TextView changePhoto;
    private EditText editName, editUsername, editEmail, editBirthdate;
    private ImageView circularImage;
    private Toolbar toolbar;
    private DatePickerDialog datePickerDialog;
    private static Calendar mCalendarDate;
    private FirebaseFirestore db;
    private DocumentReference documentReference;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private String userId;
    private Uri imageUri;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = mFirebaseAuth.getCurrentUser();
        userId = firebaseUser.getUid();
        documentReference = db.collection("Users").document(userId);

        mCalendarDate = Calendar.getInstance();
        circularImage = (ImageView) findViewById(R.id.circular_image);
        changePhoto = (TextView) findViewById(R.id.changePhoto);
        editName = (EditText) findViewById(R.id.editName);
        editUsername = (EditText) findViewById(R.id.editUsername);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editBirthdate = (EditText) findViewById(R.id.editBirthdate);

        //setting profile photo from firebase
        if(firebaseUser.getPhotoUrl() != null){
            //dependency used
            Glide.with(this)
                    .load(firebaseUser.getPhotoUrl())
                    .into(circularImage);
        }

        //get attributes from profile page
        Intent i = getIntent();
        String username = i.getStringExtra("Username");
        String email = i.getStringExtra("Email");
        String name = i.getStringExtra("Name");
        String birthdate = i.getStringExtra("Birthdate");

        editName.setText(name);
        editUsername.setText(username);
        editBirthdate.setText(birthdate);
        editEmail.setText(email);

        //datepicker
        editBirthdate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(EditProfile.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mCalendarDate.set(year, month, dayOfMonth);
                        editBirthdate.setText(dayOfMonth+"/"+(month+1)+"/"+year);
                    }
                }, year, month, day);
                datePickerDialog.show(); //show date picker
            }
        });

        //Toolbar for this page
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Profile");
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


        //change profile picture
        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, PICK_IMAGE);
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
                final Map<String, Object> user = new HashMap<>();
                //user.put("Category", newItem.getCategory());
                user.put("Email", editEmail.getText().toString());
                user.put("Name", editName.getText().toString());
                user.put("Username", editUsername.getText().toString());
                user.put("Birthdate", mCalendarDate.getTime());

                //check if email from database is not edited
                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){

                            String email = documentSnapshot.getString("Email");

                            if (email.equals(editEmail.getText().toString())){
                                //update database
                                documentReference.update(user);
                            } else {
                                editEmail.setError("Email shouldn't be edited");
                            }

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "FAILURE " + e.getMessage());
                    }
                });

                if(bitmap != null){
                    //send image to firebase storage
                    handleUpload(bitmap);
                }

                //set delay because it takes some time to update
                //to firebase storage
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 5s = 5000ms
                        startActivity(new Intent(EditProfile.this, Profile.class));
                        finish();
                    }
                }, 4000);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {

            //convert URI to bitmap
            imageUri = data.getData();
            bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //set change image profile
            circularImage.setImageBitmap(bitmap);

        }
    }

    //upload profile pic to firebase storage
    private void handleUpload(Bitmap bitmap){

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        //get uid of user
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("profileImages")
                .child(uid + ".jpeg");

        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getDownloadUrl(reference);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure", e.getCause());
                    }
                });
    }

    private void getDownloadUrl(StorageReference reference){
        reference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "onSuccess" + uri);
                        setUserProfileUrl(uri);
                    }
                });
    }

    private void setUserProfileUrl(Uri uri){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditProfile.this, "Upload sucess", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, "Upload sucess", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
