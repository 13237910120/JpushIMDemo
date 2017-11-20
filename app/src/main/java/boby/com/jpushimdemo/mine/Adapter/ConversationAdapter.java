package boby.com.jpushimdemo.mine.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import boby.com.jpushimdemo.R;
import boby.com.jpushimdemo.mine.MainActivity;
import boby.com.jpushimdemo.mine.utils.ThreadUtil;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * Created by boby on 2017/11/20 0020.
 */

public class ConversationAdapter extends BaseQuickAdapter<Conversation,BaseViewHolder> {
    private UserInfo mUserInfo;
    private GroupInfo mGroupInfo;
    private Context context;
    public ConversationAdapter( @Nullable List<Conversation> data,Context context) {
        super(R.layout.chat_conversation_item, data);
        this.context=context;
    }

    @Override
    protected void convert(final BaseViewHolder helper, Conversation item) {
//            helper.setImageResource(R.id.icon_service,item.getAvatarFile()); //对方头像
        ImageView heardImg=helper.getView(R.id.icon_service);
        if (item.getType().equals(ConversationType.single)) {

            mUserInfo = (UserInfo) item.getTargetInfo();
            if (mUserInfo != null) {
                mUserInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bitmap) {
                        if (status == 0) {
                            helper.setImageBitmap(R.id.icon_service,bitmap);
                        } else {
                            helper.setImageResource(R.id.icon_service,R.drawable.ic_launcher);
                        }
                    }
                });
            } else {
                helper.setImageResource(R.id.icon_service,R.drawable.ic_launcher);
            }
        } else {
            mGroupInfo = (GroupInfo) item.getTargetInfo();
            if (mGroupInfo != null) {
                mGroupInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int i, String s, Bitmap bitmap) {
                        if (i == 0) {
                            helper.setImageBitmap(R.id.icon_service,bitmap);
                        } else {
                            helper.setImageResource(R.id.icon_service,R.drawable.ic_launcher);
                        }
                    }
                });
            }else {
                helper.setImageResource(R.id.icon_service,R.drawable.ic_launcher);
            }
        }
        helper.setText(R.id.recent_list_item_name,item.getTitle()); //用户名 如果会话为单聊 --- 用户有昵称，显示昵称。 --- 没有昵称，显示username
        int unReadMsgCnt = item.getUnReadMsgCnt();
        if(unReadMsgCnt>0){
            helper.setGone(R.id.unreadmsg,true);
            helper.setText(R.id.unreadmsg,String.valueOf(unReadMsgCnt));
        }else {
            helper.setGone(R.id.unreadmsg,false);
        }

        Message lastMessage=item.getLatestMessage();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String lastDate = sdf.format(new Date(lastMessage.getCreateTime()));
        helper.setText(R.id.recent_list_item_time,lastDate); //最后一条消息发送时间

        String message = "";
        switch ( lastMessage.getContentType()){
            case  text:
                TextContent textContent= (TextContent) lastMessage.getContent();
                message=textContent.getText();
                break;
            case  file:
                message="[文件]";
                break;
            case  image:
                message="[图片]";
                break;

            case  voice:
                message="[语音]";
                break;

            case  location:
                message="[位置]";
                break;

            case  video:
                message="[视屏]";
                break;
        }
        helper.setText(R.id.recent_list_item_msg,message); //最后一条消息内容
    }

    /**
     * 收到消息后将会话置顶
     *
     * @param conv 要置顶的会话
     */
    public void setToTop(Conversation conv) {
        int oldCount = 0;
        int newCount = 0;
        //如果是旧的会话
        for (Conversation conversation : mData) {
            if (conv.getId().equals(conversation.getId())) {
                //因为后面要改变排序位置,这里要删除
                mData.remove(conversation);
                //这里要排序,因为第一次登录有漫游消息.离线消息(其中群组变化也是用这个事件下发的);所以有可能会话的最后一条消息
                //时间比较早,但是事件下发比较晚,这就导致乱序.所以要重新排序.

                //排序规则,每一个进来的会话去和倒叙list中的会话比较时间,如果进来的会话的最后一条消息就是最早创建的
                //那么这个会话自然就是最后一个.所以直接跳出循环,否则就一个个向前比较.
                for (int i = mData.size(); i > 0; i--) {
                    if (conv.getLatestMessage() != null && mData.get(i - 1).getLatestMessage() != null) {
                        if (conv.getLatestMessage().getCreateTime() > mData.get(i - 1).getLatestMessage().getCreateTime()) {
                            oldCount = i - 1;
                        } else {
                            oldCount = i;
                            break;
                        }
                    } else {
                        oldCount = i;
                    }
                }
                mData.add(oldCount, conv);
                notifyDataSetChanged();
                return;
            }
        }
        if (mData.size() == 0) {
            mData.add(conv);
        } else {
            //如果是新的会话,直接去掉置顶的消息数之后就插入到list中
            for (int i = mData.size(); i > 0; i--) {
                if (conv.getLatestMessage() != null && mData.get(i - 1).getLatestMessage() != null) {
                    if (conv.getLatestMessage().getCreateTime() > mData.get(i - 1).getLatestMessage().getCreateTime()) {
                        newCount = i - 1;
                    } else {
                        newCount = i;
                        break;
                    }
                } else {
                    newCount = i;
                }
            }
            mData.add(newCount, conv);
        }

        ThreadUtil.runOnMainThreadAsync(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    }
