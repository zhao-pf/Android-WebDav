package com.zhaopf.webdav;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.leon.lfilepickerlibrary.LFilePicker;
import com.zhaopf.webdav.interfaces.Result;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener, Result {
    ProgressDialog progressDialog;
    private String webdavUrl, account, password, filePath, fileName;
    private EditText edtTxtUrl;
    private EditText edtTxtAccount;
    private EditText edtTxtPassword;
    private Button btnConnect;
    private Button btnUpload;
    private Button btnGetfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();//初始化控件
    }


    private void initView() {
        //实例化控件
        edtTxtUrl = findViewById(R.id.edtTxt_url);
        edtTxtAccount = findViewById(R.id.edtTxt_account);
        edtTxtPassword = findViewById(R.id.edtTxt_password);
        btnConnect = findViewById(R.id.btn_connect);
        btnUpload = findViewById(R.id.btn_upload);
        btnGetfile = findViewById(R.id.btn_getfile);
        //设置监听器
        btnConnect.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        btnGetfile.setOnClickListener(this);
    }

    /**
     * 调用系统文件选择器的回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            assert data != null;
            String filePath = data.getStringArrayListExtra("paths").get(0);
            Log.e("filePath:", filePath);
            connectUpload(webdavUrl, account, password, filePath, "upload");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                //获取内容
                webdavUrl = edtTxtUrl.getText().toString();
                account = edtTxtAccount.getText().toString();
                password = edtTxtPassword.getText().toString();
                //存到sp
                SharedPreferences sp = this.getSharedPreferences("config", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("webdavUrl", webdavUrl);
                editor.putString("account", account);
                editor.putString("password", password);//这些可以做一下加密存储，我没弄
                editor.apply();
                //连接
                showProgress();
                connectUpload(webdavUrl, account, password, "null", "connect");
                break;
            case R.id.btn_upload:
                //上传
                //调用文件选择器---我随便用了一个开源的文件选择器:https://github.com/leonHua/LFilePicker
                int REQUESTCODE_FROM_ACTIVITY = 1000;
                new LFilePicker()
                        .withActivity(MainActivity.this)
                        .withRequestCode(REQUESTCODE_FROM_ACTIVITY)
                        .withMutilyMode(false)
                        .start();
                break;
            case R.id.btn_getfile:
                //获取坚果云中的文件
                showProgress();
                connectUpload(webdavUrl, account, password, "null", "getfiles");
                break;
        }
    }

    //对话框
    void showProgress() {
        progressDialog = ProgressDialog.show(this, "提示", "正在进行，请稍等…", true, false, null);
    }

    @Override
    public void toastResult(Boolean b) {
        progressDialog.dismiss();
        Toast.makeText(this, b ? "成功" : "失败", Toast.LENGTH_SHORT).show();
    }

    /**
     * @param webdavUrl Webdav服务器地址
     * @param account   Webdav服务器账号
     * @param password  Webdav服务器密码
     * @param filePath  本地文件路径
     * @param operation getfiles ：获取WebDav中webdavDir文件夹中的文件
     *                  connect  ：验证账号密码连接
     *                  upload   ：上传文件 除了上传文件以外filePath随便给定一个值就行
     */
    private void connectUpload(String webdavUrl, String account, String password, String filePath, String operation) {
        new ConnectUpload(this).execute(webdavUrl, account, password, filePath, operation);
    }

}
