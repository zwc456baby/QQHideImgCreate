package com.example.qqhideimgcreate;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SplashScreenActivity extends Activity {

    //所需要申请的权限数组
    private static String[] permissionsArray = null;
    //还需申请的权限列表
    private List<String> permissionsList = new ArrayList<>();
    public Map<String, String> PERMISSION_INFO = new HashMap<>();
    // 权限失败说明
//    public static String PERMISSION_REFUSE_INFO = "許可權缺少不能正常運行";
    public String GO_TO_SETPANEL = "许可权限请求失败,程序不能正常运行,请同意游戏需要的许可权限";
    //申请权限后的返回码
    private final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private boolean isOpenAppCenter = false;

    private int PERMISSION_TYPE_ALL = 0;
    private int SET_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        initPermissionData();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                checkPermission(SplashScreenActivity.this, permissionsArray, REQUEST_CODE_ASK_PERMISSIONS);
            }
        }, 3000);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void initPermissionData() {
        permissionsArray = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        PERMISSION_INFO.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "读写权限：读取图片");
    }


    private void enterNextActivity() {
        Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
        startActivity(i);
        SplashScreenActivity.this.finish();
    }

    public void checkPermission(Activity thisActivity, String[] permissionsArray, int callbackType) {
        permissionsList.clear();
        StringBuilder msg = new StringBuilder("应用需要获取以下权限:");
        for (String permission : permissionsArray) {
            if (ContextCompat.checkSelfPermission(thisActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                msg.append("\n* ");
                msg.append(PERMISSION_INFO.get(permission));
                permissionsList.add(permission);
            }
        }
        if (permissionsList.size() == 0) {
            enterNextActivity();
            return;
        }

        showMsg(PERMISSION_TYPE_ALL, msg.toString(), permissionsList, callbackType);
    }

    protected void showMsg(final int type, String msg, final List<String> permissionList, final int callbackType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(R.string.text_tip);
        builder.setTitle("提示");
        builder.setMessage(msg);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (type == SET_PERMISSION) {
                    Intent localIntent = new Intent();
                    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    localIntent.setData(Uri.fromParts("package", getPackageName(), null));
                    isOpenAppCenter = true;
                    SplashScreenActivity.this.startActivity(localIntent);
                } else {
                    ActivityCompat.requestPermissions(SplashScreenActivity.this, permissionList.toArray(new String[permissionsList.size()]), callbackType);
                }

            }
        });
        if (type == PERMISSION_TYPE_ALL) {

            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onDestroy();
                    SplashScreenActivity.this.finish();
                    System.exit(0);
                }
            });

            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface arg0) {
                    // TODO Auto-generated method stub
                }
            });
        }

        if (!this.isFinishing()) {
            builder.show();
        }

    }


    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int permissionCount = 0;
        boolean isNeverAsk;
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int i = 0; i < permissions.length; i++) {

                    isNeverAsk = ActivityCompat.shouldShowRequestPermissionRationale(this,
                            permissions[i]);

                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        permissionCount++;
                    } else {
                        if (!isNeverAsk) {
                            showMsg(SET_PERMISSION, GO_TO_SETPANEL, null, 0);
                            return;
                        }
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        if (permissionCount == permissions.length) {
            enterNextActivity();
        } else {
            checkPermission(SplashScreenActivity.this, permissionsArray, REQUEST_CODE_ASK_PERMISSIONS);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isOpenAppCenter) {
            isOpenAppCenter = false;
            checkPermission(SplashScreenActivity.this, permissionsArray, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }
}