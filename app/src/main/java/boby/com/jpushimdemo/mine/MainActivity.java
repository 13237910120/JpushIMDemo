package boby.com.jpushimdemo.mine;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import boby.com.jpushimdemo.R;

public class MainActivity extends AppCompatActivity {

    private SmartRefreshLayout smartRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        smartRefreshLayout= (SmartRefreshLayout) findViewById(R.id.smartRefresh);
        smartRefreshLayout.setRefreshHeader(new MyRefrashHeader(this));
    }



}
