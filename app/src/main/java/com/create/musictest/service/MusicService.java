package com.create.musictest.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.SeekBar;
import android.widget.Toast;

import com.create.musictest.Inface.IMusic;
import com.create.musictest.Inface.IsMusicOver;
import com.create.musictest.Utils.LogUtils;
import com.create.musictest.mode.Music;


/**
 * 开启线程 播放选中曲目
 * Created by Reo on 2016/4/20.
 */
public class MusicService extends Service {
    private MediaPlayer mMediaPlayer;
    private String mSongPath;
    private boolean mIsPlaying = false;//判断当前是否在播放音乐
    private Music mMusic;

    @Override
    public IBinder onBind(Intent intent) {
        mSongPath = intent.getStringExtra("path");
        LogUtils.d("mSongPath", mSongPath + "");
      //  return new MyBinder();
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d("onCreate", "onCreate");
        try {
            //得到媒体播放器
            mMediaPlayer = new MediaPlayer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int retVal = super.onStartCommand(intent, flags, startId);
        LogUtils.e("onStartCommand",retVal+"");
        Toast.makeText(this,"retVal来自服务"+retVal,Toast.LENGTH_SHORT).show();
        return retVal;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        LogUtils.d("onDestry", "onDestry");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtils.d("onUnbind", "onUnbind");
        return super.onUnbind(intent);
    }

    /**
     * 暂停
     */
    public void pause() {
        mMediaPlayer.pause();
    }

    /**
     * 继续
     */
    public void resume() {
        mMediaPlayer.start();//start方法表示开始或者继续播放音频
    }

    class MyBinder extends Binder implements IMusic {
        private SeekBar seekBar;
        android.os.Handler handler = new android.os.Handler();


        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                // 更新进度条
                if (mMediaPlayer.isPlaying()) {
                    seekBar.setProgress(mMediaPlayer.getCurrentPosition());
                }
                handler.postDelayed(runnable, 500);
                //使用PostDelayed方法，0.5秒后调用此Runnable对象
            }
        };


        /**暂停*/
        @Override
        public void pauseMusic() {
            if(!mIsPlaying){
                mIsPlaying = false;
                //调用本地MyService的暂停
                pause();
            }

        }

        /**
         * 重新加载
         */
        @Override
        public void resumeMusic() {
            resume();
        }

        @Override
        public void stopMusic() {
            if (mMediaPlayer!= null&&mMediaPlayer.isPlaying()){
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }

        @Override
        public void startMusic() {
            initMediaPlayer();
            mMediaPlayer.start();
        }

        @Override
        public void init(SeekBar seekBar, final IsMusicOver mIsMusice) {
            this.seekBar = seekBar;
            mMediaPlayer.reset();
            //重置
            try{
                mMediaPlayer.setDataSource(mSongPath);
                mMediaPlayer.prepare();
                seekBar.setMax(mMediaPlayer.getDuration());
            }catch (Exception e){
                e.printStackTrace();
            }
            //设置拖动进度条改变的时候的监听方法
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        mMediaPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            //设置进度
            mMediaPlayer.start();
            mIsPlaying = true;
            handler.post(runnable);

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //播放结束实现的逻辑
                    mIsMusice.onMusicOver();
                }
            });

        }

        @Override
        public void restMusic() {
            try{
                mMediaPlayer.release();
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        @Override
        public void removeMusic() {
            handler.removeCallbacks(runnable);
        }
    }

    private void initMediaPlayer(){
        try{
            mMediaPlayer.setDataSource(mSongPath);
            mMediaPlayer.prepare();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
