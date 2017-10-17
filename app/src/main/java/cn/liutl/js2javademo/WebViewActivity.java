package cn.liutl.js2javademo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;

import cn.liutl.js2javademo.util.Base64Util;
import cn.liutl.js2javademo.util.Common;
import cn.liutl.js2javademo.util.RealPathUtil;
import io.reactivex.functions.Consumer;

public class WebViewActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_IMAGE = 10086;
    private static final int PDD_PLAY_SNAKE = REQUEST_PICK_IMAGE + 1;
    private static final int REQUEST_TAKE_PHOTO = PDD_PLAY_SNAKE + 1;
    private WebView mWebView;
    private String pickPhotoName;
    private String takePhotoName;
    private String photoFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        initWebView();
    }

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    private void initWebView() {
        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setText(getIntent().getStringExtra("title"));
        findViewById(R.id.iv_left_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mWebView = (WebView) findViewById(R.id.m_web_view);
        mWebView.setWebViewClient(mWebViewClient);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JavaAndJSBridge(mWebView, this), "App");

        String url = getIntent().getStringExtra("url");
        mWebView.loadUrl(url);
    }

    WebViewClient mWebViewClient = new WebViewClient() {
        //将约定好的空js文件替换为本地的
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            WebResourceResponse webResourceResponse = super.shouldInterceptRequest(view, url);
            if (url == null) {
                return webResourceResponse;
            }
            if (url.endsWith("native-app.js")) {
                try {
                    webResourceResponse = new WebResourceResponse("text/javascript", "UTF-8", WebViewActivity.this.getAssets().open("local.js"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return webResourceResponse;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            WebResourceResponse webResourceResponse = super.shouldInterceptRequest(view, request);
            if (request == null) {
                return webResourceResponse;
            }
            String url = request.getUrl().toString();
            if (url != null && url.endsWith("native-app.js")) {
                try {
                    webResourceResponse = new WebResourceResponse("text/javascript", "UTF-8", WebViewActivity.this.getAssets().open("local.js"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return webResourceResponse;
        }
    };

    //去系统相册
    public void pickPhoto(String funcName) {
        pickPhotoName = funcName;
        new RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if (aBoolean) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, REQUEST_PICK_IMAGE);
                } else {
                    Toast.makeText(WebViewActivity.this, "请给予权限，谢谢", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //打开"玩蛇"界面
    public void playSnake(String funcName) {
        Intent intent = new Intent(this, PlaySnakeActivity.class);
        intent.putExtra("funcName", funcName);
        startActivityForResult(intent, PDD_PLAY_SNAKE);
    }

    //去拍照
    public void makePhoto(String funcName) {
        takePhotoName = funcName;
        new RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if (aBoolean) {
                    photoFileName = "img_" + System.currentTimeMillis() + ".jpeg";
                    File currentPhotoFile = new File(Common.getBasePath(WebViewActivity.this) + Common.TEMP_DIR, photoFileName);
                    photoFileName = currentPhotoFile.getAbsolutePath();
                    int currentApiVersion = android.os.Build.VERSION.SDK_INT;
                    if (currentApiVersion < 24) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentPhotoFile));
                        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
                    } else {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
                        ContentValues contentValues = new ContentValues(1);
                        contentValues.put(MediaStore.Images.Media.DATA, currentPhotoFile.getAbsolutePath());
                        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
                    }
                } else {
                    Toast.makeText(WebViewActivity.this, "请给予权限，谢谢", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            //系统相册选取完成
            Uri uri = data.getData();
            if (uri != null) {
                String filePath;
                if (!TextUtils.isEmpty(uri.toString()) && uri.toString().startsWith("file")) {
                    filePath = uri.getPath();
                } else {
                    filePath = RealPathUtil.getRealPathFromURI(this, uri);
                }
                String base64Image = Base64Util.encodeBase64ImageFile(filePath);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("image64", base64Image);
                jsonObject.addProperty("message", "图片获取成功");
                Log.d("WebViewActivity", "jsonObject:" + jsonObject);
                mWebView.loadUrl("javascript:sdk_nativeCallback(\'" + pickPhotoName + "\',\'" + jsonObject + "\')");
            }

        } else if (requestCode == PDD_PLAY_SNAKE && resultCode == RESULT_OK) {
            //玩了一波蛇
            String funcName = data.getStringExtra("funcName");
            String someWord = data.getStringExtra("someWord");
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("someWord", someWord);
            mWebView.loadUrl("javascript:sdk_nativeCallback(\'" + funcName + "\',\'" + jsonObject + "\')");

        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK && null != photoFileName) {
            //拍照回来
            Log.d("WebViewActivity1", photoFileName);
            String base64Image = Base64Util.encodeBase64ImageFile(photoFileName);
            Log.d("WebViewActivity2", base64Image);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("image64", base64Image);
            mWebView.loadUrl("javascript:sdk_nativeCallback(\'" + takePhotoName + "\',\'" + jsonObject + "\')");
        }
    }

}
