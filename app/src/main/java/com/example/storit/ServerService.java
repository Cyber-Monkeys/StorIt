package com.example.storit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class ServerService extends Service {

    private static final String CHANNEL_DEFAULT_IMPORTANCE = "serverService";
    private static final int ONGOING_NOTIFICATION_ID = 1;
    WebRtcServer server = null;
    String idToken;
    String uniqueID;
    int storageSize;
    Thread backgJob;
    public ServerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();



    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        storageSize = intent.getIntExtra("storageSize", -1);
        uniqueID = intent.getStringExtra("devId");
        backgJob = new Thread(new Runnable() { @Override
        public void run() {
            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
            mUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();
                                    // start service
                                    server = new WebRtcServer("https://www.vrpacman.com/",getApplicationContext(), "server");
                                    server.emitServer(idToken, storageSize, uniqueID);

                            } else {
                                // Handle error -> task.getException();
                                Log.d("res3", "no token verified");
                            }
                        }
                    });

        }
        });
        backgJob.start();
        Intent notificationIntent = new Intent(this, Menu.class);
        String channelId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("serverService", "foreground server");
        }

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            Notification notification =
                    new Notification.Builder(this, channelId)
                            .setContentTitle("Storit Server")
                            .setContentText("Server is running in the backkground")
                            .setSmallIcon(R.drawable.storit_logo)
                            .setContentIntent(pendingIntent)
                            .setTicker("hi")
                            .build();
            startForeground(ONGOING_NOTIFICATION_ID, notification);
        } else {
            Notification notification =
                    new NotificationCompat.Builder(this, channelId)
                            .setContentTitle("Storit Server")
                            .setContentText("Server is running in the backkground")
                            .setContentIntent(pendingIntent)
                            .setTicker("hi")
                            .build();
            startForeground(ONGOING_NOTIFICATION_ID, notification);
        }
        return super.onStartCommand(intent, flags, startId);
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(chan);
        return channelId;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        server.onDestroy();
        backgJob.interrupt();
    }
}
