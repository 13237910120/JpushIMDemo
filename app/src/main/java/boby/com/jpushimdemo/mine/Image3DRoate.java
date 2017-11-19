package boby.com.jpushimdemo.mine;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by boby on 2017/11/17.
 */

public class Image3DRoate extends ImageView {

    //Camera类
    private Camera mCamera;
    private Matrix mMatrix ;

    float scale = 1;    // <------- 像素密度

    float degrees; //旋转的角度
    private final float mDepthZ=0;//深度
    public Image3DRoate(Context context) {
        super(context);
        init(context);
    }

    public Image3DRoate(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Image3DRoate(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        Log.e("draw ",degrees+"");
        mCamera.save();
        //绕X轴翻转
//        mCamera.rotateX(-deltaY);
        //绕Y轴翻转
        mCamera.rotateY(degrees);
        //设置camera作用矩阵
        mCamera.getMatrix(mMatrix);
        mCamera.restore();
        //设置翻转中心点
        mMatrix.preTranslate(-getWidth()/2, -getHeight()/2);
        mMatrix.postTranslate(getWidth()/2, getHeight()/2);
        canvas.concat(mMatrix);
//        canvas.setMatrix(mMatrix);
        super.onDraw(canvas);

    }

    void init(Context context){
        // 获取手机像素密度 （即dp与px的比例）
        scale = context.getResources().getDisplayMetrics().density;
        mCamera=new Camera(); //创建一个没有任何转换效果的新的Camera实例
        mMatrix = new Matrix();
    }



   public void setDegrees(float degrees){
        this.degrees=degrees;
       invalidate();
    }


}
