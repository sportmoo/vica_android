package com.sportmoo.vica_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.RelativeLayout;

/**
 * Created by Farlshan on 14-7-29.
 */
public class PlayerActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "android-tutorial02";
    private SurfaceView mSurfaceView;
    private int m_frequency;// 采样率
    private int m_channel; // 声道
    private int m_sampBit; // 采样精度
    private AudioTrack mAudioTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mSurfaceView.getHolder().addCallback(this);
        if (false == startPlay()) {
            Intent intent = new Intent();//数据是使用Intent返回
            this.setResult(100, intent);//设置返回数据
            this.finish();
        }
    }

    private boolean startPlay() {
        Intent playerIntent = getIntent();
        String videoPath = playerIntent.getStringExtra("videoPath");
        Log.d(TAG, "streampath：" + videoPath);
        int initRet = naInit(videoPath);
        Log.d(TAG, "initResult:" + initRet);
        if (initRet < 0) {
            return false;
        }
        int[] playerRes = getPlayResolution();
        int[] surfaceRes = getScreenRes();
        updateSurfaceView(playerRes[0], playerRes[1]);
        naSetup(playerRes[0], playerRes[1]);
        naPlay();
        return true;
    }

    private int[] getPlayResolution() {
        int[] res = naGetVideoRes();
        if (res == null) {
            return null;
        }
        int[] screenRes = getScreenRes();
        int width, height;
        float widthScaledRatio = screenRes[0] * 1.0f / res[0];
        float heightScaledRatio = screenRes[1] * 1.0f / res[1];
        if (widthScaledRatio > heightScaledRatio) {
            //use heightScaledRatio
            width = (int) (res[0] * heightScaledRatio);
            height = screenRes[1];
        } else {
            //use widthScaledRatio
            width = screenRes[0];
            height = (int) (res[1] * widthScaledRatio);
        }
        width = width - width % 16;
        height = height - height % 16;
        Log.d(TAG, "width " + width + ",height:" + height);
        return new int[]{width, height};
    }

    private void updateSurfaceView(int pWidth, int pHeight) {
        //update surfaceview dimension, this will cause the native window to change
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
        params.width = pWidth;
        params.height = pHeight;
        mSurfaceView.setLayoutParams(params);
    }

    @SuppressLint("NewApi")
    private int[] getScreenRes() {
        int[] res = new int[2];
        Display display = getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 13) {
            Point size = new Point();
            display.getSize(size);
            res[0] = size.x;
            res[1] = size.y;
        } else {
            res[0] = display.getWidth();  // deprecated
            res[1] = display.getHeight();  // deprecated
        }
        return res;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Log.d(TAG, "surfacechanged: " + width + ":" + height);
        naSetSurface(holder.getSurface());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        naStop();
        naSetSurface(null);
    }


    //构造
//此时 frequency=44100, channel=1,
//自行设置的sampbit=SL_PCMSAMPLEFORMAT_FIXED_16即16 因为没有32
    private void setAudio(int frequency, int channel, int sampbit) {
        m_frequency = frequency;
        switch (channel) {
            case 0:
                m_channel = AudioFormat.CHANNEL_INVALID;
                break;
            case 1:
                m_channel = AudioFormat.CHANNEL_OUT_MONO;
                break;
            case 2:
                m_channel = AudioFormat.CHANNEL_OUT_STEREO;
                break;
            default:
                m_channel = AudioFormat.CHANNEL_OUT_DEFAULT;
                break;
        }

        switch (sampbit) {
            case 0:
                m_sampBit = AudioFormat.ENCODING_INVALID;
                break;
            case 8:
                m_sampBit = AudioFormat.ENCODING_PCM_8BIT;
                break;
            case 16:
                m_sampBit = AudioFormat.ENCODING_PCM_16BIT;
                break;
            default:
                m_sampBit = AudioFormat.ENCODING_DEFAULT;
                break;
        }
    }

    public void audioTrackInit() {
        // 获得构建对象的最小缓冲区大小
        int minBufSize = AudioTrack.getMinBufferSize(m_frequency, m_channel, AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, m_frequency,
                m_channel, AudioFormat.ENCODING_PCM_16BIT, minBufSize * 2, AudioTrack.MODE_STREAM);
        mAudioTrack.play();
    }

    private static native int naInit(String pFileName);

    private static native int[] naGetVideoRes();

    private static native void naSetSurface(Surface pSurface);

    private static native int naSetup(int pWidth, int pHeight);

    private static native void naPlay();

    private static native void naStop();

    private static native byte[] naGetAudioBuffer();

    private static native int[] naGetAudioParams();

    static {
        System.loadLibrary("avutil-52");
        System.loadLibrary("avcodec-55");
        System.loadLibrary("avformat-55");
        System.loadLibrary("swscale-2");
        System.loadLibrary("player");
    }
}
