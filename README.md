# WebDav-For-Android
关于WebDav使用的一系列操作实例
掘金文章地址:[传送门](https://juejin.im/post/5e64c032e51d4526c1482148)  
## 先上图上链接  
Demo效果图  
![](https://raw.githubusercontent.com/zhao-pf/zhao-pf.github.io/master/screenshots/2020/13/1.gif)  
---------------------------

## 前言  
我自己对数据存取有需求，所以研究了几天最终决定存在坚果云。<br> 因为坚果云免费的，支持WebDav，用来存取用户数据确实是不错的，在开发之前网上找到的关于资料很少，通过查看其他相同需求软件最后找到了sardine这个项目，项目地址：[传送门](https://github.com/thegrizzlylabs/sardine-android)
----------------------------
## 一、开始前的配置
### 1. 添加依赖库
查看最新版本号:[传送门](https://github.com/thegrizzlylabs/sardine-android/releases)
``` java
dependencies {
    ...
    implementation 'com.thegrizzlylabs.sardine-android:sardine-android:最新版本号'
}
```
### 2. 添加各种权限
``` java
    <!-- 网络权限 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <!-- 文件读写权限 -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
>这里有两个坑<br>
安卓9以上的网络安全策略:[谷歌官方文档](https://developer.android.google.cn/training/articles/security-config.html)<br>
安卓10沙箱限制:[谷歌官方文档](https://developer.android.com/training/data-storage/files/external-scoped?hl=zh-cn)<br>

#### 解决办法:<BR>
右键res文件夹新建一个目录选择xml
![](https://raw.githubusercontent.com/zhao-pf/zhao-pf.github.io/master/screenshots/2020/13/2.jpg)  
之后右键xml文件夹新建一个名为**network_security_config.xml**的xml文件(名字自己取一个)  
在文件中输入
``` java
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false" />
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">www.pgyer.com</domain>
        <domain includeSubdomains="true">app-global.pgyer.com</domain>
    </domain-config>
</network-security-config>
```
在**AndroidManifest.xml**文件中添加
``` java
    <application
        android:networkSecurityConfig="@xml/network_security_config"
        ...
```
解决第二个坑，文件访问限制
在**AndroidManifest.xml**文件中添加
``` java
    <application
        android:requestLegacyExternalStorage="true"
        ...
```
接下来去申请WebDav的账号用来测试  
坚果云:[点击进入](https://www.jianguoyun.com/)<br>
进去注册完成之后，点击右上角账户信息
![](https://raw.githubusercontent.com/zhao-pf/zhao-pf.github.io/master/screenshots/2020/13/3.png)  
>我们需要拿到的三个信息：
服务器地址 账号 密码  
把这三样保存一下，待会儿测试用
## 二、一些操作
``` !
注:必须在新线程中执行操作
```
我的方法是写一个类继承AsyncTask操作，逻辑操作看demo
<!--继承AsyncTask类时需要指定三个泛型参数-->
<!--这三个参数的用途：-->
<!--+ **Params**  -->
<!--在执行AsyncTask的execute(Params)时传入的参数，可用于在doInBackground()任务中使用。  -->
<!--+ **Progress**  -->
<!--后台执行任务进度值的类型。  -->
<!--+ **Result**  -->
<!--后台任务执行完毕后，如果需要结果返回，可指定为Result类型的泛型数据作为返回值类型。  -->

###  初始化
``` java
    Sardine sardine = new OkHttpSardine();//实例化
    sardine.setCredentials(账号, 密码);
```
###  连接服务器
``` java
    //通过判断文件夹是否存在去判断是否连接成功，如果错误会抛出异常 返回false
    //会在WebDav中新建一个名为Test的文件夹
    if (!sardine.exists("https://dav.jianguoyun.com/dav/Test/")) {
        //不存在目录即创建
        sardine.createDirectory("https://dav.jianguoyun.com/dav/Test/");
    }
```
###  判断文件/文件夹是否存在
``` java
    if (sardine.exists("https://dav.jianguoyun.com/dav/Test/Test.apk")) {
        //文件存在
    }
```
``` java
    if (sardine.exists("https://dav.jianguoyun.com/dav/Test/")) {
        //文件夹存在
    }
```
### 上传文件
``` java
    if (sardine.exists("https://dav.jianguoyun.com/dav/Test/Test.apk")) {
        //YourCode
        //如果同名文件存在
        Log.e("isHava:", fileName);
    } else {
        //第一个参数是webdav的存放路径，第二个参数是本机文件路径，第三个http请求头
        sardine.put("https://dav.jianguoyun.com/dav/Test/Test.apk", new File(filePath), "application/x-www-form-urlencoded");
    }
```
### 下载文件
``` java
    InputStream fis = sardine.get("https://dav.jianguoyun.com/dav/Test/Test.apk");//服务器上的文件名字
    FileOutputStream fos = new FileOutputStream("/storage/emulated/0/Test.apk");//下载的路径
    int len = -1;
    byte[] buffer = new byte[1024];
    while ((len = fis.read(buffer)) != -1) {
        fos.write(buffer, 0, len);
    }
    fis.close();
    fos.close();//关闭输入输出流
```
### 删除文件
``` java
    sardine.delete("https://dav.jianguoyun.com/dav/Test/Test.apk");
```
### 获取WebDav中的文件
``` java
    List<DavResource> resources = null;
    resources = sardine.list("https://dav.jianguoyun.com/dav/Test/");//后面需斜杠
    for (DavResource res : resources) {
        //YourCode
        Log.e("WebDavFile:", res.toString());//获取webdavDir文件夹内的文件名字
        //格式为/dav/Test/Test.apk
    }
```
### 分隔获取文件完整名字
``` java
    //分隔字符串"/",获取长度-1为文件名字，可以直接在上一步中处理出来
    filePath.split("/")[filePath.split("/").length - 1];//根据路径获取文件名
```
``` !
下图将文件列表写入适配器列表中，在子线程中无法刷新UI，子线程可以通过Handler来将UI更新操作切换到主线程中执行，具体操作看Demo

```
![](https://raw.githubusercontent.com/zhao-pf/zhao-pf.github.io/master/screenshots/2020/13/4.jpg)  
## 三、后言
据我的查找国内支持 WebDAV 协议的网盘只有坚果一家，每月有 1GB 的上传流量和 3GB 的下载流量，对于我来说存储数据完全够用  
再此之前自己写了一个我自己有需求的软件，用来备份整个文件夹的  
感谢各位的阅读，因为本人学习安卓时间并不长，文章中和Demo中出现什么问题，欢迎大家及时指正。 
欢迎大家添加我的交流群或好友技术交流,广交朋友
交流群:
<img src="https://files.catbox.moe/ubh2w4.png"  width="300"/>
作者微信:
<img src="https://files.catbox.moe/bp58t6.jpg"   width="300" />
