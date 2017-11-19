package boby.com.jpushimdemo;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreater;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreater;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;

//import com.squareup.leakcanary.LeakCanary;


public class IMUISampleApplication extends Application {
    Map<String, Integer> mFaceMap; //表情
    Map<String, String> mGifMap;    //动画表情
    public static IMUISampleApplication instance;
    //static 代码段可以防止内存泄露
    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreater(new DefaultRefreshHeaderCreater() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                layout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white);//全局设置主题颜色
                return new ClassicsHeader(context).setSpinnerStyle(SpinnerStyle.Translate);//指定为经典Header，默认是 贝塞尔雷达Header
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreater(new DefaultRefreshFooterCreater() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new ClassicsFooter(context).setSpinnerStyle(SpinnerStyle.Translate);
            }
        });

    }

    public static IMUISampleApplication getInstance(){
        if(null == instance){
            instance = new IMUISampleApplication();

        }
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            return;
//        }
//        LeakCanary.install(this);

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
        //初始化极光
        JMessageClient.init(this,true);

    }

    public Map<String, Integer> getFaceMap() {
        if (mFaceMap.size() == 0) {
//            initFaceMap();
        }
        if (!mFaceMap.isEmpty())
            return mFaceMap;
        return null;
    }

    public Map<String, String> getGifMap() {
        if (mGifMap.size() == 0) {
//            initGifMap();
        }
        if (!mGifMap.isEmpty())
            return mGifMap;
        return null;
    }
}
