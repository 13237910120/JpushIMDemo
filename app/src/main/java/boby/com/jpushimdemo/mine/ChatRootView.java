package boby.com.jpushimdemo.mine;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by boby on 2017/11/14 0014.
 *   聊天页面根布局
 */

public class ChatRootView extends RelativeLayout {

    // 输入法软件盘状态
    public static final byte KEYBOARD_STATE_SHOW = -3; //显示
    public static final byte KEYBOARD_STATE_HIDE = -2; //隐藏
    public static final byte KEYBOARD_STATE_INIT = -1; //加载中

    private boolean mHasInit;  //是否已经实例化
    private boolean mHasKeyboard;  // 软件盘是否隐藏
    private int mHeight; // 高度


    private OnKeyboardChangedListener mKeyboardListener;
    private OnSizeChangedListener mSizeChangedListener;

    public ChatRootView(Context context) {
        super(context);
    }

    public ChatRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatRootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mSizeChangedListener != null) {
            mSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!mHasInit) {
            mHasInit = true;
            mHeight = b;
            if (null != mKeyboardListener) {
                mKeyboardListener.onKeyBoardStateChanged(KEYBOARD_STATE_INIT);
            }
        } else {
            if (null != mKeyboardListener) {
                mKeyboardListener.onKeyBoardStateChanged(KEYBOARD_STATE_INIT);
            }
            mHeight = mHeight < b ? b : mHeight;
        }
        if (mHasInit && mHeight > b) {  // 软件盘隐藏
            mHasKeyboard = true;
            if (null != mKeyboardListener) {
                mKeyboardListener.onKeyBoardStateChanged(KEYBOARD_STATE_SHOW);
            }
        }
        if (mHasInit && mHasKeyboard && mHeight == b) {  //软件盘隐藏
            mHasKeyboard = false;
            if (null != mKeyboardListener) {
                mKeyboardListener.onKeyBoardStateChanged(KEYBOARD_STATE_HIDE);
            }
        }
    }

    /**
     * 软件盘状态监听
     */
    public interface OnKeyboardChangedListener {

        /**
         * 输入法监听
         *
         * @param state Three states: init, show, hide.
         */
        public void onKeyBoardStateChanged(int state);
    }

    // 屏幕大小改变监听（主要是软件盘弹出与隐藏）
    public interface OnSizeChangedListener {
        void onSizeChanged(int w, int h, int oldw, int oldh);
    }
}
