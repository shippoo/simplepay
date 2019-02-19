package com.sjk.simplepay;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hdl.logcatdialog.LogcatDialog;
import com.sjk.simplepay.po.DataSynEvent;
import com.sjk.simplepay.utils.PayUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.List;

import static com.sjk.simplepay.ServiceMain.mIsAlipayRunning;
import static com.sjk.simplepay.ServiceMain.mIsRunning;
import static com.sjk.simplepay.ServiceMain.mIsUnionpayRunning;
import static com.sjk.simplepay.ServiceMain.mIsWechatRunning;

public class MainActivity extends AppCompatActivity {

    private Intent serviceIntent;
    private Button btnStart;

    private GradientDrawable btnStartDrawable;
    private static DrawableSwitch alipaySwitch;
    private static DrawableSwitch wechatSwitch;
    private static DrawableSwitch unionpaySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceIntent = new Intent(this, ServiceMain.class);
        btnStart = (Button)findViewById(R.id.btn_start);
        btnStartDrawable = (GradientDrawable) btnStart.getBackground();
        EventBus.getDefault().register(this);

        alipaySwitch = (DrawableSwitch) findViewById(R.id.drawableSwitch);
        alipaySwitch.setListener(new DrawableSwitch.MySwitchStateChangeListener()
        {
            @Override
            public void mySwitchStateChanged(boolean isSwitchOn)
            {
                if(!alipaySwitch.isPressed()) {
                    return;
                }
                alipaySwitchChange(isSwitchOn);
            }
        });

        wechatSwitch = (DrawableSwitch) findViewById(R.id.drawableSwitch2);
        wechatSwitch.setListener(new DrawableSwitch.MySwitchStateChangeListener()
        {
            @Override
            public void mySwitchStateChanged(boolean isSwitchOn)
            {
                if(!wechatSwitch.isPressed()) {
                    return;
                }
                wechatSwitchChange(isSwitchOn);
            }
        });

        unionpaySwitch = (DrawableSwitch) findViewById(R.id.drawableSwitch3);
        unionpaySwitch.setListener(new DrawableSwitch.MySwitchStateChangeListener()
        {
            @Override
            public void mySwitchStateChanged(boolean isSwitchOn)
            {
                if(!unionpaySwitch.isPressed()) {
                    return;
                }
                unionpaySwitchChange(isSwitchOn);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * 点开始的操作
     */
    public void btn_startClick(View v) {
        /*if (getPackageInfo(HookMain.WECHAT_PACKAGE) != null
                && !getPackageInfo(HookMain.WECHAT_PACKAGE).versionName.contentEquals("6.7.2")) {
            Toast.makeText(MainActivity.this, "微信版本不对！官方下载版本号：6.7.2", Toast.LENGTH_SHORT).show();
        }
        if (getPackageInfo(HookMain.ALIPAY_PACKAGE) != null
                && !getPackageInfo(HookMain.ALIPAY_PACKAGE).versionName.contentEquals("10.1.35.828")) {
            Toast.makeText(MainActivity.this, "支付宝版本不对！官方下载版本号：10.1.35.828", Toast.LENGTH_SHORT).show();
        }*/
        mIsRunning= mIsRunning>0 ? 0 : 1;
        changeBtnStart();

        if(mIsRunning==0){
            serviceIntent.putExtra("data", "close");
            startService(serviceIntent);
        }else {
            serviceIntent.putExtra("data", "start");
            startService(serviceIntent);
        }

        startService(new Intent(this, ServiceProtect.class));
    }

    //改变按钮颜色，和选项
    private void changeBtnStart(){
        //Log.d("arik", "改变按钮颜色: ");
        switch (mIsRunning){
            case 0:
                btnStart.setText("已关闭");
                btnStartDrawable.setColor(Color.parseColor("#940000"));
                btnStartDrawable.setStroke(0, Color.parseColor("#e6a711"));//描边的颜色和宽度
                //同步状态，不知为何Switch.setSwitchOn()无效，只能妥协方法处理
                mIsAlipayRunning = false;
                mIsWechatRunning = false;
                mIsUnionpayRunning = false;
                if(alipaySwitch.isSwitchOn())alipaySwitch.changeStatus();
                if(wechatSwitch.isSwitchOn())wechatSwitch.changeStatus();
                if(unionpaySwitch.isSwitchOn())unionpaySwitch.changeStatus();
                break;
            case 1:
                btnStart.setText("正在连接");
                btnStartDrawable.setColor(Color.parseColor("#e6a711"));
                btnStartDrawable.setStroke(0, Color.parseColor("#e6a711"));//描边的颜色和宽度
                break;
            case 2:
                btnStart.setText("已连接");
                btnStartDrawable.setColor(Color.parseColor("#009f07"));
                btnStartDrawable.setStroke(0, Color.parseColor("#e6a711"));//描边的颜色和宽度
                break;
        }

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==1){
                switch (mIsRunning){
                    case 0:

                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        }
    };

    //支付宝状态改变
    private void alipaySwitchChange(Boolean isSwitchOn){
        if(mIsRunning<2){
            Toast.makeText(MainActivity.this, "请先连接", Toast.LENGTH_SHORT).show();
            alipaySwitch.setSwitchOn(false);
            return;
        }
        Log.d("arik", "alipay pack: "+getPackageInfo(HookMain.ALIPAY_PACKAGE).versionName);
        if(isSwitchOn){
            if (getPackageInfo(HookMain.ALIPAY_PACKAGE) != null
                    && !getPackageInfo(HookMain.ALIPAY_PACKAGE).versionName.contentEquals("10.1.35.828")) {
                Toast.makeText(MainActivity.this, "支付宝版本不对！官方下载版本号：10.1.35.828", Toast.LENGTH_SHORT).show();
                alipaySwitch.setSwitchOn(false);
                return;
            }else if(!isAppAlive(MainActivity.this,HookMain.ALIPAY_PACKAGE)){
                Toast.makeText(MainActivity.this, "请先启动支付宝再打开开关。", Toast.LENGTH_SHORT).show();
                alipaySwitch.setSwitchOn(false);
                return;
            }
        }
        Log.d("arik", "开关状态: "+isSwitchOn);
        mIsAlipayRunning = isSwitchOn;
        serviceIntent.putExtra("data", "alipay");
        startService(serviceIntent);
    }

    //微信状态改变
    private void wechatSwitchChange(Boolean isSwitchOn){
        if(mIsRunning<2){
            Toast.makeText(MainActivity.this, "请先连接", Toast.LENGTH_SHORT).show();
            wechatSwitch.setSwitchOn(false);
            return;
        }
        Log.d("arik", "wechat pack: "+getPackageInfo(HookMain.WECHAT_PACKAGE).versionName);
        if(isSwitchOn){
            if(getPackageInfo(HookMain.WECHAT_PACKAGE) != null
                    && !getPackageInfo(HookMain.WECHAT_PACKAGE).versionName.contentEquals("6.7.2")){
                Toast.makeText(MainActivity.this, "微信版本不对！官方下载版本号：6.7.2", Toast.LENGTH_SHORT).show();
                wechatSwitch.setSwitchOn(false);
                return;
            }else if(!isAppAlive(MainActivity.this,HookMain.WECHAT_PACKAGE)){
                Toast.makeText(MainActivity.this, "请先启动微信再打开开关。", Toast.LENGTH_SHORT).show();
                wechatSwitch.setSwitchOn(false);
                return;
            }
        }
        mIsWechatRunning = isSwitchOn;
        serviceIntent.putExtra("data", "wechat");
        startService(serviceIntent);
    }

    //云闪付状态改变
    private void unionpaySwitchChange(Boolean isSwitchOn){
        if(mIsRunning<2){
            Toast.makeText(MainActivity.this, "请先连接", Toast.LENGTH_SHORT).show();
            unionpaySwitch.setSwitchOn(false);
            return;
        }
        Log.d("arik", "unionpay pack: "+getPackageInfo(HookMain.UNIONPAY_PACKAGE).versionName);
        if(isSwitchOn){
            if(getPackageInfo(HookMain.UNIONPAY_PACKAGE) != null
                    && !getPackageInfo(HookMain.UNIONPAY_PACKAGE).versionName.contentEquals("6.1.2")){
                Toast.makeText(MainActivity.this, "云闪付版本不对！官方下载版本号：6.1.2", Toast.LENGTH_SHORT).show();
                unionpaySwitch.setSwitchOn(false);
                return;
            }else if(!isAppAlive(MainActivity.this,HookMain.UNIONPAY_PACKAGE)){
                Toast.makeText(MainActivity.this, "请先启动云闪付再打开开关。", Toast.LENGTH_SHORT).show();
                unionpaySwitch.setSwitchOn(false);
                return;
            }
        }
        mIsUnionpayRunning = isSwitchOn;
        serviceIntent.putExtra("data", "unionpay");
        startService(serviceIntent);
    }

    //点击设置
    public void clsSetting(View v){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    //点日志
    public void clsLog(View view) {
        LogcatDialog s= new LogcatDialog(MainActivity.this,R.style.Translucent_NoTitle);
        s.searchTag="arik";
        s.setTitle("日志");
        s.show();
    }

    //判断包名的版本
    private PackageInfo getPackageInfo(String packageName) {
        PackageInfo pInfo = null;
        try {
            //通过PackageManager可以得到PackageInfo
            PackageManager pManager = getPackageManager();
            pInfo = pManager.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
            return pInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pInfo;
    }

    //应用是否启用
    public static boolean isAppAlive(Context context, String packageName) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos
                = activityManager.getRunningAppProcesses();
        for (int i = 0; i < processInfos.size(); i++) {
            if (processInfos.get(i).processName.equals(packageName)) {
                return true;
            }
        }

        return false;
    }

    //接收其它页面传来的信息
    @Subscribe(threadMode = ThreadMode.MAIN) //在ui线程执行
    public void onDataSynEvent(DataSynEvent event) {
        final String str = event.getMessage();
        switch (str){
            case "状态改变":
                Log.d("arik", "onDataSynEvent: " + str);
                changeBtnStart();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){ //按后退键只返回桌面，不退出程序
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }

}
