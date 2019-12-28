package com.example.bo.mediatest.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.bo.mediatest.MediaActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {
    private MediaRecorder mediaRecorder;
    private boolean isRecording;
    private String[] permissions = new String[]
            { Manifest.permission.WRITE_EXTERNAL_STORAGE,
              Manifest.permission.RECORD_AUDIO };
    private List<String> mPermissionList = new ArrayList<>();

    private MyBinder myBinder = new MyBinder();
    private static final String TAG = "media";

    public MyService() {
    }

    public class MyBinder extends Binder {
        public void startRecord() {
            Log.d(TAG, "startRecord: pid = " + android.os.Process.myPid() + " tid = " + android.os.Process.myTid());
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
                    }
                });

                Log.d(TAG, "startRecord: prepare");
                mediaRecorder.prepare();
                Log.d(TAG, "startRecord: start!");
                mediaRecorder.start();
                isRecording = true;
            }catch (Exception e) {
                Log.d(TAG, "startRecord: Exception");
                e.printStackTrace();
            }
        }

        public void stopRecord() {
            if(!isRecording) {
                return;
            }
            Log.d(TAG, "stop");
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        }

        public boolean isRecording() {
            return  isRecording;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: pid = " + android.os.Process.myPid() + " tid = " + android.os.Process.myTid());
        return super.onStartCommand(intent, flags, startId);
    }


}
