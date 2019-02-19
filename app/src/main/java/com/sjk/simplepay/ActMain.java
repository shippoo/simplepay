package com.sjk.simplepay;

/*import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;*/
import android.os.Bundle;
/*import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;*/
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hdl.logcatdialog.LogcatDialog;
import com.sjk.simplepay.po.CommFunction;
/*import com.sjk.simplepay.po.Configer;
import com.sjk.simplepay.utils.HTTPSTrustManager;
import com.sjk.simplepay.utils.ReceiveUtils;
import com.sjk.simplepay.utils.SaveUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;*/


/**
 * @ Created by Dlg
 * @ <p>TiTle:  ActMain</p>
 * @ <p>Description: 启动首页，直接在xml绑定的监听
 * @ 其实我是不推荐这种绑定方式的，哈哈哈，为了项目简洁点还是就这样吧</p>
 * @ date:  2018/09/11
 */
public class ActMain extends AppCompatActivity {

    //private Intent serviceIntent;
    //private Button btnStart;

    /*private DrawableSwitch wechatSwitch;
    private DrawableSwitch alipaySwitch;
    private DrawableSwitch unionpaySwitch;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);

        //serviceIntent = new Intent(this, ServiceMain.class);
        //btnStart = (Button)findViewById(R.id.btn_start);

    }

    /*@Override
    protected void onResume() {
        super.onResume();
        *//*mBtnSubmit.setText(ServiceMain.mIsRunning ? "停止服务" : "确认配置并启动");
        mBtnWechat.setText(ServiceMain.mIsWechatRunning ? "停止微信服务" : "启动微信收款");
        mBtnAlipay.setText(ServiceMain.mIsAlipayRunning ? "停止支付服务" : "启动支付宝收款");*//*
    }*/

    /**
     * 切换APP服务的运行状态
     *
     * @return
     */
    /*private boolean changeStatus() {
        ServiceMain.mIsRunning = !ServiceMain.mIsRunning;
        mBtnSubmit.setText(ServiceMain.mIsRunning ? "停止服务" : "确认配置并启动");

        if(!ServiceMain.mIsRunning)
        {
            if(ServiceMain.mIsWechatRunning) changeWechatStatus();
            if(ServiceMain.mIsAlipayRunning) changeAlipayStatus();
            if(ServiceMain.mIsUnionpayRunning) changeUnionpayStatus();
        }

        return ServiceMain.mIsRunning;
    }
    private boolean changeWechatStatus() {
        ServiceMain.mAccountChanged = true;
        ServiceMain.mIsWechatRunning = !ServiceMain.mIsWechatRunning;
        mBtnWechat.setText(ServiceMain.mIsWechatRunning ? "停止微信服务" : "启动微信收款");
        return ServiceMain.mIsWechatRunning;
    }
    private boolean changeAlipayStatus() {
        ServiceMain.mAccountChanged = true;
        ServiceMain.mIsAlipayRunning = !ServiceMain.mIsAlipayRunning;
        mBtnAlipay.setText(ServiceMain.mIsAlipayRunning ? "停止支付宝服务" : "启动支付宝收款");
        return ServiceMain.mIsAlipayRunning;
    }
    private boolean changeUnionpayStatus() {
        ServiceMain.mAccountChanged = true;
        ServiceMain.mIsUnionpayRunning = !ServiceMain.mIsUnionpayRunning;
        mBtnUnionpay.setText(ServiceMain.mIsUnionpayRunning ? "停止云闪付" : "启动云闪付收款");
        return ServiceMain.mIsUnionpayRunning;
    }*/


    /*private PackageInfo getPackageInfo(String packageName) {
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
    }*/

    /**
     * 点开始的操作
     */
    /*public void btn_startClick(View v) {
        if (getPackageInfo(HookMain.WECHAT_PACKAGE) != null
                && !getPackageInfo(HookMain.WECHAT_PACKAGE).versionName.contentEquals("6.7.2")) {
            Toast.makeText(ActMain.this, "微信版本不对！官方下载版本号：6.7.2", Toast.LENGTH_SHORT).show();
        }
        if (getPackageInfo(HookMain.ALIPAY_PACKAGE) != null
                && !getPackageInfo(HookMain.ALIPAY_PACKAGE).versionName.contentEquals("10.1.35.828")) {
            Toast.makeText(ActMain.this, "支付宝版本不对！官方下载版本号：10.1.35.828", Toast.LENGTH_SHORT).show();
        }

        //serviceIntent.putExtra("data", "start");
        //startService(serviceIntent);

    }*/

    /**
     * 测试微信获取二维码的功能
     *
     * @param view
     */
    public void clsWechatPay(View view) {
        /*if(!ServiceMain.mIsRunning)
        {
            Toast.makeText(ActMain.this, "请确认配置信息后，再启动微信收款功能", Toast.LENGTH_SHORT).show();
            return;
        }*/
        /*if (!changeWechatStatus()) {
            return;
        }*/
        //changeWechatStatus();

        CommFunction.getInstance().postEventBus(CommFunction.getInstance().updateWechatStr(ServiceMain.mIsWechatRunning));
        //CommFunction.getInstance().postEventBus("updateActive");

        /*Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(HookMain.RECEIVE_BILL_WECHAT);
        HKApplication.app.sendBroadcast(broadCastIntent);*/

//        String time = System.currentTimeMillis() / 1000 + "";
//        PayUtils.getInstance().creatWechatQr(this, 12, "test" + time);
    }


    /**
     * 测试支付宝获取二维码的功能
     *
     * @param view
     */
    public void clsAlipayPay(View view) {
        /*if(!ServiceMain.mIsRunning)
        {
            Toast.makeText(ActMain.this, "请确认配置信息后，再启动支付宝收款功能", Toast.LENGTH_SHORT).show();
            return;
        }*/
        /*if (!changeAlipayStatus()) {
            return;
        }*/
        //changeAlipayStatus();

        CommFunction.getInstance().postEventBus(CommFunction.getInstance().updateAlipayStr(ServiceMain.mIsAlipayRunning));

        /*Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(HookMain.RECEIVE_BILL_ALIPAY2);
        HKApplication.app.sendBroadcast(broadCastIntent);*/

//        String time = System.currentTimeMillis() / 1000 + "";
//        PayUtils.getInstance().creatAlipayQr(this, 12, "test" + time);
    }

    //云闪付
    public void clsUnionpayPay(View view) {
        /*if(!ServiceMain.mIsRunning)
        {
            Toast.makeText(ActMain.this, "请确认配置信息后，再启动云闪付收款功能", Toast.LENGTH_SHORT).show();
            return;
        }*/
        //changeUnionpayStatus();

        CommFunction.getInstance().postEventBus(CommFunction.getInstance().updateUnionpayStr(ServiceMain.mIsUnionpayRunning));

        /*Intent intent = new Intent(UNIONPAY_CREAT_QR);
        String money ="12";
        String mark ="aaa";
        intent.putExtra("money",money);
        intent.putExtra("mark",mark);
        sendBroadcast(intent);*/
    }


}
