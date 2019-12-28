package com.example.bo.mediatest;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.example.bo.mediatest.service.MyService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaActivity extends AppCompatActivity {

    private static final String TAG = "media";
    private Button btn_start, btn_stop;
    private Chronometer chronometer;

    private String[] permissions = new String[]
            { Manifest.permission.WRITE_EXTERNAL_STORAGE,
              Manifest.permission.RECORD_AUDIO };
    private List<String> mPermissionList = new ArrayList<>();

    private MyService.MyBinder myBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: pid = " + android.os.Process.myPid() + " tid = " + android.os.Process.myTid());
            myBinder = (MyService.MyBinder)iBinder;
            btn_start.setEnabled(!myBinder.isRecording());
            btn_stop.setEnabled(myBinder.isRecording());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: ");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        Log.d(TAG, "onCreate: pid = " + android.os.Process.myPid() + " tid = " + android.os.Process.myTid());
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
        Intent bindIntent = new Intent(this, MyService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);

        checkPermission();
    }

    private void init() {
        Log.d(TAG, "init: ");
        btn_start = (Button) findViewById(R.id.start);
        btn_stop = (Button) findViewById(R.id.stop);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        btn_start.setOnClickListener(clickListener);
        btn_stop.setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
           switch (view.getId()) {
               case R.id.start:
                   chronometer.setBase(SystemClock.elapsedRealtime());
                   int hour = (int) ((SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000 / 60);
                   chronometer.setFormat("0" + String.valueOf(hour)+":%s");
                   chronometer.start();
                   myBinder.startRecord();
                   if(myBinder.isRecording()) {
                       btn_start.setEnabled(false);
                       btn_stop.setEnabled(true);
                   }
                   break;
               case R.id.stop:
                   chronometer.stop();
                   if(myBinder.isRecording()) {
                       btn_start.setEnabled(true);
                       btn_stop.setEnabled(false);
                   }
                   myBinder.stopRecord();
                   break;
               default:
                   break;
           }
        }
    };

    private void checkPermission() {
        mPermissionList.clear();
        for(String permission : permissions) {
            Log.d(TAG, "initPermission: checking permission : " + permission);
            if(ContextCompat.checkSelfPermission(MediaActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "initPermission: permission : " + permission + "added");
                mPermissionList.add(permission);
            }
        }
        if(mPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(MediaActivity.this, permissions, 1);
        }
        else {
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean denied = false;
        switch (requestCode) {
            case 1:
                for(int i = 0; i < grantResults.length; i ++) {
                    if(grantResults[i] == -1) {
                        denied = true;
                    }
                }
                if(denied) {
                    Toast.makeText(MediaActivity.this, "permission denied", Toast.LENGTH_SHORT).show();
                }
                else {
                    init();
                }
                break;
            default:
                break;
        }
    }

    /*
    private void startRecord() {
        try {
            File file = new File("/sdcard/mediarecoder.amr");
            Log.d(TAG, "startRecord: file path = " + file.getAbsolutePath());
            if (file.exists()) {
                file.delete();
            }
            mediaRecorder = new MediaRecorder();
            Log.d(TAG, "startRecord: set audio source");
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            Log.d(TAG, "startRecord: set output format");
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            Log.d(TAG, "startRecord: set audio encoder");
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            Log.d(TAG, "startRecord: set output file");
            mediaRecorder.setOutputFile(file.getAbsolutePath());
            Log.d(TAG, "startRecord: set error listener");
            mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mediaRecorder, int i, int i1) {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    mediaRecorder = null;
                    isRecording = false;
                    btn_start.setEnabled(true);
                    btn_stop.setEnabled(false);
                    Toast.makeText(MediaActivity.this, "shit happens", Toast.LENGTH_SHORT).show();
                }
            });

            Log.d(TAG, "startRecord: prepare");
            mediaRecorder.prepare();
            Log.d(TAG, "startRecord: start!");
            mediaRecorder.start();
            isRecording = true;
            btn_start.setEnabled(false);
            btn_stop.setEnabled(true);
            Toast.makeText(MediaActivity.this, "start", Toast.LENGTH_SHORT).show();
        }catch (Exception e) {
            Log.d(TAG, "startRecord: Exception");
            e.printStackTrace();
        }
    }

    private void stop() {
       if(!isRecording) {
           return;
       }
        Log.d(TAG, "stop");
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        isRecording = false;
        btn_start.setEnabled(true);
        btn_stop.setEnabled(false);
        Toast.makeText(MediaActivity.this, "stop", Toast.LENGTH_SHORT).show();
    }
    */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
//        if(isRecording) {
//            mediaRecorder.stop();
//            mediaRecorder.release();
//            mediaRecorder = null;
//        }
    }
}
