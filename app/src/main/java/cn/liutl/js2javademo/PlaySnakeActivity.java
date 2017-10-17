package cn.liutl.js2javademo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 一个简单的本地页面
 */
public class PlaySnakeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_snake);
        final String funcName = getIntent().getStringExtra("funcName");
        final EditText etSomeWord = (EditText) findViewById(R.id.et_some_word);
        findViewById(R.id.iv_left_back).setVisibility(View.INVISIBLE);
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText("本地原生页面");

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String someWord = etSomeWord.getText().toString();
                if (TextUtils.isEmpty(someWord)) {
                    Toast.makeText(PlaySnakeActivity.this, "至少随便输入点东西", Toast.LENGTH_SHORT).show();
                } else {
                    Intent data = new Intent();
                    data.putExtra("someWord", someWord);
                    data.putExtra("funcName", funcName);
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        });
    }
}
