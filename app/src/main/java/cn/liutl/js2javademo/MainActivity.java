package cn.liutl.js2javademo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_go_web1).setOnClickListener(this);
        findViewById(R.id.btn_go_web2).setOnClickListener(this);
        findViewById(R.id.btn_go_web3).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
        String title = "";
        String url = "";
        switch (v.getId()) {
            case R.id.btn_go_web1:
                title = "网页示例1";
                url = "file:///android_asset/index1.html";
                break;
            case R.id.btn_go_web2:
                title = "网页示例2";
                url = "file:///android_asset/index2.html";
                break;
            case R.id.btn_go_web3:
                title = "网页示例3";
                url = "file:///android_asset/index3.html";
                break;
        }
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        startActivity(intent);
    }
}
