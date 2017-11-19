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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import boby.com.jpushimdemo.R;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.FileContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;

import static boby.com.jpushimdemo.mine.ChatRootView.KEYBOARD_STATE_SHOW;
import static cn.jpush.im.android.api.enums.MessageDirect.receive;
import static cn.jpush.im.android.api.enums.MessageDirect.send;

public class MessageActivity extends AppCompatActivity {
    private SmartRefreshLayout smartRefreshLayout;
    private RecyclerView reMessage_list;
    private List<Message> mMessageList;
    private MessageAdapter messageAdapter;

    private TextView chat_title_txt; //标题
    private View chat_back; //返回

    private String targetUserName; //目标对象
    private Conversation conversation; //会话
    private int index=0;
    private int pageSize=2;
    private ChatRootView rootView ;
    private int lastState=0;
    public static void show(Context context, String targetUserName){
        Intent intent =new Intent(context,MessageActivity.class);
        intent.putExtra("targetUserName",targetUserName);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//无标题
        setContentView(R.layout.activity_message);
        rootView= (ChatRootView) findViewById(R.id.chat_view);
        init();
        initMessage();
        initLinster();
    }

    void init(){
        targetUserName=getIntent().getStringExtra("targetUserName");
        //在进入聊天会话界面时调用，设置当前正在聊天的对象，
        JMessageClient.enterSingleConversation(targetUserName);

        chat_title_txt= (TextView) findViewById(R.id.chat_title_txt);
        chat_back=findViewById(R.id.chat_back);

        chat_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        smartRefreshLayout= (SmartRefreshLayout) findViewById(R.id.smartRefresh);
        smartRefreshLayout.setRefreshHeader(new MyRefrashHeader(this));

        reMessage_list= (RecyclerView) findViewById(R.id.message_list);
        mMessageList=new ArrayList<>();
        messageAdapter=new MessageAdapter(mMessageList);
        reMessage_list.setAdapter(messageAdapter);
        reMessage_list.setLayoutManager(new LinearLayoutManager(this));

        smartRefreshLayout.setEnableLoadmore(false);
        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                index++;
                getMessage();
                smartRefreshLayout.finishRefresh();
                messageAdapter.notifyDataSetChanged();
            }
        });
    }

    void initMessage(){
         conversation= JMessageClient.getSingleConversation(targetUserName);
        chat_title_txt.setText(conversation.getTitle()) ;
        index=0;
        getMessage();
        messageAdapter.notifyDataSetChanged();
        reMessage_list.scrollToPosition(mMessageList.size()-1);
    }

    void initLinster(){
        rootView.setmKeyboardListener(new ChatRootView.OnKeyboardChangedListener() {
            @Override
            public void onKeyBoardStateChanged(int state) { // init, show, hide.
                if(lastState!=state){
                    reMessage_list.scrollToPosition(mMessageList.size()-1);
                    lastState=state;
                }
            }
        });

    }
    void getMessage(){
        List<Message> messagesFromNewest = conversation.getMessagesFromNewest(index, pageSize * (index + 1));
        if (messagesFromNewest!=null){
            if(index==0){
                mMessageList.clear();
            }
            for (Message message : messagesFromNewest) {
                mMessageList.add(0,message);
            }
            if(messagesFromNewest.size()<pageSize){
                smartRefreshLayout.setEnableRefresh(false);
            }
        }else {
            smartRefreshLayout.setEnableRefresh(true);
        }

    }


    public class  MessageAdapter extends BaseQuickAdapter<Message,BaseViewHolder> {

        public MessageAdapter( @Nullable List<Message> data) {
            super(R.layout.single_chat_message_item, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, Message item) {
            if(item.getDirect()==receive){ //发送的消息
                helper.setGone(R.id.chat_hs_left,true);
                helper.setGone(R.id.chat_hs_right,false);

                helper.setGone(R.id.chat_left_msg,false);//文字消息
                helper.setGone(R.id.chat_left_msgimg,false);//图片消息
                helper.setGone(R.id.chat_left_record,false);//语音消息
                helper.setGone(R.id.chat_left_file,false);//文件消息
                switch ( item.getContentType()){
                    case  text:
                        TextContent textContent= (TextContent) item.getContent();
                        helper.setGone(R.id.chat_left_msg,true);//文字消息
                        helper.setText(R.id.chat_left_msg,textContent.getText());
                        break;
                    case  file:
                        FileContent filecontent= (FileContent) item.getContent();
                        helper.setText(R.id.chat_left_filename,filecontent.getFileName());
                        helper.setGone(R.id.chat_left_file,true);//文件消息
                        break;
                    case  image:
                        ImageContent imageContent= (ImageContent) item.getContent();
                        ImageView pic=helper.getView(R.id.chat_left_msgimg);
                        pic.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels*0.5));
                        Glide.with(MessageActivity.this).
                                load(imageContent.getLocalThumbnailPath()).
                                into(pic);
                        helper.setGone(R.id.chat_left_msgimg,true);//图片消息
                        break;
                    case  voice:
                        VoiceContent voiceContent= (VoiceContent) item.getContent();
                        helper.setText(R.id.chat_left_player_time,voiceContent.getDuration());
                        helper.setGone(R.id.chat_left_record,true);//语音消息
                        break;

                    case  location:
                       //位置
                        break;

                    case  video:
                        //视屏
                        break;
                }


            }else if(item.getDirect()==send){
                helper.setGone(R.id.chat_hs_left,false);
                helper.setGone(R.id.chat_hs_right,true);

                helper.setGone(R.id.chat_right_msg,false);//文字消息
                helper.setGone(R.id.chat_right_msgimg,false);//图片消息
                helper.setGone(R.id.chat_right_record,false);//语音消息
                helper.setGone(R.id.chat_right_file,false);//文件消息
                switch ( item.getContentType()){
                    case  text:
                        TextContent textContent= (TextContent) item.getContent();
                        helper.setGone(R.id.chat_right_msg,true);//文字消息
                        helper.setText(R.id.chat_right_msg,textContent.getText());
                        break;
                    case  file:
                        FileContent filecontent= (FileContent) item.getContent();
                        helper.setText(R.id.chat_right_filename,filecontent.getFileName());
                        helper.setGone(R.id.chat_right_file,true);//文件消息
                        break;
                    case  image:
                        ImageContent imageContent= (ImageContent) item.getContent();
                        ImageView pic=helper.getView(R.id.chat_right_msgimg);
                        pic.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels*0.5));
                        Glide.with(MessageActivity.this).
                                load(imageContent.getLocalThumbnailPath()).
                                into(pic);
                        helper.setGone(R.id.chat_right_msgimg,true);//图片消息
                        break;
                    case  voice:
                        VoiceContent voiceContent= (VoiceContent) item.getContent();
                        helper.setText(R.id.chat_right_player_time,voiceContent.getDuration());
                        helper.setGone(R.id.chat_right_record,true);//语音消息
                        break;

                    case  location:
                        //位置
                        break;

                    case  video:
                        //视屏
                        break;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        //推荐在退出聊天会话界面时调用，清除当前正在聊天的对象，用于判断notification是否需要展示
        JMessageClient.exitConversation();
        super.onStop();
    }
}
