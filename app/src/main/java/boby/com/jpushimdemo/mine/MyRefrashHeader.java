package boby.com.jpushimdemo.mine;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.internal.pathview.PathsView;

import java.util.zip.Inflater;

import boby.com.jpushimdemo.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * 消息列表下拉刷新头控件
 * Created by boby on 2017/11/15.
 */

public class MyRefrashHeader implements RefreshHeader {
    private View rootView;
    private  Context mContext;
    private TextView mHearHintText; //下拉刷新提示语
    private Image3DRoate reflash_header_arrow;//下拉动画
    private ProgressBar reflash_header_progressbar;//加载进度条
    public MyRefrashHeader(Context context) {
        mContext=context;
        initView();
    }

    /**
     * 手指拖动下拉（会连续多次调用，用于实时控制动画关键帧）
     * @param percent 下拉的百分比 值 = offset/headerHeight (0 - percent - (headerHeight+extendHeight) / headerHeight )
     * @param offset 下拉的像素偏移量  0 - offset - (headerHeight+extendHeight)
     * @param headerHeight Header的高度
     * @param extendHeight Header的扩展高度
     */
    @Override
    public void onPullingDown(float percent, int offset, int headerHeight, int extendHeight) {
//        Log.e("pullingdown:" ," percent:"+percent+" offset:"+offset+" headerHeight:"+headerHeight+" extendHeight:"+extendHeight);
        reflash_header_arrow.setDegrees(percent*720);
    }

    /**
     * 手指释放之后的持续动画（会连续多次调用，用于实时控制动画关键帧）
     * @param percent 下拉的百分比 值 = offset/headerHeight (0 - percent - (headerHeight+extendHeight) / headerHeight )
     * @param offset 下拉的像素偏移量  0 - offset - (headerHeight+extendHeight)
     * @param headerHeight Header的高度
     * @param extendHeight Header的扩展高度
     */
    @Override
    public void onReleasing(float percent, int offset, int headerHeight, int extendHeight) {

    }

    private void initView() {
        rootView=LayoutInflater.from(mContext).inflate(R.layout.reflash_m_header,null);
        mHearHintText= (TextView) rootView.findViewById(R.id.reflash_header_hint_textview);
        reflash_header_arrow= (Image3DRoate) rootView.findViewById(R.id.reflash_header_arrow);
        reflash_header_progressbar= (ProgressBar) rootView.findViewById(R.id.reflash_header_progressbar);
    }

    @NonNull
    @Override
    public View getView() {
        return rootView;
    }

    @Override
    public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.Translate;//指定为平移，不能null;
    }

    @Override
    public void setPrimaryColors(@ColorInt int... colors) {

    }
    /**
     * 尺寸定义初始化完成 （如果高度不改变（代码修改：setHeader），只调用一次, 在RefreshLayout#onMeasure中调用）
     * @param kernel RefreshKernel 核心接口（用于完成高级Header功能）
     * @param height HeaderHeight or FooterHeight
     * @param extendHeight extendHeaderHeight or extendFooterHeight
     */
    @Override
    public void onInitialized(RefreshKernel kernel, int height, int extendHeight) {

    }

    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {

    }

    /**
     * 开始动画（开始刷新或者开始加载动画）
     * @param layout RefreshLayout
     * @param height HeaderHeight or FooterHeight
     * @param extendHeight extendHeaderHeight or extendFooterHeight
     */
    @Override
    public void onStartAnimator(RefreshLayout layout, int height, int extendHeight) {

    }
    /**
     * 动画结束
     * @param layout RefreshLayout
     * @param success 数据是否成功刷新或加载
     * @return 完成动画所需时间 如果返回 Integer.MAX_VALUE 将取消本次完成事件，继续保持原有状态
     */
    @Override
    public int onFinish(RefreshLayout layout, boolean success) {
        if(success){

        }else {

        }
         return 200;//延迟500毫秒之后再弹回;
    }

    @Override
    public boolean isSupportHorizontalDrag() {
        return false;  //是否支持水平滑动
    }

    @Override
    public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) {
        switch (newState) {
            case None:
            case PullDownToRefresh:
                mHearHintText.setText("下拉开始刷新");
                reflash_header_arrow.setVisibility(VISIBLE);//显示下拉动画
                reflash_header_progressbar.setVisibility(GONE);//隐藏动画
//                reflash_header_arrow.animate().rotation(0);//
                break;
            case Refreshing:
                mHearHintText.setText("正在刷新");
                reflash_header_progressbar.setVisibility(VISIBLE);//显示加载进度动画
                reflash_header_arrow.setVisibility(GONE);//隐藏下拉动画
                break;
            case ReleaseToRefresh:
                mHearHintText.setText("松开加载数据");
//                reflash_header_arrow.animate().rotation(180);//显示箭头改为朝上

                break;
        }
    }
}
