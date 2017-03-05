package com.zhouas.creazymoly;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by zhouas on 2017/3/4.
 */

public class PlayActivity extends AppCompatActivity {

    private ImageView mouse;
    private ImageView boom;
    private ImageView hunter;
    private TextView time;
    private LinearLayout easyLayout;

    private SharedPreferences sharedPreferences;

    private MediaPlayer mediaPlayerKick;
    private MediaPlayer mediaPlayerStart;

    private int count = 0;               //打到的数目
    private int playtime = 6000;         //游戏时间
    private Thread thread = null;
    private boolean normal = true;       //是否正常结束游戏
    private Handler handler = null;      //
    private int[][] position = new int[][]{
            {787, 567}, {263, 450}, {431, 70}, {1487, 520}, {515, 232}, {960, 232}, {809, 382}, {1271, 532}, {625, 532}
    };                                   //地鼠洞坐标

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏

        setContentView(R.layout.layout_play_easy);
        initView();

        easyLayout.setOnTouchListener(new View.OnTouchListener() {   //移动锤子的位置
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hunter.setX(motionEvent.getX());
                hunter.setY(motionEvent.getY());

                mediaPlayerKick.start();
                return false;
            }
        });
        mouse.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.setVisibility(View.INVISIBLE); //设置地鼠不显示
                int[] location = new int[2];
                mouse.getLocationOnScreen(location);
                hunter.setVisibility(View.INVISIBLE);//锤子消失
                boom.setX(location[0]);            //显示被打图片
                boom.setY(location[1]);
                boom.setVisibility(View.VISIBLE);
                count++;
                return false;
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int index = 0;
                if (playtime > 0 && normal == true) {
                    if (msg.what == 0x101) {
                        time.setText("剩余时间为：" + playtime / 100 + "   打到地鼠：" + count + "只");
                        index = msg.arg1;
                        mouse.setX(position[index][0]);         //设置地鼠出现位置，即为鼠洞坐标
                        mouse.setY(position[index][1]);
                        boom.setVisibility(View.INVISIBLE);     //被打图消失
                        hunter.setVisibility(View.VISIBLE);     //锤子出现

                        mouse.setVisibility(View.VISIBLE);      //设置地鼠显示

                        mediaPlayerStart.start();
                    }
                    super.handleMessage(msg);
                } else if (normal == true) {
                    gameOver();
                }
            }
        };

        thread = new Mythred();
        thread.start();

    }

    private void initView() {
        mouse = (ImageView) findViewById(R.id.mouse);
        boom = (ImageView) findViewById(R.id.boom);
        hunter = (ImageView) findViewById(R.id.hunter);
        time = (TextView) findViewById(R.id.time);
        easyLayout=(LinearLayout) findViewById(R.id.easyLayout);

        mediaPlayerKick = MediaPlayer.create(getBaseContext(), R.raw.kick);
        mediaPlayerStart = MediaPlayer.create(getBaseContext(), R.raw.start);

        sharedPreferences=getSharedPreferences("user",MODE_PRIVATE);
    }

    @Override
    //非正常退出
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(PlayActivity.this).setTitle("结束游戏").setMessage("您确定要退出吗？")
                    .setNeutralButton("否",null)
                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            normal = false;
                            mediaPlayerKick.release();
                            mediaPlayerStart.release();
                            PlayActivity.this.finish();
                        }
                    }).create().show();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void gameOver() {
        time.setText("时间到！");
        handler.removeCallbacks(thread);
        thread = null;
        new AlertDialog.Builder(PlayActivity.this).setTitle("游戏结束").setMessage("您一共打了 " + count + "只地鼠")
                .setNegativeButton("再来一局", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        thread = new Mythred();

                        recordGoal();   //更新最高记录

                        playtime = 6000;
                        count=0;
                        normal=true;
                        thread.start();
                    }
                }).setNeutralButton("返回", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                PlayActivity.this.finish();
                recordGoal();
            }
        }).create().show();
}

    private void recordGoal() {
        if(sharedPreferences.getInt("goal",0)<count){
            SharedPreferences.Editor editor =sharedPreferences.edit();
            editor.putInt("goal",count);
            editor.commit();
        }

        mediaPlayerKick.release();
        mediaPlayerStart.release();
    }

    public class Mythred extends Thread {

        @Override
        public void run() {
            super.run();

            int index = 0;
            while (!Thread.currentThread().isInterrupted()) {
                index = new Random().nextInt(position.length);
                Message m = handler.obtainMessage();
                m.what = 0x101;
                m.arg1 = index;
                handler.sendMessage(m);
                if (playtime > 0 && normal) {
                    try {

                        if(playtime>4000){
                            Thread.sleep(new Random().nextInt(1000) + 1000);//   休眠一段时间
                        }
                        else if(playtime>2000){
                            Thread.sleep(new Random().nextInt(500) + 1000);//
                        }
                        else{
                            Thread.sleep(new Random().nextInt(500) + 500);//   不同等级
                        }
                        playtime -= 100;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    thread.interrupt();
                }
            }
        }

    }
}