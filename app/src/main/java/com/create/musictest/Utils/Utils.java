package com.create.musictest.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.style.ForegroundColorSpan;

import com.create.musictest.BuildConfig;
import com.create.musictest.MainActivity;
import com.create.musictest.R;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by 24540 on 2016/4/19.
 */
public class Utils {
    static int mSize = 0;


    /**
     * 获取手机和sd卡目录内的所有歌曲信息，
     *
     * @return
     */
    public static ArrayList<HashMap<String, Object>> getDataFromSD(Context context) {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

        ContentResolver musicResolcer = context.getContentResolver();
        Cursor musicCursor = musicResolcer.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Audio.Media.SIZE + ">8000", null, null);
        int musicColumnIndex;
        if (musicCursor != null && musicCursor.getCount() > 0) {
            for (musicCursor.moveToFirst(); !musicCursor.isAfterLast();
                 musicCursor.moveToNext()) {
                HashMap<String, Object> item = new HashMap<String, Object>();
                musicColumnIndex = musicCursor.getColumnIndex(
                        MediaStore.Audio.AudioColumns._ID);
                int musicRating = musicCursor.getInt(musicColumnIndex);
                item.put("musicRating", musicRating + "");
                //item.put("id",size+"");
                //取得音乐播放路径
                musicColumnIndex = musicCursor.getColumnIndex(
                        MediaStore.Audio.AudioColumns.DATA);
                item.put("musicPath", musicCursor.getString(musicColumnIndex));
                //获取音乐的名字
                musicColumnIndex = musicCursor.getColumnIndex(
                        MediaStore.Audio.AudioColumns.TITLE);
                item.put("musicName", musicCursor.getString(musicColumnIndex));
                //获取音乐的演唱者
                musicColumnIndex = musicCursor
                        .getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST);
                item.put("musicArtist", musicCursor.getString(musicColumnIndex));

                //获取歌曲的时间
                musicColumnIndex = musicCursor.getColumnIndex(
                        MediaStore.Audio.AudioColumns.DURATION);
                int musicTime = musicCursor.getInt(musicColumnIndex);//单位是毫秒
                LogUtils.e("Utils",musicTime+"时间");
                if(musicTime<59*1000){//过滤掉时间少于一分钟的歌曲
                    continue;
                }
                //
                // Time musicTime = new Time();
                // musicTime.set(musicTime);
                String readableTime = ":";
                int m = musicTime % 60000 / 1000;
                int o = musicTime / 60000;
                if (o == 0) {
                    readableTime = "00" + readableTime;
                } else if (0 < o && o < 10) {
                    readableTime = "0" + o + readableTime;
                } else {
                    readableTime = o + readableTime;
                }
                if (m < 10) {
                    readableTime = readableTime + "0" + m;
                } else {
                    readableTime = readableTime + m;
                }

                item.put("musicTime", readableTime);
                //获取歌曲的路径
                musicColumnIndex = musicCursor
                        .getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
                String path = musicCursor.getString(musicColumnIndex);
                item.put("path", path);

                data.add(item);
            }
        }
        mSize = data.size();
        return data;
    }

    /**
     * 显示通知
     */
    public static boolean showNotification(Context context,String songName) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context,MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification noti = new Notification.Builder(context)
                .setContentTitle("正在播放歌曲 ")
                .setContentText(songName)
                .setSmallIcon(R.drawable.ab_share)
                //.setLargeIcon(aBitmap)
                .setContentIntent(pi)
                .build();

        manager.notify(1,noti);
     //   service.startForeground(1,noti);
        return true;
    }
    /**
     * 取消通知
     */
    public static void cancelNotication(Context context){
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(1);
    }

    /**
     * 保存数据
     * @param index
     */
    public static void saveDate(int index,Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences("data",
                Context.MODE_PRIVATE).edit();
        editor.putInt("index",index);
        editor.commit();
    }

    /**
     * 读取数据
     * @return
     */
    public static int getDate(Context context){
       SharedPreferences pre  = context.getSharedPreferences("data",Context.MODE_PRIVATE);
        int a = pre.getInt("index",0);
        return a;
    }
}
