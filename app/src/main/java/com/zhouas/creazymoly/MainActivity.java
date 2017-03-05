package com.zhouas.creazymoly;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button exit;              //退出
    private Button btnEasy;           //easy模式
    private Button btnHard;

    private TextView goal;

    private CheckBox cbMusic;         //静音键
    private Intent intent=null;
    private SharedPreferences sharedPreferences;//记录

    private int MUSICselseted;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        initView();

        goal.setText("最高成就："+sharedPreferences.getInt("goal",0)+"只");

        judgeMusic();

    }

    private void initView() {
        exit = (Button) findViewById(R.id.exit);
        btnEasy = (Button) findViewById(R.id.btn_easy);
        btnHard = (Button) findViewById(R.id.btn_hard);

        goal = (TextView) findViewById(R.id.goal);
        cbMusic=(CheckBox) findViewById(R.id.cb_music);

        exit.setOnClickListener(this);
        btnEasy.setOnClickListener(this);
        btnHard.setOnClickListener(this);

        intent =new Intent(MainActivity.this, MusicService.class);//背景音乐服务

        sharedPreferences=getSharedPreferences("user",MODE_PRIVATE);


    }

    private void judgeMusic() {

        MUSICselseted=sharedPreferences.getInt("music",0);
        if(MUSICselseted==0){
            cbMusic.setChecked(false);
            startService(intent);
        }
        else{
            cbMusic.setChecked(true);
        }

        cbMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (cbMusic.isChecked()) {

                    stopService(intent);

                } else {

                    startService(intent);

                }
            }
        });

    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exit:
               exit();
                break;
            case R.id.btn_easy:
                MainActivity.this.startActivity(new Intent(MainActivity.this,PlayActivity.class));
                break;
            case R.id.btn_hard:

                break;
        }
    }

    private void exit() {
        new AlertDialog.Builder(MainActivity.this).setTitle("Tips").
                setMessage("是否退出？").setNeutralButton("取消",null).
                setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.finish();
                        stopService(intent);

                        if(cbMusic.isChecked()){
                            SharedPreferences.Editor editor =sharedPreferences.edit();
                            editor.putInt("music",1);
                            editor.commit();
                        }
                        else if(!cbMusic.isChecked()){
                            SharedPreferences.Editor editor =sharedPreferences.edit();
                            editor.putInt("music",0);
                            editor.commit();
                        }
                    }
                }).create().show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            exit();
        }
        return super.onKeyDown(keyCode, event);
    }
}


