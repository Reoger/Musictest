package com.create.musictest;



import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.create.musictest.Inface.IMusic;
import com.create.musictest.Inface.IsMusicOver;
import com.create.musictest.Utils.LogUtils;
import com.create.musictest.Utils.MyAdapter;
import com.create.musictest.Utils.Utils;
import com.create.musictest.View.NextActivity;
import com.create.musictest.mode.Sequence;
import com.create.musictest.service.ServiceForMusic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private ListView mMusicList;
    private MyAdapter adaper;
    private boolean mIsMusicPlaying = true;
    private IMusic binder;
    private ArrayList<HashMap<String,Object>> mMusicData = new ArrayList<HashMap<String,Object>>();
    private Intent in;
    private static final String ACTION = "com.create.musictest.service";
    private SeekBar mSeekBar;
    private int mCurrSongIndex = 0;//用于记录当前歌曲的索引
    private static final String TAG = "MainActivity";
    private ImageView mImagePause;
    private ImageButton mImageButtonOrder;//用于判断当前是随机播放，顺序播放和循环播放
    private String mCurrSongName;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initTitle();
        initIntent();
        mMusicList.setAdapter(adaper);
        mMusicList.setOnItemClickListener(this);


    }

    /**
     * 初始化title
     */
    private void initTitle() {
        mCurrSongName =  mMusicData.get(mCurrSongIndex).get("musicName") + "";
        toolbar.setTitle(mCurrSongName);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ab_android);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NextActivity.class);
                startActivity(intent);
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "你点击了我", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 初始化服务
     */
    private void initIntent() {
        if(mMusicData.size()>0){
            in = new Intent();
            // in.setAction(ACTION);
            in.setClass(this, ServiceForMusic.class);
            in.putExtra("path", mMusicData.get(mCurrSongIndex).get("path") + "");
            startService(in);//隐式启动service
            bindService(in,conn, Context.BIND_AUTO_CREATE);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(1000);
                    if(binder != null){
                        binder.init(mSeekBar, new IsMusicOver() {
                            @Override
                            public void onMusicOver() {
                                nextMusic(null);//自动播放下一曲


                                LogUtils.e(TAG, "这是在主函数里面的的自动播放下一曲");

                            }
                        });
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }).start();

    }

    ServiceConnection conn = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (IMusic)service;

            LogUtils.d(TAG,binder+"");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.d(TAG,"onServiceDisconnected");
        }
    };

    /**
     * 初始化控件,进行绑定
     */
    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mMusicList = (ListView)findViewById(R.id.musicList);
        mMusicData = Utils.getDataFromSD(this);
        adaper = new MyAdapter(this,mMusicData);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar1);
        mImagePause = (ImageView)findViewById(R.id.ic_pause);
        mImageButtonOrder = (ImageButton) findViewById(R.id.img_zj);
        mCurrSongIndex = Utils.getDate(this);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void playMusic(View view){

            if (mIsMusicPlaying) {//当前在正在播放歌曲
               binder.pauseMusic();//暂停播放
                mImagePause.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.pause));
                LogUtils.d(TAG, "当前正在播放，点击暂停了");
                mIsMusicPlaying = false;

            } else {
                binder.resumeMusic();
                LogUtils.e(TAG, "当前没有播放，点击播放了");
               // binder.startMusic();
                mImagePause.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.play));
                mIsMusicPlaying = true;
            }
    }

    public void upMusic(View view){
        mCurrSongIndex--;
        if(mCurrSongIndex<=0){
            mCurrSongIndex = 0;
            Toast.makeText(MainActivity.this,"这已经是第一首歌了",Toast.LENGTH_SHORT).show();
            LogUtils.d(TAG,"这已经是第一首歌了");
            return;
        }
        if(in != null && binder != null){
            binder.restMusic();
            binder.removeMusic();
            binder.stopMusic();
            unbindService(conn);
            stopService(in);
        }

        initIntent();
        if(onlyPlayMusic()){
            LogUtils.d(TAG, "下一曲");
        }
    }

    public void nextMusic(View view){

        switch(status){
            case CYCLE:
                mCurrSongIndex++;
                Toast.makeText(MainActivity.this,"顺序播放",Toast.LENGTH_SHORT).show();
                break;
            case RANDER://单曲循环
                Toast.makeText(MainActivity.this,"单曲循环",Toast.LENGTH_SHORT).show();
                break;
            case OREDR://随机播放
               int temp = (int)(0+Math.random()*(mMusicData.size()-0+1));
                if(temp==mCurrSongIndex){
                    mCurrSongIndex =1+temp;
                }else{
                    mCurrSongIndex = temp;
                }
                LogUtils.d(TAG,mCurrSongIndex+" mMusicData.size()"+mMusicData.size());
                Toast.makeText(MainActivity.this,"随机播放"+mCurrSongIndex,Toast.LENGTH_SHORT).show();
                break;
        }

        if(mCurrSongIndex>=mMusicData.size()){
            Toast.makeText(MainActivity.this,"这已经是最后一首歌了"+mCurrSongIndex,Toast.LENGTH_SHORT).show();
            mCurrSongIndex = 0;
        }
        if(in != null && binder != null){
            binder.restMusic();
            binder.stopMusic();
            binder.removeMusic();
            unbindService(conn);
            stopService(in);
        }

        initIntent();
        if(onlyPlayMusic()){
            LogUtils.d(TAG, "下一曲");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.cancelNotication(MainActivity.this);
        Toast.makeText(MainActivity.this,"我又满血复活了",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.showNotification(MainActivity.this, mMusicData.get(mCurrSongIndex).get("musicName") + "");
        Utils.saveDate(mCurrSongIndex, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //这里需要添加保存数据的逻辑代码
        Utils.saveDate(mCurrSongIndex, this);
    }

    public boolean onlyPlayMusic(){
        binder.startMusic();
        mImagePause.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.play));
        mIsMusicPlaying = true;
        mCurrSongName =  mMusicData.get(mCurrSongIndex).get("musicName") + "";
        toolbar.setTitle(mCurrSongName);
        Utils.showNotification(MainActivity.this, mCurrSongName);
        return true;
    }

    /**
     * 点击歌曲时候的处理逻辑
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mCurrSongIndex = position;
        if(in != null && binder != null){
            binder.restMusic();
            binder.removeMusic();
            binder.stopMusic();
            unbindService(conn);
            stopService(in);
        }
        initIntent();
        if (onlyPlayMusic()){
            LogUtils.d(TAG,"text");
        }
    }

    Sequence status = Sequence.OREDR;
    /**
     * 判断是循环播放，循序播放还是随机播放
     */
    public void sequence(View view){//有三个状态

        switch(status){
            case OREDR:
                mImageButtonOrder.setBackgroundResource( R.drawable.order);
                status = Sequence.CYCLE;
                LogUtils.i(TAG,"当前的状态是"+status+"CYCLE");
                break;
            case CYCLE:
                mImageButtonOrder.setBackgroundResource(R.drawable.cycle);
                status = Sequence.RANDER;
                LogUtils.i(TAG,"当前的状态是"+status+"RANDER");
                break;
            case RANDER:
                status = Sequence.OREDR;
                mImageButtonOrder.setBackgroundResource(R.drawable.random);
                LogUtils.i(TAG, "当前的状态是" + status+"OREDR");
                break;
        }
    }


}
