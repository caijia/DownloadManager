package com.caijia.downloadmanager;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caijia.download.CallbackInfo;
import com.caijia.download.DownloadListener;
import com.caijia.download.FileDownloader;
import com.caijia.download.FileRequest;

public class MainActivity extends AppCompatActivity {

    private TextView tvMsg;
    private EditText etThreadCount;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnDownload = findViewById(R.id.btn_download);
        Button btnPause = findViewById(R.id.btn_pause);
        progressBar = findViewById(R.id.progressBar);
        etThreadCount = findViewById(R.id.et_thread_count);
        tvMsg = findViewById(R.id.tv_msg);

        String strThreadCount = etThreadCount.getText().toString();
        int threadCount = TextUtils.isEmpty(strThreadCount) ? 3 : Integer.parseInt(strThreadCount);

        FileRequest fileRequest = new FileRequest.Builder()
                .url("http://app.mi.com/download/656145")
                .build();

        fileDownloader = new FileDownloader.Builder()
                .threadCount(threadCount)
                .saveFileDirPath(Environment.getExternalStorageDirectory().getAbsolutePath())
                .fileRequest(fileRequest)
                .debug(true)
                .build();


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
        fileDownloader.download(new DownloadListener() {
            @Override
            public void onStart(CallbackInfo state) {
                showMsg(state.toString());
                progressBar.setMax(100);
            }

            @Override
            public void onDownloading(CallbackInfo state) {
                int progress = state.getFileSize() > 0 ?
                        (int) (state.getDownloadSize() * 100 / state.getFileSize()) : 0;
                progressBar.setProgress(progress);
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

