package com.sjk.simplepay;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.sjk.simplepay.bll.ApiBll;
import com.sjk.simplepay.po.Configer;
import com.sjk.simplepay.po.DataSynEvent;
import com.sjk.simplepay.utils.LogUtils;
import com.sjk.simplepay.utils.PayUtils;
import com.sjk.simplepay.utils.ReceiveUtils;
import com.sjk.simplepay.po.CommFunction;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP;


/**
 * @ Created by Dlg
 * @ <p>TiTle:  ServiceMain</p>
 * @ <p>Description: 这个类就一直轮循去请求是否需要二维码</p>
 * @ date:  2018/09/22
 */
public class ServiceMain extends Service {

    //是否启动了检测二维码需求的功能
    public static int mIsRunning = 0; //0:未连接，1连接中，2已连接
    public static Boolean mIsWechatRunning = false;
    public static Boolean mIsAlipayRunning = false;
    public static Boolean mIsUnionpayRunning = false;
    public static Boolean mAccountChanged = false;

    // 增加WebSocket方式，提高获单效率
    public static WebSocketClient socketConnect;
    private static final String wsUrl = Configer.getInstance().getUrl().replaceAll("http","ws") + "signalr-deviceHub";
    private static long lastHeartbeat = 0;
    private static int socketNum = 0;

    //上次询问服务器是否需要二维码的时间
    public static long mLastQueryTime = 0;

    //上次同步收款状态的时间
    public static long mLastPayStateTime = System.currentTimeMillis();

    //防止被休眠，你们根据情况可以开关，我是一直打开的，有点费电是必然的，哈哈
    private PowerManager.WakeLock mWakeLock;

    private ApiBll mApiBll;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLastQueryTime = System.currentTimeMillis();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | ACQUIRE_CAUSES_WAKEUP, "com.sjk.tpay:waketag");
        mWakeLock.acquire();

        Log.d("arik", "onCreate: 服务启动");
        ReceiveUtils.startReceive();

        mApiBll = new ApiBll();
        if (!handler.hasMessages(0)) {
            handler.sendEmptyMessage(0);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.sendEmptyMessage(0);
        String data = intent.getStringExtra("data");
        //Log.d("arik", "onStartCommand: 从主页面传过来的值为:"+ data);
        if("start".equals(data)){
            mIsRunning = 1;
            try {
                socketNum = 1;
                openSocket();
            }catch (Exception e){
                Log.e("arik", "onCreate 出错:" + e.toString());
                //reconnectToServer();
            }
        } else if("close".equals(data)){
            closeSocket();
        } else if(data.equals("alipay")){
            UpdateAccounts("alipay");
        } else if(data.equals("wechat")){
            UpdateAccounts("wechat");
        } else if(data.equals("unionpay")){
            UpdateAccounts("unionpay");
        }

        return START_STICKY;
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mIsRunning>0 && System.currentTimeMillis() - lastHeartbeat > 20000) {//停止任务或WebSocket正常连接的时候，不会去轮循
                //Log.e("arik", "handler添加二维码任务");
                mApiBll.checkQR();
                reconnectToServer();
            }
            if (handler.hasMessages(0)) {
                return;
            }
            if (ReceiverMain.getmLastSucc() != 0 && System.currentTimeMillis() - ReceiverMain.getmLastSucc() > 5000
			&& System.currentTimeMillis() - ReceiverMain.getmLastSucc() < 30000) {
                PayUtils.dealAlipayWebTrade(ServiceMain.this, ReceiverMain.getCook());
            }

            mLastQueryTime = System.currentTimeMillis();
            //0-7点的时候就慢速轮循
            handler.sendEmptyMessageDelayed(0,
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 7 ? 5000
                            : 10000);
        }
    };

    @Override
    public void onDestroy() {
        try {
            if (mWakeLock != null)
                mWakeLock.release();
            mWakeLock = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*mIsRunning = 0;
        mIsAlipayRunning = false;
        mIsWechatRunning = false;
        mIsUnionpayRunning = false;
        socketConnect.close();
        socketConnect = null;*/
        Log.d("arik", "服务已关闭");
        Intent intent = new Intent(this.getApplicationContext(), ServiceMain.class);
        this.startService(intent);
        super.onDestroy();
    }

    public void openSocket() throws URISyntaxException {
        //if(!mIsRunning) return;

        if(socketConnect==null){
            Log.d("arik", "openSocket: 尝试连接 ");
            socketConnect = new WebSocketClient(new URI(wsUrl)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d("arik", "WebSocket onOpen");
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("protocol", "json");
                        obj.put("version", 1);
                        sendMsg(obj.toString()+(char)0x1e);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                @Override
                public void onMessage(String message) {

                    // 心跳包
                    if(CommFunction.getType(message)==6){
                        lastHeartbeat = System.currentTimeMillis();

                        /*if(mAccountChanged)
                        {
                            UpdateAccounts();
                            mAccountChanged = false;
                        }*/
                        //20分钟同步一次收款状态
                        if(System.currentTimeMillis()-mLastPayStateTime>20*60*1000)
                        {
                            UpdateAccounts();
                            mAccountChanged = false;
                            Log.i("arik", "已同步收款状态");
                        }
                        //Log.i("arik", "心跳包");
                        return;
                    }

                    Log.i("arik", "服务器返回：" + message);

                    // 握手成功后，登陆
                    if(CommFunction.getType(message)==0){
                        //Log.d("arik", "握手成功");
                        try {
                            sendMsg(CommFunction.getInstance().getLoginString());
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    sendMsg(CommFunction.formateMessage("hello"));
                                }
                            }, 5000);
                        }catch (Exception e){

                        }
                        lastHeartbeat = System.currentTimeMillis();
                    }

                    //收到生成二维码任务
                    JSONArray qrTask = CommFunction.getArgument(message,"ReceiveQRTaskFromServer");
                    if(qrTask!=null){
                        try {
                            switch (qrTask.get(0).toString().toLowerCase()){
                                case "wechat":
                                    //Log.d("arik", "准备生成微信二维码...");
                                    PayUtils.getInstance().creatWechatQr(HKApplication.app, (int)qrTask.get(1), (String) qrTask.get(2));
                                    break;
                                case "alipay":
                                    String v = CommFunction.formateMessage(
                                            "ReceiptCodeFinished",
                                            "alipay",Configer.getInstance().getUserAlipay(),"",(String) qrTask.get(2),(int)qrTask.get(1)
                                    );
                                    sendMsg(v);
                                    break;
                                case "alibank":
                                    String v2 = CommFunction.formateMessage(
                                            "ReceiptCodeFinished",
                                            "alibank",Configer.getInstance().getUserAlipay(),"",(String) qrTask.get(2),(int)qrTask.get(1)
                                    );
                                    sendMsg(v2);
                                    break;
                                case "unionpay":
                                    //Log.d("arik", "准备生成云闪付二维码...");
                                    PayUtils.getInstance().creatUnionpayQr(HKApplication.app, (int)qrTask.get(1), (String) qrTask.get(2));
                                    break;
                            }
                        }catch (Exception e){

                        }
                    }

                    final String state = CommFunction.getState(message);
                    if(state==null) return;
                    switch (state){
                        case "[40001]"://设备登录成功
                            Log.d("arik", "登陆成功");
                            mApiBll.checkQR();
                            UpdateAccounts();
                            mIsRunning=2;
                            postEventBus("状态改变");
                            break;
                        case "[10001]"://设备登录失败
                            Log.d("arik", "设备登陆失败");
                            mIsRunning=1;
                            break;
                        case "[10002]"://设备登录过期
                            Log.d("arik", "设备连接状态过期，需要关闭后重新连接");
                            mIsRunning=1;
                            //socketConnect.reconnect();
                            sendMsg(CommFunction.getInstance().getLoginString());
                            break;
                    }
                }

                @Override
                public void reconnect() {
                    Log.e("arik", "重连...");
                    super.reconnect();

                    socketNum -= 1;
                    if(socketNum == 0) {
                        final Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                reconnectToServer();
                            }
                        }, (int) ((Math.random() * 9 + 1) * 1000));
                    }
                }
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d("arik", "WebSocket onClose,reason:"+reason + "   " + remote + "   "+code);
                    if(mIsRunning==0) return; //为0就不再重试重连了
                    if(mIsRunning==2) mIsRunning=1;
                    postEventBus("状态改变");
                    socketNum -= 1;
                    if(socketNum == 0) {
                        final Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                reconnectToServer();
                            }
                        }, (int) ((Math.random() * 9 + 1) * 300));
                    }
                }
                @Override
                public void onError(Exception ex) {
                    Log.e("arik", "onError:" + ex.toString());
//                    socketNum -= 1;
//                    if(socketNum == 0) {
//                        reconnectToServer();
//                    }
                }
            };

            socketConnect.connect();
        }
        else
        {
            Log.d("arik","已建立连接，不重复操作");
        }
    }

    public void UpdateAccounts(String... args)
    {
        // 发送微信账号信息
        if(args.length==0 ||(args.length>0 && args[0].equals("wechat")))sendMsg(CommFunction.getInstance().updateWechatStr(mIsWechatRunning));
        // 发送支付宝账号信息
        if(args.length==0 ||(args.length>0 && args[0].equals("alipay")))sendMsg(CommFunction.getInstance().updateAlipayStr(mIsAlipayRunning));
        // 发送云闪付账号信息
        if(args.length==0 ||(args.length>0 && args[0].equals("unionpay")))sendMsg(CommFunction.getInstance().updateUnionpayStr(mIsUnionpayRunning));

        mLastPayStateTime = System.currentTimeMillis();
        /*// 发送微信账号信息
        String strAccountUpdateWechat = CommFunction.formateMessage("DeviceAccountUpdate","wechat",mIsWechatRunning.toString(),Configer.getInstance().getUserWechat());
        sendMsg(strAccountUpdateWechat);
        // 发送支付宝账号信息
        String strAccountUpdateAlipay = CommFunction.formateMessage("DeviceAccountUpdate","alipay",mIsAlipayRunning.toString(),Configer.getInstance().getUserAlipay());
        sendMsg(strAccountUpdateAlipay);
        // 发送云闪付账号信息*/
    }

    public void reconnectToServer()
    {
        Log.d("arik", "正在尝试重连，lockReconnect为" + socketNum);
        if(socketNum != 0) return;

        socketNum = 1;
        // 延迟重连，避免过多连接
        socketConnect = null;

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("arik", "reconnectToServer:重连服务器");
                try {
                    Log.d("arik", "reconnect: 尝试用WebSocket连接服务器");
                    openSocket();
                } catch (Exception e) {
                    Log.e("arik", "reconnectToServer 出错:" + e.toString());
                    reconnectToServer();
                }
            }
        }, 5000);
    }

    public void connect()
    {
        try {
            openSocket();
        }catch (Exception e){

        }
    }

    public void closeSocket()
    {
        if(socketConnect!=null){
            try {
                socketConnect.close();
                socketConnect = null;
            }catch (Exception e){

            }
        }
    }

    /**
     *发送消息
     */
    public void sendMsg(String msg) {
        try {
            socketConnect.send(msg);
            Log.d("arik", "发送信息: " + msg);
        } catch (Exception e) {
            //Log.d("arik", "sendMsg error: " + e.toString());
        }
    }

    public void postEventBus(String v){
        DataSynEvent dataSynEvent = new DataSynEvent();//创建事件对象
        dataSynEvent.setMessage(v);//給类对象设置数据
        EventBus.getDefault().post(dataSynEvent);//发送事件
    }

}
