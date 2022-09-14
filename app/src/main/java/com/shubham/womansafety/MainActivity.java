package com.shubham.womansafety;

import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    ImageButton emergencyContactBtn, map, fakeCall,rightInfo, contact_list;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private boolean audioRecordingPermissionGranted = false;

    private String fileName;
    private ImageButton startRecordingButton, stopRecordingButton, playRecordingButton, stopPlayingButton;
    private MediaRecorder recorder;
    private MediaPlayer player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        emergencyContactBtn=findViewById(R.id.emergencyContactBtn);
        fakeCall=findViewById(R.id.fakeCall);
        contact_list=findViewById(R.id.contact_list);
        rightInfo=findViewById(R.id.rightInfo);
        map=findViewById(R.id.map);

        fakeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, CallMainActivity.class);
                startActivity(i);
            }
        });
        contact_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ContactListActivity.class);
                startActivity(i);
            }
        });
        rightInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, RightInfo.class);
                startActivity(i);
            }
        });

        emergencyContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SOSActivity.class);
                startActivity(i);
            }
        });
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, NearByPlaces.class);
                startActivity(i);
            }
        });

        // Audio Recording



        startRecordingButton = findViewById(R.id.activity_main_record);
        startRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecordingButton.setVisibility(GONE);
                stopRecordingButton.setVisibility(View.VISIBLE);
                startRecording();
            }
        });

        stopRecordingButton = findViewById(R.id.activity_main_stop);
        stopRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecordingButton.setVisibility(GONE);
                startRecordingButton.setVisibility(GONE);
                playRecordingButton.setVisibility(View.VISIBLE);
                stopRecording();
            }
        });

        playRecordingButton = findViewById(R.id.activity_main_play);
        playRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRecordingButton.setVisibility(GONE);
                stopPlayingButton.setVisibility(View.VISIBLE);
                playRecording();
            }
        });

        stopPlayingButton = findViewById(R.id.activity_main_stop_playing);
        stopPlayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlayingButton.setVisibility(GONE);
                startRecordingButton.setVisibility(View.VISIBLE);
                stopPlaying();
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                audioRecordingPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }

        if (!audioRecordingPermissionGranted) {
            finish();
        }
    }

    private void startRecording() {
        String uuid = UUID.randomUUID().toString();
        fileName = getFilesDir().getPath() + "/" + uuid + ".3gp";
        Log.i(MainActivity.class.getSimpleName(), fileName);

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(MainActivity.class.getSimpleName() + ":startRecording()", "prepare() failed");
        }

        recorder.start();

        startRecorderService();
    }

    private void stopRecording() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
            stopRecorderService();
        }
    }

    private void playRecording() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopPlaying();
                }
            });
            player.prepare();
            player.start();
            startPlayerService();
        } catch (IOException e) {
            Log.e(MainActivity.class.getSimpleName() + ":playRecording()", "prepare() failed");
        }
    }

    private void stopPlaying() {
        if (player != null) {
            player.release();
            player = null;
            stopPlayerService();
        }
    }

    private void startRecorderService() {
        Intent serviceIntent = new Intent(this, RecorderService.class);
        serviceIntent.putExtra("inputExtra", "Recording in progress");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopRecorderService() {
        Intent serviceIntent = new Intent(this, RecorderService.class);
        stopService(serviceIntent);
    }

    private void startPlayerService() {
        Intent serviceIntent = new Intent(this, PlayerService.class);
        serviceIntent.putExtra("inputExtra", "Playing recording");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopPlayerService() {
        Intent serviceIntent = new Intent(this, PlayerService.class);
        stopService(serviceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.useApp:
                Intent i = new Intent(MainActivity.this, LearnAppActivity.class);
                startActivity(i);
                return true;
            case R.id.aboutUS:
                Intent it = new Intent(MainActivity.this, AboutUsActivity.class);
                startActivity(it);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

