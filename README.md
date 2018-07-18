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
	        implementation 'com.github.caijia:DownloadManager:0.1'
	}
```

##### 使用
```
FileRequest fileRequest = new FileRequest.Builder()
                .url("http://app.mi.com/download/656145")
                .build();

        fileDownloader = new FileDownloader.Builder()
                .threadCount(threadCount)
                .saveFileDirPath(Environment.getExternalStorageDirectory().getAbsolutePath())
                .fileRequest(fileRequest)
                .build();

        fileDownloader.download()
```
