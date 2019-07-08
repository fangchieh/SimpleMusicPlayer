package com.example.myapplication;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;

public class MainActivity extends AppCompatActivity {

    private MusicService musicService;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MyBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService = null;
        }
    };
    private SeekBar seekBar;
    private TextView musicStatus;
    private TextView musicTime;
    private Button btnPlayOrPause;

    private void bindServiceConnection() {
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        startService(intent);
        bindService(intent, sc, this.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.
                WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            Log.d("hint", "ready to new MusicService");
            musicService = new MusicService();
            Log.d("hint", "finish to new MusicService");
            bindServiceConnection();
        }
        seekBar = (SeekBar) this.findViewById(R.id.MusicSeekBar);
        seekBar.setProgress(musicService.mp.getCurrentPosition());
        seekBar.setMax(musicService.mp.getDuration());

        musicStatus = (TextView) this.findViewById(R.id.MusicStatus);
        musicTime = (TextView) this.findViewById(R.id.MusicTime);

        btnPlayOrPause = (Button) this.findViewById(R.id.BtnPlayorPause);

        Log.d("hint", Environment.getExternalStorageDirectory().getAbsolutePath() + "/You.mp3");
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/You.mp3");
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public android.os.Handler handler = new android.os.Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (musicService.mp.isPlaying()) {
                musicStatus.setText(getResources().getString(R.string.playing));
                btnPlayOrPause.setText(getResources().getString(R.string.pause).toUpperCase());
            } else {
                musicStatus.setText(getResources().getString(R.string.pause));
                btnPlayOrPause.setText(getResources().getString(R.string.play).toUpperCase());
            }
            SimpleDateFormat time = new SimpleDateFormat("mm:ss");
            musicTime.setText(time.format(musicService.mp.getCurrentPosition()) + "/"
                    + time.format(musicService.mp.getDuration()));
            seekBar.setProgress(musicService.mp.getCurrentPosition());
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        musicService.mp.seekTo(seekBar.getProgress());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            handler.postDelayed(runnable, 100);
        }
    };

    @Override
    protected void onResume() {
        if (musicService.mp.isPlaying()) {
            musicStatus.setText(getResources().getString(R.string.playing));
        } else {
            musicStatus.setText(getResources().getString(R.string.pause));
        }
        seekBar.setProgress(musicService.mp.getCurrentPosition());
        seekBar.setMax(musicService.mp.getDuration());
        handler.post(runnable);
        super.onResume();
        Log.d("hint", "handler post runnable");
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.BtnPlayorPause:
                musicService.playOrPause();
                break;
            case R.id.BtnStop:
                musicService.stop();
                seekBar.setProgress(0);
                break;
            case R.id.BtnQuit:
                handler.removeCallbacks(runnable);
                unbindService(sc);
                try {
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btnPre:
                musicService.preMusic();
                seekBar.setMax(musicService.mp.getDuration());
                break;
            case R.id.btnNext:
                musicService.nextMusic();
                seekBar.setMax(musicService.mp.getDuration());
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.
                        PERMISSION_GRANTED) {
                    Log.d("hint", "ready to new MusicService");
                    musicService = new MusicService();
                    Log.d("hint", "finish to new MusicService");
                    bindServiceConnection();
                } else {
                    Toast.makeText(this, "拒绝权限将无法使用程序",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    public void onDestroy() {
        unbindService(sc);
        super.onDestroy();
    }

}
