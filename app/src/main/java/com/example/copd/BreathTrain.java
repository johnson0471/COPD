package com.example.copd;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class BreathTrain extends AppCompatActivity {
    private Button piBtn, peBtn, aiBtn, aeBtn, playBtn;   //e:exercise a:abdomen p:pouting i:illustrate
    private TextView onplayTxv, totalTxv, progressTxv;
    private SeekBar musicSb;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breath_train);

        val();
        setListener();
    }

    private void val() {
        mediaPlayer = MediaPlayer.create(this, R.raw.pipe);
        piBtn = findViewById(R.id.btn_pi);
        peBtn =  findViewById(R.id.btn_pe);
        aiBtn =  findViewById(R.id.btn_ai);
        aeBtn =  findViewById(R.id.btn_ae);
        playBtn =  findViewById(R.id.btn_play);

        onplayTxv = findViewById(R.id.txv_onplay);
        totalTxv = findViewById(R.id.txv_total);
        progressTxv = findViewById(R.id.txv_progress);

        musicSb = findViewById(R.id.sb);
    }

    private void setListener() {
        piBtn.setOnClickListener(listener);
        peBtn.setOnClickListener(listener);
        aiBtn.setOnClickListener(listener);
        aeBtn.setOnClickListener(listener);
        playBtn.setOnClickListener(listener);
    }

    private void setMusic() {
        totalTxv.setText(convert(mediaPlayer.getDuration()));
        musicSb.setMax(mediaPlayer.getDuration());
        musicSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                //mediaPlayer.start();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }
        });

    }

    private void doPlay() { //mode:  0:restart, 1:pause, 2:start
        playBtn.setText("||");
        mediaPlayer.reset();
        switch (onplayTxv.getText().toString()) {
            case "噘嘴式呼吸說明":
                mediaPlayer = MediaPlayer.create(this, R.raw.pipe);
                break;

            case "噘嘴式呼吸練習":
                mediaPlayer = MediaPlayer.create(this, R.raw.poue);
                break;

            case "腹式呼吸說明":
                mediaPlayer = MediaPlayer.create(this, R.raw.aiae);
                break;

            case "腹式呼吸練習":
                mediaPlayer = MediaPlayer.create(this, R.raw.abde);
                break;

        }
        setMusic();
        new Thread() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }//一秒
                    Message msg =
                            handler.obtainMessage();
                    msg.what = 0x123;
                    handler.sendMessage(msg);
                }
            }

            ;
        }.start();
        mediaPlayer.start();


    }

    //點擊歌曲即可撥放，且原歌曲會停止。
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_pi:
                    onplayTxv.setText(piBtn.getText());
                    doPlay();
                    break;

                case R.id.btn_pe:
                    onplayTxv.setText(peBtn.getText());
                    doPlay();
                    break;

                case R.id.btn_ai:
                    onplayTxv.setText(aiBtn.getText());
                    doPlay();
                    break;

                case R.id.btn_ae:
                    onplayTxv.setText(aeBtn.getText());
                    doPlay();
                    break;

                case R.id.btn_play:
                    if (playBtn.getText().toString().equals("||")) { //pause
                        mediaPlayer.pause();
                        playBtn.setText("▶");
                    } else {                                          //start
                        mediaPlayer.start();
                        playBtn.setText("||");
                    }

            }
        }
    };

    private Handler handler = new Handler() {
        //收到Handler发回的消息被回调
        public void handleMessage(Message msg) {
            //更新UI组件
            if (msg.what == 0x123) {
                try {
                    int current = mediaPlayer.getCurrentPosition();
                    musicSb.setProgress(current);
                    progressTxv.setText(convert(current));
                } catch (Exception e) {

                }
            }
        }
    };


    private String convert(int duration) {

        //总秒
        int second = duration / 1000;
        //总分
        int minute = second / 60;
        //剩余秒数
        int miao = second % 60;
        if (miao < 10) {
            return minute + ":0" + miao;
        }

        return minute + ":" + miao;


    }

    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }


}