package com.create.musictest.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.SeekBar;

import com.create.musictest.Inface.IMusic;
import com.create.musictest.Inface.IsMusicOver;
import com.create.musictest.Utils.LogUtils;
import com.create.musictest.Utils.Utils;

/**
 * Created by 24540 on 2016/4/21.
 */
public class ServiceForMusic extends Service{
    private String mSongPath;
    private MediaPlayer mMediaPlayer;
    /**正在播放音乐为true，否则false*/
    private boolean mIsPlaying= false;
    private static final String TAG = "ServiceForMusic";



    @Override
    public IBinder onBind(Intent intent){
        mSongPath = intent.getStringExtra("path");
        LogUtils.e(TAG, "path" + mSongPath);
        return new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMediaPlayer = new MediaPlayer();
        LogUtils.e(TAG,"服务开启");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }



    class MyBinder extends Binder implements IMusic{
        private SeekBar mSeekBar;
        Handler handler  = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //更新进度条
                mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                handler.postDelayed(runnable,500);
            }
        };

        @Override
        public void pauseMusic() {
            if(mIsPlaying){
                LogUtils.e(TAG,"暂停当前音乐"+mSongPath);
                mMediaPlayer.pause();
                mIsPlaying = false;
            }
        }

        /**
         *  继续播放
         */
        @Override
        public void resumeMusic() {
            if(!mIsPlaying){
                LogUtils.e(TAG,"继续播放当前音乐"+mSongPath);
                mMediaPlayer.start();
                mIsPlaying = true;
            }
        }

        @Override
        public void stopMusic() {
            if(mIsPlaying){
                LogUtils.e(TAG,"停止当前音乐"+mSongPath);
                mMediaPlayer.stop();
                mIsPlaying = false;
            }
        }

        @Override
        public void startMusic() {
            if (!mIsPlaying){
                initMediaPlayer();
            }
            mMediaPlayer.start();
            mIsPlaying = true;
        }

        @Override
        public void init(SeekBar seekBar, final IsMusicOver over) {
            LogUtils.e(TAG,"初始化进度条");
            this.mSeekBar = seekBar;
            mMediaPlayer.reset();
            //设置播放资源
            try{
                 mMediaPlayer.setDataSource(mSongPath);
                mMediaPlayer.prepare();
                seekBar.setMax(mMediaPlayer.getDuration());
                mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {//是否是用户操作
                            mMediaPlayer.seekTo(progress);
                            LogUtils.e(TAG, "我是seekBar方法，我执行了,在线程中被开启");
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                mMediaPlayer.start();
                mIsPlaying = true;
                handler.post(runnable);
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        LogUtils.e(TAG,"歌曲播放完毕");
                        //在此添加歌曲播放完毕的逻辑代码
                        over.onMusicOver();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        /**
         * 将 MediaPlayer对象重置到刚刚创建的状态
         */
        @Override
        public void restMusic() {
            mMediaPlayer.reset();

            LogUtils.e(TAG,"重置MediaPlayer控件");
        }

        /**
         * 停止当前线程
         */
        @Override
        public void removeMusic() {
            handler.removeCallbacks(runnable);
            LogUtils.e(TAG,"停止当前线程");
        }
    }

    private void initMediaPlayer(){
        try{
            mMediaPlayer.setDataSource(mSongPath);
            mMediaPlayer.reset();
            mMediaPlayer.prepare();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}


