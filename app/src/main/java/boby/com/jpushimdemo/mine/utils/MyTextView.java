package boby.com.jpushimdemo.mine.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import boby.com.jpushimdemo.IMUISampleApplication;

public class MyTextView extends TextView{
	/**

     * ＠author Dragon

     * SpanInfo 类用于存储一个要显示的（动态或静态）的信息，包含分化后的每一帧mapList、调换文字的肇端地位、终止地位

     * 、帧的总数、当前须要显示的帧、帧与帧之间的时候间隔 

     */

    private class SpanInfo{

        ArrayList<Bitmap> mapList;

        int start,end,frameCount,currentFrameIndex,delay;

        public SpanInfo(){

            mapList=new ArrayList<Bitmap>();

            start=end=frameCount=currentFrameIndex=delay=0;    

        }

    }

    

    /**

     * spanInfoList 是一个SpanInfo的list ，用于处理惩罚一个TextView中呈现多个要匹配的的景象

     */

    private ArrayList<SpanInfo> spanInfoList=null;

    private Handler handler;           //用于处理惩罚从子线程TextView传来的消息

    private String myText;             //存储textView应当显示的文本

    

    /**

     * 这三个机关办法一个也不要少，不然会产生CastException，重视在这三个机关函数中都为spanInfoList实例化，可能有些浪费

     * ，但包管不会有空指针异常

     * ＠param context

     * ＠param attrs

     * ＠param defStyle

     */

    public MyTextView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);

        // TODO Auto-generated constructor stub

        spanInfoList=new ArrayList<SpanInfo>();

    }



    public MyTextView(Context context, AttributeSet attrs) {

        super(context, attrs);

        // TODO Auto-generated constructor stub

        spanInfoList=new ArrayList<SpanInfo>();

    }



    public MyTextView(Context context) {

        super(context);

        // TODO Auto-generated constructor stub

        spanInfoList=new ArrayList<SpanInfo>();

    }

    

    

    

    /**

     * 对要显示在textView上的文本进行解析，看看是否文本中有须要与Gif或者静态匹配的文本

     * 如有，那么调用parseGif 对该文本对应的Gif进行解析 或者嗲用parseBmp解析静态

     * ＠param inputStr

     */

    private void parseText(String inputStr){

        myText=inputStr;

        Pattern mPattern=Pattern.compile("\\[(\\S+?)\\]");

        Matcher mMatcher=mPattern.matcher(inputStr);

        while(mMatcher.find()){

            String faceName=mMatcher.group();

            Integer faceId=null;

            /**

             * 这里匹配时用到了库，即一个专门存放id和其匹配的名称的静态对象，这两个静态对象放在了FaceData.java

             * 中，并采取了静态块的办法进行了初始化，不会有空指针异常

             */

            

            /*if((faceId=FaceData.gifFaceInfo.get(faceName))!=null){

                parseGif(faceId, mMatcher.start(), mMatcher.end());

            }

            else if((faceId=FaceData.staticFaceInfo.get(faceName))!=null){

                parseBmp(faceId, mMatcher.start(), mMatcher.end());

            }*/
            if ((faceId = IMUISampleApplication.getInstance().getFaceMap().get(faceName)) != null) {
            	 Log.e("faceName = " ,faceName);
            	 Log.e("faceId = ",""+ faceId);
            	 parseGif(faceId, mMatcher.start(), mMatcher.end());
			}

            

        }

        

        

    }

    

    /**

     * 对静态进行解析：

     * 创建一个SpanInfo对象，帧数设为1，遵守下面的参数设置，最后不要忘怀将SpanInfo对象添加进spanInfoList中，

     * 不然不会显示

     * ＠param resourceId

     * ＠param start

     * ＠param end

     */

    private void parseBmp(int resourceId,int start, int end) {

        Bitmap bitmap=BitmapFactory.decodeResource(getContext().getResources(), resourceId);

        ImageSpan imageSpan=new ImageSpan(getContext(),bitmap);

        SpanInfo spanInfo=new SpanInfo();

        spanInfo.currentFrameIndex=0;

        spanInfo.frameCount=1;

        spanInfo.start=start;

        spanInfo.end=end;

        spanInfo.delay=100;

        spanInfo.mapList.add(bitmap);

        spanInfoList.add(spanInfo);

        

    }

    

    /**

     * 解析Gif，与静态独一的不合是这里须要调用GifOpenHelper类读取Gif返回一系一组bitmap（用for 轮回把这一

     * 组的bitmap存储在SpanInfo.mapList中，此时的frameCount参数也大于1了）

     * ＠param resourceId

     * ＠param start

     * ＠param end

     */

    private void parseGif(int resourceId ,int start, int end){   

    

        GifOpenHelper helper=new GifOpenHelper();

        helper.read(getContext().getResources().openRawResource(resourceId));

        SpanInfo spanInfo=new SpanInfo();

        spanInfo.currentFrameIndex=0;

        spanInfo.frameCount=helper.getFrameCount();

        spanInfo.start=start;

        spanInfo.end=end;

        spanInfo.mapList.add(helper.getImage());

        for(int i=1; i<helper.getFrameCount(); i++){

            spanInfo.mapList.add(helper.nextBitmap());

        }

        spanInfo.delay=helper.nextDelay();        //获得每一帧之间的延迟

        spanInfoList.add(spanInfo);



    }

    

    /**

     * MyTextView 与外部对象的接口，今后设置文本内容时应用setSpanText（） 而不再是setText（）;

     * ＠param handler

     * ＠param text

     */

    public void setSpanText(Handler handler, String text){

        this.handler=handler;      //获得UI的Handler

        parseText(text);           //对String对象进行解析

        TextRunnable r=new TextRunnable();   //生成Runnable对象

        handler.post(r);       //哄骗UI线程的Handler 将r添加进消息队列中。

        

    }

    

    

    public class TextRunnable implements Runnable{



        @Override

        public void run() {

            // TODO Auto-generated method stub

            SpannableString sb=new SpannableString(""+myText);   //获得要显示的文本

            int gifCount=0;

            SpanInfo info=null;

            for(int i=0; i<spanInfoList.size(); i++){  //for轮回，处理惩罚显示多个的题目

                info=spanInfoList.get(i);

                if(info.mapList.size()>1){       

                    /*

                     * gifCount用来区分是Gif还是BMP，若是gif gifCount>0 ，不然gifCount=0

                     */

                    gifCount++;

                    

                }

                Bitmap bitmap=info.mapList.get(info.currentFrameIndex);

                info.currentFrameIndex=(info.currentFrameIndex+1)%(info.frameCount);

                /**

                 * currentFrameIndex 用于把握当前应当显示的帧的序号，每次显示之后currentFrameIndex

                 * 应当加1 ，加到frameCount后再变成0轮回显示

                 */

                

                if(gifCount!=0){

                    bitmap=Bitmap.createScaledBitmap(bitmap, 60, 60, true);

                

                }

                else{

                    bitmap=Bitmap.createScaledBitmap(bitmap, 30, 30, true);

                }

                ImageSpan imageSpan=new ImageSpan(getContext(), bitmap);   

                sb.setSpan(imageSpan, info.start, info.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                

            }

            //对所有的对应的ImageSpan完成设置后，调用TextView的setText办法设置文本

            MyTextView.this.setText(sb);  

            

            /**

             * 这一步是为了节俭内存而是用，即若是文本中只有静态没有动态，那么该线程就此终止，不会反复履行

             * 。而若是有动图，那么会一向履行

             */

            if(gifCount!=0){

                handler.postDelayed(this, info.delay);

            

            }

            

        }

        

    }

    

    


}
