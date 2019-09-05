#### 多线程断点下载,在极端网络下表现良好

##### Android Studio添加
```
allprojects {
		repositories {
			maven { url 'https://jitpack.io' }
		}
	}
```

```
dependencies {
	        implementation 'com.github.caijia:DownloadManager:0.23'
	}
```

##### 使用
```
FileRequest fileRequest = new FileRequest.Builder()
                .url("http://app.mi.com/download/656145")
                .build();

FileDownloader fileDownloader = new FileDownloader.Builder()
                .threadCount(threadCount)
                .saveFileDirPath(Environment.getExternalStorageDirectory().getAbsolutePath())
                .fileRequest(fileRequest)
                .build();
```

##### 下载
```
        fileDownloader.download(downloadListener)
```

##### 暂停
```
        fileDownloader.pause()
```

##### 下载监听
```
public interface DownloadListener {

    void onStart(CallbackInfo state);

    void onPrepared(CallbackInfo state);

    void onDownloading(CallbackInfo state);

    void onComplete(CallbackInfo state);

    void onPausing(CallbackInfo state);

    void onPause(CallbackInfo state);

}

```

##### 回调数据 CallbackInfo
方法 | 说明
--- | --- 
downloadPath | 文件下载路径
state | 下载状态{@link DownloadState}
savePath | 保存的文件路径,只有当文件下载完后才有值 {@link DownloadState#COMPLETE}
fileSize | 下载文件的总大小
downloadSize | 当前下载的总大小
speed | 下载的速度,单位是B/s，可以转换格式{@link Utils#formatSpeed(long)}

##### 回调方法 CallbackInfo
方法 | 说明
--- | --- 
onStart | 下载开始
onPrepared | 下载已准备好，此时知道下载文件的信息，包括大小等
onDownloading | 正在下载，进度条更新在这个方法里
onComplete | 下载完成
onPausing | 正在暂停中
onPause | 暂停了







