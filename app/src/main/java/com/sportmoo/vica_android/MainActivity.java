package com.sportmoo.vica_android;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
    private static final String TAG = "android-ffmpeg-tutorial02";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Button btnStart = (Button) this.findViewById(R.id.buttonStart);
        final EditText streamAddress = (EditText) findViewById(R.id.streamAddress);
        streamAddress.setText("rtsp://192.168.1.1:8557/2?videoCodecType=H.264");
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String videoPath = streamAddress.getText().toString();
                Intent playerIntent = new Intent();
                playerIntent.putExtra("videoPath", videoPath);
                playerIntent.setClass(MainActivity.this, PlayerActivity.class);
                startActivityForResult(playerIntent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 100) {
            new AlertDialog.Builder(this)
                    .setTitle("播放失败")
                    .setMessage("请确认网络是否已连接")
                    .setPositiveButton("关闭", null)
                    .show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
