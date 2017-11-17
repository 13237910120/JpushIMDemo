package boby.com.jpushimdemo.mine;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by boby on 2017/11/17.
 */

public class Image3DRoate extends ImageView {

    //Camera类
    private Camera mCamera=new Camera();
    private Matrix mMatrix = new Matrix();


    float scale = 1;    // <------- 像素密度

    float degrees; //旋转的角度
    private final float mDepthZ=0;//深度
    public Image3DRoate(Context context) {
        super(context);
    }

    public Image3DRoate(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Image3DRoate(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        roate3d(canvas.getMatrix());
    }

    void init(Context context){
        // 获取手机像素密度 （即dp与px的比例）
        scale = context.getResources().getDisplayMetrics().density;

    }


    void roate3d(Matrix matrix){

        final float centerX = getWidth()/2;
        final float centerY = getHeight()/2;
        final Camera camera = mCamera;
        camera.save();

//        // 调节深度
//        if (mReverse) {
//            camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
//        } else {
//            camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));
//        }

        // 绕y轴旋转
        camera.rotateY(degrees);

        camera.getMatrix(matrix);
        camera.restore();

        // 调节中心点
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
    }

   public void setDegrees(float degrees){
        this.degrees=degrees;
        postInvalidate();
    }
}
