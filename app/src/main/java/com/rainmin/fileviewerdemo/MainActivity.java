package com.rainmin.fileviewerdemo;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.rainmin.fileviewer.FileViewActivity;

public class MainActivity extends AppCompatActivity {

//    private final String filePath = "/storage/sdcard0/rainmin.docx";
    private final String filePath = Environment.getExternalStorageDirectory().getPath() + "/documents/test.xlsx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.btn_view);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileViewActivity.actionStart(MainActivity.this, filePath);
            }
        });
    }
}
