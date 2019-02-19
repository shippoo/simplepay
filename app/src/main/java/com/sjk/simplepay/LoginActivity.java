package com.sjk.simplepay;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sjk.simplepay.po.Configer;
import com.sjk.simplepay.utils.HTTPSTrustManager;
import com.sjk.simplepay.utils.ReceiveUtils;
import com.sjk.simplepay.utils.SaveUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LoginActivity extends Activity {

    private EditText mEdtUrl;
    private EditText mEdtToken;

    private Button btnLogin;
    private Button btnCopy;
    private TextView mEdtSN;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        mEdtUrl =  findViewById(R.id.edt_url);
        mEdtToken = findViewById(R.id.edt_token);
        btnLogin = (Button) this.findViewById(R.id.btn1);
        btnCopy = (Button) this.findViewById(R.id.btncopy);
        mEdtSN = (TextView) this.findViewById(R.id.textView4);

        mEdtSN.setText(android.os.Build.SERIAL);
        ((TextView) findViewById(R.id.txt_version)).setText("Ver：" + BuildConfig.VERSION_NAME);
        HTTPSTrustManager.allowAllSSL();
        getPermissions();

        //给btnLogin绑定响应事件，跳转页面
        btnLogin.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            saveConfig();
                                        }
                                    }

        );

        //给btncopy绑定响应事件，弹窗
        btnCopy.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           //给btncopy添加点击事件

                                           //获取剪贴板管理器：
                                           ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                           cm.setText(mEdtSN.getText()); // 复制

                                           //弹出提示
                                           showDialog();
                                       }
                                   }

        );
    }

    private void saveConfig(){

        mEdtUrl.setText(mEdtUrl.getText().toString().trim());
        mEdtToken.setText(mEdtToken.getText().toString().trim());
        if (mEdtUrl.length() < 5 || mEdtToken.length() < 10) {
            Toast.makeText(LoginActivity.this, "请先输入正确配置！", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!mEdtUrl.getText().toString().startsWith("http"))
        {
            Toast.makeText(LoginActivity.this, "请输入正确的网址！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mEdtUrl.getText().toString().endsWith("/")) {
            mEdtUrl.setText(mEdtUrl.getText().toString() + "/");//保持以/结尾的网址
        }

        //下面开始获取最新配置并启动服务。
        Configer.getInstance()
                .setUrl(mEdtUrl.getText().toString());
        Configer.getInstance()
                .setToken(mEdtToken.getText().toString());

        //保存配置
        new SaveUtils().putJson(SaveUtils.BASE, Configer.getInstance()).commit();

        //有的手机就算已经静态注册服务还是不行启动，再手动启动一下。
        //startService(new Intent(this, ServiceMain.class));
        //startService(new Intent(this, ServiceProtect.class));

        //广播也再次注册一下。。。机型兼容。。。
        ReceiveUtils.startReceive();
        addStatusBar();

        Intent intent = new Intent(this, MainActivity.class);
        //启动
        startActivity(intent);
        finish();
    }


    //弹窗
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setIcon(R.drawable.picture);
        builder.setTitle("温馨提示");
        builder.setMessage("设备码已复制！");
        builder.setPositiveButton("我知道了",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    /**
     * 在状态栏添加图标
     */
    private void addStatusBar() {
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();

        PendingIntent pi = PendingIntent.getActivity(this, 0, getIntent(), 0);
        Notification noti = new Notification.Builder(this)
                .setTicker("程序启动成功")
                .setContentTitle("看到我，说明我在后台正常运行")
                .setContentText("始于：" + new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date()))
                .setSmallIcon(R.mipmap.ic_launcher)//设置图标
                .setDefaults(Notification.DEFAULT_SOUND)//设置声音
                .setContentIntent(pi)//点击之后的页面
                .build();

        manager.notify(17952, noti);
    }

    /**
     * 当获取到权限后才操作的事情
     */
    private void onPermissionOk() {
        mEdtUrl.setText(Configer.getInstance().getUrl());
        mEdtToken.setText(Configer.getInstance().getToken());
    }

    /**
     * 获取权限。
     */
    private void getPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onPermissionOk();
            return;
        }
        List<String> sa = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请READ_PHONE_STATE权限。。。。
            sa.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            sa.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            sa.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (sa.size() < 1) {
            onPermissionOk();
            return;
        }
        ActivityCompat.requestPermissions(this, sa.toArray(new String[]{}), 1);
    }

    /**
     * 获取到权限后的回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //获取到了权限之后才可以启动xxxx操作。
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "部分权限未开启\n可能部分功能暂时无法工作。", Toast.LENGTH_SHORT).show();
                //如果被永久拒绝。。。那只有引导跳权限设置页
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!shouldShowRequestPermissionRationale(permissions[i])) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName())); // 根据包名打开对应的设置界面
                        startActivity(intent);
                        onPermissionOk();
                        return;
                    }
                }
                break;
            }
        }
        onPermissionOk();
    }
}
