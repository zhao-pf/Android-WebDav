package com.zhaopf.webdav;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;
import com.zhaopf.webdav.adapder.RecyclerViewAdapder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileList extends AppCompatActivity {
    public static final int UPDATE_TEXT = 1;
    private List<String> items = new ArrayList<>();
    private RecyclerViewAdapder adapter;
    private String webdavUrl, account, password;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == UPDATE_TEXT) {
                adapter.notifyDataSetChanged();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        setTitle("文件列表");
        //获取WebDav中webdavDir文件夹中的文件
        //****************************

        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sp = FileList.this.getSharedPreferences("config", MODE_PRIVATE);
                webdavUrl = sp.getString("webdavUrl", "https://dav.jianguoyun.com/dav/");
                account = sp.getString("account", "null");
                password = sp.getString("password", "null");
                List<String> items = new ArrayList<>();
                String webdavDir = "Test";
                Sardine sardine = new OkHttpSardine();//实例化
                sardine.setCredentials(account, password);
                List<DavResource> resources = null;
                try {
                    resources = sardine.list(webdavUrl + "Test/");//目录在后面加上一个斜杠
                    for (DavResource res : resources) {
                        //YourCode
                        items.add(res.toString());
                        Log.e("WebDavFile:", items.toString());//获取webdavDir文件夹内的文件名字
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                items.remove(0);//删除第一个
                adapter.putData(items);
                Message message = new Message();
                message.what = UPDATE_TEXT;
                handler.sendMessage(message);
                //子线程更新UI
            }
        }).start();
        initRecyclerView();


    }

    private void initRecyclerView() {
        RecyclerView rvFilelist = findViewById(R.id.rv_filelist);
        adapter = new RecyclerViewAdapder(items, FileList.this);
        rvFilelist.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(FileList.this);
        rvFilelist.setLayoutManager(layoutManager);
        rvFilelist.addItemDecoration(new DividerItemDecoration(FileList.this, DividerItemDecoration.VERTICAL));
    }

}
