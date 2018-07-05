package com.caijia.downloadmanager;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.caijia.download.AndroidSchedule;
import com.caijia.download.CallbackInfo;
import com.caijia.download.DownloadListener;
import com.caijia.download.FileDownloader;
import com.caijia.download.FileRequest;

public class MainActivity extends AppCompatActivity {

    private TextView tvMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnDownload = findViewById(R.id.btn_download);
        Button btnPause = findViewById(R.id.btn_pause);
        tvMsg = findViewById(R.id.tv_msg);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download();
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileDownloader != null) {
                    fileDownloader.pause();
                }
            }
        });
    }

    private FileDownloader fileDownloader;

    private void download() {
        fileDownloader = new FileDownloader.Builder()
                .threadCount(3)
                .saveFileDirPath(Environment.getExternalStorageDirectory().getAbsolutePath())
                .schedule(new AndroidSchedule())
                .build();
        FileRequest fileRequest = new FileRequest.Builder()
                .url("http://app.mi.com/download/532898")
                .build();
        fileDownloader.download(fileRequest,new DownloadListener() {
            @Override
            public void onStart(CallbackInfo state) {
                showMsg(state.toString());
            }

            @Override
            public void onDownloading(CallbackInfo state) {
                showMsg(state.toString());
            }

            @Override
            public void onComplete(CallbackInfo state) {
                showMsg(state.toString());
            }

            @Override
            public void onPausing(CallbackInfo state) {
                showMsg(state.toString());
            }

            @Override
            public void onPause(CallbackInfo state) {
                showMsg(state.toString());
            }
        });
    }

    private void showMsg(String s) {
        tvMsg.setText(s);
    }
}

