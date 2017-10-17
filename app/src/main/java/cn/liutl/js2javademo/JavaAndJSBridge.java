package cn.liutl.js2javademo;

import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class JavaAndJSBridge {
    private final WebView mWebView;
    private final WebViewActivity mContext;

    public JavaAndJSBridge(@NonNull WebView webView, @NonNull WebViewActivity context) {
        mWebView = webView;
        mContext = context;
    }

    //暴露给sdk的本地方法
    @JavascriptInterface
    public void native_launchFunc(final String funcName, final String jsonStr) {
        //这里基本上不会是ui线程
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject jsonObject = new JSONObject(jsonStr);
                    if (funcName.startsWith("pickPhotoFromLibrary")) {
                        //去选取图片
                        boolean needCompress = jsonObject.getBoolean("needCompress");
                        goPickPhoto(funcName, needCompress);
                    } else if (funcName.startsWith("makePhotoFromCamera")) {
                        //去拍照
                        mContext.makePhoto(funcName);
                    } else if (funcName.startsWith("encrypt")) {
                        //去加密
                        goEncrypt(funcName, jsonObject);
                    } else if (funcName.startsWith("playSnake")) {
                        //去玩蛇
                        mContext.playSnake(funcName);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void goEncrypt(final String funcName, final JSONObject oldJsonObj) throws JSONException {
        //h5传过来的参数是不定的，所以加密的时间也不会固定
        //加密的过程不重要，只是演示这种需要并发调用的例子
        final long delay = (long) (Math.random() * 3500);
        Observable.timer(delay, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.d("JavaAndJSBridge", "aLong:" + aLong);
                        //随机时长完成，调用匿名回调并返回数据
                        int index = oldJsonObj.getInt("index");
                        JsonObject jsonObject = new JsonObject();
                        String message = "耗时" + delay + "毫秒，这是JS中第" + (index + 1) + "次添加的回调函数";
                        jsonObject.addProperty("message", message);
                        mWebView.loadUrl("javascript:sdk_nativeCallback(\'" + funcName + "\',\'" + jsonObject + "\')");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }


    private void goPickPhoto(final String funcName, boolean needCompress) {
        if (needCompress) {
            //压缩裁剪代码就不贴了，这里只是个简单的选择系统相册的示例
        } else {
            mContext.pickPhoto(funcName);
        }
    }

}
