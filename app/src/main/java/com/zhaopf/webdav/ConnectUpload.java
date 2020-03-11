package com.zhaopf.webdav;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;
import com.zhaopf.webdav.interfaces.Result;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 赵鹏飞 on 2020/3/9 12:41
 * 新线程中执行操作
 */
public class ConnectUpload extends AsyncTask<String, Void, Boolean> {
    private final Result result;
    ConnectUpload(Result result) {
        this.result = result;
    }
    @Override
    protected Boolean doInBackground(String... strings) {
        String webdavUrl = strings[0];
        String account = strings[1];
        String password = strings[2];
        String filePath = strings[3];
        String operation = strings[4];
        String webdavDir = "Test";//WebDav里面创建的文件夹名称，可更改

        Sardine sardine = new OkHttpSardine();//实例化
        sardine.setCredentials(account, password);
        if (!webdavUrl.endsWith("/")) {
            webdavUrl = webdavUrl + "/";//防止输错，创建多余目录
        }
        String fileName = filePath.split("/")[filePath.split("/").length - 1];//根据路径获取文件名
        try {
            switch (operation) {
                case "getfiles":
                    //获取WebDav中webdavDir文件夹中的文件
                    //****************************
                    List<DavResource> resources = null;
                    resources = sardine.list("https://dav.jianguoyun.com/dav/" + webdavDir + "/");//目录在后面加上一个斜杠
                    for (DavResource res : resources) {
                        //YourCode
                        Log.e("WebDavFile:", res.toString());//获取webdavDir文件夹内的文件名字
                    }
                    break;
                case "connect":
                    //通过判断文件夹是否存在去判断是否连接成功
                    //****************************
                    if (!sardine.exists(webdavUrl + webdavDir)) {
                        //不存在目录即创建
                        sardine.createDirectory(webdavUrl + webdavDir);
                    }
                    break;
                case "upload":
                    //上传,同名文件如果存在的操作没有去写
                    //****************************
                    if (sardine.exists(webdavUrl + webdavDir + "/" + fileName)) {
                        //YourCode
                        Log.e("isHava:", fileName);
                    } else {
                        //上传
                        //第一个参数是webdav的存放路径，第二个参数是文件，第三个http请求头
                        sardine.put(webdavUrl + webdavDir + "/" + fileName, new File(filePath), "application/x-www-form-urlencoded");
                    }
                    break;
            }

            return true;
        } catch (IOException e) {
            //失败之后返回false
            e.printStackTrace();
            return false;
        }
    }


    //结果
    @Override
    protected void onPostExecute(Boolean aBoolean) {
        Log.e("result:", aBoolean ? "成功" : "失败");
        result.toastResult(aBoolean);
        super.onPostExecute(aBoolean);
    }

}
