package boby.com.jpushimdemo.mine;

import java.util.List;

/**
 * Created by boby on 2017/11/14 0014.
 * 聊天底部菜单常用点击事件
 */

public interface OnMenuClickListener {
    /**
     * 输入框输入文字后，点击发送按钮事件
     * @param input 发送的文字
     * @return
     */
     boolean onSendTextMessage(CharSequence input);

    /**
     *  选中文件或者录制完视频后，点击发送按钮触发此事件
     * @param list
     */
     void onSendFiles(List<String> list);

    /**
     *  点击语音按钮触发事件，显示录音界面前触发此事件
     * @return
     */
    boolean switchToMicrophoneMode();

    /**
     *点击图片按钮触发事件，显示图片选择界面前触发此事件
     * @return
     */
    boolean switchToGalleryMode();

    /**
     * 点击拍照按钮触发事件，显示拍照界面前触发此事件
     * @return
     */
    boolean switchToCameraMode();
}
