package boby.com.jpushimdemo.mine;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.model.Conversation;
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
        conversationAdapter=new ConversationAdapter(conversationList);
        message_list.setAdapter(conversationAdapter);
        message_list.setLayoutManager(new LinearLayoutManager(this));
        initJGConversationList();
        conversationAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ConversationType type = conversationList.get(position).getType();
                if(single==type){ //个人
                   UserInfo targetUserInfo= (UserInfo) conversationList.get(position).getTargetInfo();
                    MessageActivity.show(MainActivity.this,targetUserInfo.getUserName());
                }else {  //群聊

                }


            }
        });
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

    @Override
    protected void onResume() {
        super.onResume();
        getMessageList();
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


    public class ConversationAdapter extends BaseQuickAdapter<Conversation,BaseViewHolder>{

        public ConversationAdapter( @Nullable List<Conversation> data) {
            super(R.layout.chat_conversation_item, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, Conversation item) {
//            helper.setImageResource(R.id.icon_service,item.getAvatarFile()); //对方头像
            ImageView heardImg=helper.getView(R.id.icon_service);
            Glide.with(MainActivity.this).fromFile().load(item.getAvatarFile()).into(heardImg);
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
    }


}
