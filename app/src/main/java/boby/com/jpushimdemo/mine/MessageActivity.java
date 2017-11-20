package boby.com.jpushimdemo.mine;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnMultiPurposeListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadmoreListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import boby.com.jpushimdemo.R;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.content.FileContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.options.MessageSendingOptions;

import static boby.com.jpushimdemo.mine.ChatRootView.KEYBOARD_STATE_INIT;
import static boby.com.jpushimdemo.mine.ChatRootView.KEYBOARD_STATE_SHOW;
import static cn.jpush.im.android.api.enums.MessageDirect.receive;
import static cn.jpush.im.android.api.enums.MessageDirect.send;

public class MessageActivity extends AppCompatActivity {
    private SmartRefreshLayout smartRefreshLayout;
    private RecyclerView reMessage_list;
    private LinearLayoutManager linearLayoutManager;
    private List<Message> mMessageList;
    private MessageAdapter messageAdapter;

    private TextView chat_title_txt; //标题
    private View chat_back; //返回

    //底部输入菜单相关
    private ImageView face_switch_btn;    //表情
    private ImageView face_add_btn; //选择其它文件按钮
    private EditText et_chat_content;     // 文字输入框
    private Button et_chat_video;         //语音按钮
    private ImageView face_video_btn;     //语音/文字切换按钮
    private TextView img_chat_send;       //文字发送按钮
    private View file_ll;                  //其它文件类型选择布局
    private View face_ll;                 //表情布局

    private boolean mIsFaceShow = false;// 是否显示表情
    private boolean mIsMenuShow = false;//是否显示文件选择菜单
    private boolean mIsVideShow = false;//是否显示录音
    private InputMethodManager mInputMethodManager;

    private String targetUserName; //目标对象
    private long groupId = -1;//群 id
    private Conversation conversation; //会话
    private int index = 0;
    private int pageSize = 10;
    private ChatRootView rootView;
    private int lastState = 0;
    private int loadSize; //加载大小

    /**
     * @param context
     * @param targetUserName 对方用户id
     */
    public static void show(Context context, String targetUserName) {
        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra("targetUserName", targetUserName);
        context.startActivity(intent);
    }

    /**
     * 群聊对话
     *
     * @param context
     * @param groupId 群id
     */

    public static void show(Context context, long groupId) {
        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra("groupId", groupId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//无标题
        setContentView(R.layout.activity_message);
        rootView = (ChatRootView) findViewById(R.id.chat_view);
        initView();
        init();
        initMessage();
        initLinster();
    }

    void initView() {
        chat_title_txt = (TextView) findViewById(R.id.chat_title_txt);
        chat_back = findViewById(R.id.chat_back);
        smartRefreshLayout = (SmartRefreshLayout) findViewById(R.id.smartRefresh);
        reMessage_list = (RecyclerView) findViewById(R.id.message_list);

        //底部输入菜单相关
        face_switch_btn = (ImageView) findViewById(R.id.face_switch_btn);
        face_add_btn = (ImageView) findViewById(R.id.face_add_btn);
        et_chat_content = (EditText) findViewById(R.id.et_chat_content);
        et_chat_video = (Button) findViewById(R.id.et_chat_video);
        face_video_btn = (ImageView) findViewById(R.id.face_video_btn);
        img_chat_send = (TextView) findViewById(R.id.img_chat_send);
        file_ll = findViewById(R.id.file_ll);
        face_ll = findViewById(R.id.face_ll);

    }

    void init() {
        targetUserName = getIntent().getStringExtra("targetUserName");
        //在进入聊天会话界面时调用，设置当前正在聊天的对象，
        groupId = getIntent().getLongExtra("groupId", -1);
        if (groupId == -1) {
            JMessageClient.enterSingleConversation(targetUserName);
        } else {
            JMessageClient.enterGroupConversation(groupId);
        }

        smartRefreshLayout.setRefreshHeader(new MyRefrashHeader(this));
        mMessageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(mMessageList);
        //下拉加载
        messageAdapter.setUpFetchEnable(true);

        reMessage_list.setAdapter(messageAdapter);
        linearLayoutManager = new LinearLayoutManager(this);
        reMessage_list.setLayoutManager(linearLayoutManager);

        smartRefreshLayout.setEnableLoadmore(false);
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        JMessageClient.registerEventReceiver(this);
    }

    void initMessage() {
        if (groupId == -1) {
            conversation = JMessageClient.getSingleConversation(targetUserName);
        } else {
            conversation = JMessageClient.getGroupConversation(groupId);
        }
        chat_title_txt.setText(conversation.getTitle());
        index = 0;
        getMessage();
        messageAdapter.notifyDataSetChanged();
        reMessage_list.scrollToPosition(mMessageList.size() - 1);
    }

    void initLinster() {

        chat_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rootView.setmKeyboardListener(new ChatRootView.OnKeyboardChangedListener() {
            @Override
            public void onKeyBoardStateChanged(int state) { // init, show, hide.
                if (KEYBOARD_STATE_INIT != state && lastState != state) {
                    Log.e("messageActivity", "  state:" + state);
                    reMessage_list.scrollToPosition(mMessageList.size() - 1);
                    lastState = state;
                }
            }
        });
        //文字发送按钮
        img_chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = conversation.createSendTextMessage(et_chat_content.getText().toString());
                MessageSendingOptions options = new MessageSendingOptions();
                options.setNeedReadReceipt(true);
                JMessageClient.sendMessage(msg, options);
                messageAdapter.addMessageData(msg);
                et_chat_content.setText("");
                reMessage_list.scrollToPosition(mMessageList.size() - 1);
            }
        });

        //输入文字监听，是否显示文字发送按钮
        et_chat_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    img_chat_send.setVisibility(View.GONE);
                    face_video_btn.setVisibility(View.VISIBLE);
                } else {
                    img_chat_send.setVisibility(View.VISIBLE);
                    face_video_btn.setVisibility(View.GONE);
                }
            }
        });

        //下拉获取历史消息
        smartRefreshLayout.setOnMultiPurposeListener(new OnMultiPurposeListener() {
            @Override
            public void onHeaderPulling(RefreshHeader header, float percent, int offset, int headerHeight, int extendHeight) {

            }

            @Override
            public void onHeaderReleasing(RefreshHeader header, float percent, int offset, int headerHeight, int extendHeight) {

            }

            @Override
            public void onHeaderStartAnimator(RefreshHeader header, int headerHeight, int extendHeight) {

            }

            @Override
            public void onHeaderFinish(RefreshHeader header, boolean success) {
                messageAdapter.setUpFetching(false);
//                messageAdapter.setUpFetchListener();
                int scrollY = reMessage_list.getScrollY();
                int height = linearLayoutManager.getHeight();
                messageAdapter.notifyDataSetChanged();
                reMessage_list.scrollToPosition(loadSize - 1);
                reMessage_list.scrollBy(0, height - 60);
            }

            @Override
            public void onFooterPulling(RefreshFooter footer, float percent, int offset, int footerHeight, int extendHeight) {

            }

            @Override
            public void onFooterReleasing(RefreshFooter footer, float percent, int offset, int footerHeight, int extendHeight) {

            }

            @Override
            public void onFooterStartAnimator(RefreshFooter footer, int footerHeight, int extendHeight) {

            }

            @Override
            public void onFooterFinish(RefreshFooter footer, boolean success) {

            }

            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {

            }

            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                index++;
                messageAdapter.setUpFetching(true);
                getMessage();
                smartRefreshLayout.finishRefresh();
            }

            @Override
            public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) {

            }
        });

        //选择文件，和输入文字切换
        face_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsMenuShow) {
                    mInputMethodManager.hideSoftInputFromWindow(
                            et_chat_content.getWindowToken(), 0);
                    file_ll.setVisibility(View.VISIBLE);
                    mIsMenuShow = true;
                    face_add_btn.setImageResource(R.drawable.chat_iconinput);
                    // 将表情缩下去
                    face_ll.setVisibility(View.GONE);
                    face_switch_btn.setImageResource(R.drawable.chat_expression);

                    // 将录音换成输入框
                    et_chat_content.setVisibility(View.VISIBLE);
                    et_chat_video.setVisibility(View.GONE);
                    mIsVideShow = false;
                    face_video_btn.setImageResource(R.drawable.chat_iconvoice);

                    mIsFaceShow = false;
                } else {
                    mInputMethodManager.showSoftInput(et_chat_content, 1);
                    face_add_btn.setImageResource(R.drawable.chat_iconmore);
                    file_ll.setVisibility(View.GONE);
                    mIsMenuShow = false;
                }
            }
        });
    }

    void getMessage() {
        List<Message> messagesFromNewest = conversation.getMessagesFromNewest(index * pageSize, pageSize);
        if (messagesFromNewest != null) {
            if (messagesFromNewest.size() < pageSize) {
                smartRefreshLayout.setEnableRefresh(false);
                messageAdapter.setUpFetchEnable(false);
            }
            if (index == 0) {
                mMessageList.clear();
            }
            for (Message message : messagesFromNewest) {
                mMessageList.add(0, message);
            }
            loadSize = messagesFromNewest.size();
        } else {
            smartRefreshLayout.setEnableRefresh(false);
            messageAdapter.setUpFetchEnable(false);
            loadSize = 0;
        }

    }


    public void onEvent(MessageEvent event) {
        final Message message = event.getMessage();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (message.getTargetType() == ConversationType.single) {
                    UserInfo userInfo = (UserInfo) message.getTargetInfo();
                    if (userInfo.getUserName().equals(targetUserName)) {
                        Message lastMsg = messageAdapter.getLastMessage();
                        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();//获取最后一条可见item的位置
                        if (lastMsg == null || message.getId() != lastMsg.getId()) {
                            messageAdapter.addMessageData(message);
                            if (lastVisibleItemPosition == mMessageList.size() - 2) { //如果最后一条可见则定位可见消息到最后一条
                                reMessage_list.scrollToPosition(mMessageList.size() - 1);
                            }
                        } else {
                            messageAdapter.notifyDataSetChanged();
                            if (lastVisibleItemPosition == mMessageList.size() - 1) { //如果最后一条可见则定位可见消息到最后一条
                                reMessage_list.scrollToPosition(mMessageList.size() - 1);
                            }
                        }
                    }
                } else {
                    //群消息
                    GroupInfo groupInfo = (GroupInfo) message.getTargetInfo();
                    if (groupInfo.getGroupID() == groupId) {
                        Message lastMsg = messageAdapter.getLastMessage();
                        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();//获取最后一条可见item的位置
                        if (lastMsg == null || message.getId() != lastMsg.getId()) {
                            messageAdapter.addMessageData(message);
                            if (lastVisibleItemPosition == mMessageList.size() - 2) { //如果最后一条可见则定位可见消息到最后一条
                                reMessage_list.scrollToPosition(mMessageList.size() - 1);
                            }
                        } else {
                            messageAdapter.notifyDataSetChanged();
                            if (lastVisibleItemPosition == mMessageList.size() - 1) { //如果最后一条可见则定位可见消息到最后一条
                                reMessage_list.scrollToPosition(mMessageList.size() - 1);
                            }
                        }
                    }
                }
            }
        });
        switch (message.getContentType()) {
            case text:
                //处理文字消息
                TextContent textContent = (TextContent) message.getContent();
                textContent.getText();
                break;
            case image:
                //处理图片消息
                ImageContent imageContent = (ImageContent) message.getContent();
                imageContent.getLocalPath();//图片本地地址
                imageContent.getLocalThumbnailPath();//图片对应缩略图的本地地址
                break;
            case voice:
                //处理语音消息
                VoiceContent voiceContent = (VoiceContent) message.getContent();
                voiceContent.getLocalPath();//语音文件本地地址
                voiceContent.getDuration();//语音文件时长
                break;
            case custom:
                //处理自定义消息
                CustomContent customContent = (CustomContent) message.getContent();
                customContent.getNumberValue("custom_num"); //获取自定义的值
                customContent.getBooleanValue("custom_boolean");
                customContent.getStringValue("custom_string");
                break;
            case eventNotification:
                //处理事件提醒消息
                EventNotificationContent eventNotificationContent = (EventNotificationContent) message.getContent();
                switch (eventNotificationContent.getEventNotificationType()) {
                    case group_member_added:
                        //群成员加群事件
                        break;
                    case group_member_removed:
                        //群成员被踢事件
                        break;
                    case group_member_exit:
                        //群成员退群事件
                        break;
                    case group_info_updated://since 2.2.1
                        //群信息变更事件
                        break;
                }
                break;
        }
    }

    public class MessageAdapter extends BaseQuickAdapter<Message, BaseViewHolder> {

        public MessageAdapter(@Nullable List<Message> data) {
            super(R.layout.single_chat_message_item, data);
        }

        @Override
        protected void convert(final BaseViewHolder helper, Message item) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String sendDate = sdf.format(new Date(item.getCreateTime()));

            long longtime = item.getCreateTime();
            int position = helper.getAdapterPosition();
            boolean isShowTime = false;
            if (position > 0) {
                long lastTime = (mMessageList.get(position - 1).getCreateTime());
                isShowTime = longtime - lastTime < 60 * 1000;
            }
            UserInfo mUserInfo = (UserInfo) item.getTargetInfo();
            if (item.getDirect() == receive) { //发送的消息
                helper.setGone(R.id.chat_hs_left, true);
                helper.setGone(R.id.chat_hs_right, false);

                helper.setGone(R.id.chat_left_msg, false);//文字消息
                helper.setGone(R.id.chat_left_msgimg, false);//图片消息
                helper.setGone(R.id.chat_left_record, false);//语音消息
                helper.setGone(R.id.chat_left_file, false);//文件消息

                helper.setGone(R.id.chat_letf_time, isShowTime);
                helper.setText(R.id.chat_letf_time, sendDate);
                mUserInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bitmap) {
                        if (status == 0) {
                            helper.setImageBitmap(R.id.chat_left_img, bitmap);
                        } else {
                            helper.setImageResource(R.id.chat_left_img, R.drawable.ic_launcher);
                        }
                    }
                });

                switch (item.getContentType()) {
                    case text:
                        TextContent textContent = (TextContent) item.getContent();
                        helper.setGone(R.id.chat_left_msg, true);//文字消息
                        helper.setText(R.id.chat_left_msg, textContent.getText());
                        break;
                    case file:
                        FileContent filecontent = (FileContent) item.getContent();
                        helper.setText(R.id.chat_left_filename, filecontent.getFileName());
                        helper.setGone(R.id.chat_left_file, true);//文件消息
                        break;
                    case image:
                        ImageContent imageContent = (ImageContent) item.getContent();
                        ImageView pic = helper.getView(R.id.chat_left_msgimg);
                        pic.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.5));
                        Glide.with(MessageActivity.this).
                                load(imageContent.getLocalThumbnailPath()).
                                into(pic);
                        helper.setGone(R.id.chat_left_msgimg, true);//图片消息
                        break;
                    case voice:
                        VoiceContent voiceContent = (VoiceContent) item.getContent();
                        helper.setText(R.id.chat_left_player_time, voiceContent.getDuration());
                        helper.setGone(R.id.chat_left_record, true);//语音消息
                        break;

                    case location:
                        //位置
                        break;

                    case video:
                        //视屏
                        break;
                }

            } else if (item.getDirect() == send) {
                helper.setGone(R.id.chat_hs_left, false);
                helper.setGone(R.id.chat_hs_right, true);
                helper.setGone(R.id.chat_right_msg, false);//文字消息
                helper.setGone(R.id.chat_right_msgimg, false);//图片消息
                helper.setGone(R.id.chat_right_record, false);//语音消息
                helper.setGone(R.id.chat_right_file, false);//文件消息
                helper.setGone(R.id.chat_right_time, isShowTime);
                helper.setText(R.id.chat_right_time, sendDate);

                mUserInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bitmap) {
                        if (status == 0) {
                            helper.setImageBitmap(R.id.chat_right_img, bitmap);
                        } else {
                            helper.setImageResource(R.id.chat_right_img, R.drawable.ic_launcher);
                        }
                    }
                });
                switch (item.getContentType()) {
                    case text:
                        TextContent textContent = (TextContent) item.getContent();
                        helper.setGone(R.id.chat_right_msg, true);//文字消息
                        helper.setText(R.id.chat_right_msg, textContent.getText());
                        break;
                    case file:
                        FileContent filecontent = (FileContent) item.getContent();
                        helper.setText(R.id.chat_right_filename, filecontent.getFileName());
                        helper.setGone(R.id.chat_right_file, true);//文件消息
                        break;
                    case image:
                        ImageContent imageContent = (ImageContent) item.getContent();
                        ImageView pic = helper.getView(R.id.chat_right_msgimg);
                        pic.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.5));
                        Glide.with(MessageActivity.this).
                                load(imageContent.getLocalThumbnailPath()).
                                into(pic);
                        helper.setGone(R.id.chat_right_msgimg, true);//图片消息
                        break;
                    case voice:
                        VoiceContent voiceContent = (VoiceContent) item.getContent();
                        helper.setText(R.id.chat_right_player_time, voiceContent.getDuration());
                        helper.setGone(R.id.chat_right_record, true);//语音消息
                        break;

                    case location:
                        //位置
                        break;

                    case video:
                        //视屏
                        break;
                }
            }

        }

        /**
         * 添加最新消息都列表
         */
        public void addMessageData(Message data) {
            super.addData(mMessageList.size(), data);
        }

        /**
         * 获取最新一条消息
         */
        public Message getLastMessage() {
            int size = mMessageList.size();
            if (size != 0) {
                return mMessageList.get(size - 1);
            } else {
                return null;
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
        JMessageClient.unRegisterEventReceiver(this);
        super.onStop();
    }
}
