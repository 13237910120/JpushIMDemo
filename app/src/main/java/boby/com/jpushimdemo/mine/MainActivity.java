package boby.com.jpushimdemo.mine;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadmoreListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import boby.com.jpushimdemo.R;
import boby.com.jpushimdemo.mine.Adapter.ConversationAdapter;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.ConversationRefreshEvent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;

import static cn.jpush.im.android.api.enums.ContentType.*;
import static cn.jpush.im.android.api.enums.ConversationType.single;

public class MainActivity extends AppCompatActivity {

    private SmartRefreshLayout smartRefreshLayout;
   private  List<Conversation> conversationList;
    private ConversationAdapter conversationAdapter;
    private RecyclerView message_list;

    public static void show(Context context){
        Intent intent=new Intent(context,MainActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        smartRefreshLayout= (SmartRefreshLayout) findViewById(R.id.smartRefresh);
        smartRefreshLayout.setRefreshHeader(new MyRefrashHeader(this));
        message_list= (RecyclerView) findViewById(R.id.message_list);
        conversationList=new ArrayList<>();
        conversationAdapter=new ConversationAdapter(conversationList,this);
        message_list.setAdapter(conversationAdapter);
        message_list.setLayoutManager(new LinearLayoutManager(this));
        initJGConversationList();
        conversationAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ConversationType type = conversationList.get(position).getType();
                Conversation conversation = conversationList.get(position);
                conversation.resetUnreadCount();//重置会话的未读数。设置为已读
                conversationAdapter.notifyItemChanged(position);
                if(single==type){ //个人
                    UserInfo targetUserInfo= (UserInfo) conversation.getTargetInfo();
                    MessageActivity.show(MainActivity.this,targetUserInfo.getUserName());
                }else {  //群聊
                    GroupInfo groupInfo= (GroupInfo) conversation.getTargetInfo();
                    MessageActivity.show(MainActivity.this,groupInfo.getGroupID());
                }
            }
        });
        //订阅接收消息,要重写onEvent就能收到消息
        JMessageClient.registerEventReceiver(this);
    }

    void initJGConversationList(){
       //获取当前登录账号的用户信息
//        UserInfo userInfo= JMessageClient.getMyInfo();
//        Log.e("mine userInfo",userInfo.getAppKey()+" , "+userInfo.getAvatar());
        smartRefreshLayout.setEnableLoadmore(false);
        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                getMessageList();
                smartRefreshLayout.finishRefresh();
            }
        });

    }

    /**
     * 收到消息
     */
    public void onEvent(MessageEvent event) {
        Log.e("MainActivity","收到消息");
        Message msg = event.getMessage();
        if (msg.getTargetType() == ConversationType.group) {
            long groupId = ((GroupInfo) msg.getTargetInfo()).getGroupID();
            Conversation conv = JMessageClient.getGroupConversation(groupId);
            conversationAdapter.setToTop(conv);
        } else {
            final UserInfo userInfo = (UserInfo) msg.getTargetInfo();
            String targetId = userInfo.getUserName();
            Conversation conv = JMessageClient.getSingleConversation(targetId, userInfo.getAppKey());
            if (conv != null ) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(userInfo.getAvatar())) {
                            userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                                @Override
                                public void gotResult(int responseCode, String responseMessage, Bitmap avatarBitmap) {
                                    if (responseCode == 0) {
                                        conversationAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                });
                conversationAdapter.setToTop(conv);
            }
        }

    }
    /**
     * 消息漫游完成事件
     *
     * @param event 漫游完成后， 刷新会话事件
     */
    public void onEvent(ConversationRefreshEvent event) {
        Log.e("MainActivity","漫游完成");
        Conversation conv = event.getConversation();
        conversationAdapter.setToTop(conv);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        getMessageList();
    }
    void getMessageList(){
        List<Conversation> conversationList1 = JMessageClient.getConversationList();
        if(conversationList1!=null){
            //获取会话列表
            conversationList.clear();
            this.conversationList.addAll(conversationList1);
            conversationAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        //注销消息接收
        JMessageClient.unRegisterEventReceiver(this);
        super.onDestroy();
    }
}
