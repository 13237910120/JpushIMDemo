package boby.com.jpushimdemo.mine;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import boby.com.jpushimdemo.R;

public class MainActivity extends AppCompatActivity {

    Image3DRoate image3DRoate;
    float degrees=30;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image3DRoate= (Image3DRoate) findViewById(R.id.img);
    }



    public void onAddDreegs(View view) {
        image3DRoate.setDegrees(degrees);
        degrees=degrees+30;
    }
}
