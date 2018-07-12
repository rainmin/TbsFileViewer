package com.rainmin.fileviewer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;
import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;

public class FileViewActivity extends AppCompatActivity implements TbsReaderView.ReaderCallback {

    private static final String TAG = "FileViewActivity";

    private RelativeLayout rlTbsView;
    private TextView tvTitle;
    private TextView tvState;
    private ProgressBar progressBar;

    private TbsReaderView mTbsReaderView;
    private String mFilePath;
    private String mFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_view);
        initX5Environment();
        initView();
        getFileUrlByIntent();
        if (isLocalExist()) {
            displayFile();
        } else {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTbsReaderView != null) {
            mTbsReaderView.onStop();
        }
    }

    private void initView() {
        rlTbsView = findViewById(R.id.rl_tbsView);
        tvState = findViewById(R.id.tv_state);
        progressBar = findViewById(R.id.progressBar_download);
        mTbsReaderView = new TbsReaderView(this, this);
        rlTbsView.addView(mTbsReaderView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {

    }

    private void getFileUrlByIntent() {
        Intent intent = getIntent();
        mFilePath = intent.getStringExtra("fileUrl");
    }

    public static void actionStart(Context context, String fileUrl) {
        Intent intent = new Intent(context, FileViewActivity.class);
        intent.putExtra("fileUrl", fileUrl);
        context.startActivity(intent);
    }

    private boolean isLocalExist() {
        if (TextUtils.isEmpty(mFilePath)) {
            return false;
        } else {
            File file = new File(mFilePath);
            if (file.exists()) {
                return true;
            } else {
                return false;
            }
        }
    }

    private void displayFile() {
        Bundle bundle = new Bundle();
        bundle.putString("filePath", mFilePath);
        bundle.putString("tempPath", Environment.getExternalStorageDirectory().getPath());
        boolean result = mTbsReaderView.preOpen(getFileType(mFilePath), false);
        if (result) {
            mTbsReaderView.openFile(bundle);
        } else {
            Log.d(TAG, "TbsReaderView cannot open the file, handled by system");
            File file = new File(mFilePath);
            if (file.exists()) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                String type = getMimeType(mFilePath);
                intent.setDataAndType(Uri.fromFile(file), type);
                startActivity(intent);
                finish();
            }
        }
    }

    private String getFileType(String filePath) {
        String result = "";
        if (TextUtils.isEmpty(filePath)) {
            Log.e(TAG, "file path is null");
            return result;
        }
        return filePath.substring(filePath.lastIndexOf(".") + 1);
    }

    private String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private void initX5Environment() {
        // 搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
                // x5内核初始化完成回调接口，可通过参数判断是否加载起来了x5内核
                Log.d(TAG, " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                // x5内核初始化完成回调接口，此接口回调并表示已经加载起来了x5，有可能特殊情况下x5内核加载失败，切换到系统内核
                Log.d(TAG, " onViewInitFinished");
            }
        };
        // x5内核初始化
        QbSdk.initX5Environment(getApplicationContext(), cb);

        // 非wifi网络条件下也允许下载内核
        QbSdk.setDownloadWithoutWifi(true);

        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
                Log.d(TAG, "onDownloadFinish");
            }

            @Override
            public void onInstallFinish(int i) {
                Log.d(TAG, "onInstallFinish");
            }

            @Override
            public void onDownloadProgress(int i) {
                Log.d(TAG, "onDownloadProgress:" + i);
            }
        });
    }
}
