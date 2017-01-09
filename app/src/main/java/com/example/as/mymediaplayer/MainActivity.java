package com.example.as.mymediaplayer;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {
    private Display currDisplay;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private MediaPlayer player;
    private int vWidth,vHeight;
    private Timer timer;
    private ImageButton rew;
    private ImageButton pasue;
    private ImageButton start;
    private ImageButton ff;
    private TextView play_time;
    private TextView all_time;
    private TextView title;
    private SeekBar seekbar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,1,0,"文件夹");
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //获取传过来的媒体路径
        Intent intent=getIntent();
        Uri uri=intent.getData();
        String mPath="";
        if(uri!=null){
            mPath=uri.getPath();
        }else{
            Bundle localBundle=getIntent().getExtras();
            if(localBundle!=null){
                String t_path = localBundle.getString("path");
                if (t_path!=null&&!"".equals(t_path)){
                    mPath=t_path;
                }
            }
        }
        //加载当前布局文件空间操作
        title= (TextView) findViewById(R.id.title);
        surfaceView= (SurfaceView) findViewById(R.id.surfaceview);
        rew= (ImageButton) findViewById(R.id.rew);
        pasue= (ImageButton) findViewById(R.id.pause);
        start= (ImageButton) findViewById(R.id.start);
        ff= (ImageButton) findViewById(R.id.ff);
        play_time= (TextView) findViewById(R.id.play_time);
        all_time= (TextView) findViewById(R.id.all_time);
        seekbar= (SeekBar) findViewById(R.id.seekbar);

        //给Surface添加CallBack监听
        holder=surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //当Surfaceview中的Surface被创建时调用
                //在这里我们制定MediaPlayer在当亲的surface中进行播放
                player.setDisplay(holder);
                //在指定了MediaPlayer播放的容器后，我们就可以使用prepare或者prepareAsynv来准备播放了
                player.prepareAsync();
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }
        });
        //为了可以播放或者使用Camera预览，我们需要指定其Buffer类型
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //下面开始实例化MediaPlayer对象
        player=new MediaPlayer();
        //设置播放完成监听器
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //当MediaPlayer播放完成后触发
                if(timer!=null){
                    timer.cancel();
                    timer=null;
                }
            }
        });
        //设置prepare完成监听器
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //当prepare完成后，该方法触发，在这里我饿美女播放视频
                //首先取得video的宽和高
                vWidth=player.getVideoWidth();
                vHeight=player.getVideoHeight();

                if (vWidth>currDisplay.getWidth()||vHeight>currDisplay.getHeight()){
                    //如果video的宽或者高超出了当前屏幕的大笑，则要进行缩放
                    float wRatio=(float)vWidth/(float)currDisplay.getWidth();
                    float hRatio=(float)vHeight/(float)currDisplay.getHeight();
                    //选择大的一个进行缩放
                    float ratio=Math.max(wRatio,hRatio);
                    vWidth=(int)Math.ceil((float)vWidth/ratio);
                    vHeight=(int)Math.ceil((float)vHeight/ratio);
                    //设置surfaceView的布局参数
                    surfaceView.setLayoutParams(new LinearLayout.LayoutParams(vWidth,vHeight));
                    //然后开始播放视频
                    player.start();
                }else{
                    player.start();
                }
                if(timer!=null){
                    timer.cancel();
                    timer=null;
                }
                //启动时间更新及进度条更新任务每0.5秒更新一次
                timer=new Timer();
                timer.schedule(new MyTask(),50,500);
            }
        });
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{
            if (!mPath.equals("")){
                title.setText(mPath.substring(mPath.lastIndexOf("/")+1));
                player.setDataSource(mPath);
            }else{
                AssetFileDescriptor afd=this.getResources().openRawResourceFd(R.raw.exodus);
                player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getDeclaredLength());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //暂停操作
        pasue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //貌似有问题这里

                pasue.setVisibility(View.GONE);
                start.setVisibility(View.VISIBLE);
                player.pause();
                if(timer!=null){
                    timer.cancel();
                    timer=null;
                }
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //貌似有问题这里

                pasue.setVisibility(View.GONE);
                start.setVisibility(View.VISIBLE);

                player.start();
                if(timer!=null){
                    timer.cancel();
                    timer=null;
                }

                timer=new Timer();
                timer.schedule(new MyTask(),50,500);
            }
        });

        rew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(player.isPlaying()){
                    int cuttentPosition=player.getCurrentPosition();
                    if(cuttentPosition-10000>0){
                        player.seekTo(cuttentPosition-10000);
                    }
                }
            }
        });
        ff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(player.isPlaying()) {
                    int cuttentPosition = player.getCurrentPosition();
                    if (cuttentPosition + 10000 < player.getDuration()) {
                        player.seekTo(cuttentPosition + 10000);
                    }
                }
            }
        });

        currDisplay=this.getWindowManager().getDefaultDisplay();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==1){
            Intent intent=new Intent(this,MyFileActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    public class MyTask extends TimerTask{
        public void run(){
            Message message=new Message();
            message.what=1;

            handler.sendMessage(message);
        }
    }

    public final Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:

                    Time progress=new Time(player.getCurrentPosition());
                    Time allTime=new Time(player.getDuration());
                    String timeStr=progress.toString();
                    String timeStr2=allTime.toString();

                    play_time.setText(timeStr.substring(timeStr.indexOf(":")+1));

                    all_time.setText(timeStr2.substring(timeStr2.indexOf(":")+1));
                    int progressValue=0;
                    if (player.getDuration()>0){
                        progressValue=seekbar.getMax()*
                                player.getCurrentPosition()/player.getDuration();
                    }
                    seekbar.setProgress(progressValue);
                    break;
            }
            super.handleMessage(msg);

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player!=null){
            player.stop();
        }
    }
}