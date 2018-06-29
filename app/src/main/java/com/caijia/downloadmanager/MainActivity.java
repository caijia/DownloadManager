package com.caijia.downloadmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.caijia.download.FileDownloader;
import com.caijia.download.FileRequest;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.btn_download);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileDownloader fileDownloader = new FileDownloader.Builder().build();
                FileRequest fileRequest = new FileRequest.Builder()
                        .url("http://app.mi.com/download/639962")
                        .build();
                fileDownloader.download(fileRequest);
            }
        });
    }
}
