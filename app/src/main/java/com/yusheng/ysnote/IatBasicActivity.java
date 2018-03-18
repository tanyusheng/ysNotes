package com.yusheng.ysnote;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

public  class IatBasicActivity extends Activity  {

    public static final String PRIVATE_SETTING="com.iflytek.setting";           //缓存数据的名称
    private static final String TAG = "BasicIatActivity";
    private EditText mContent;                          //显示内容
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private SharedPreferences mSharedPreferences;

    private Toast mToast;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    int ret = 0; // 函数调用返回值
    //[] 表示可以匹配的字符规模,即类型         {}描述匹配出现的频度    ()提取匹配的字符串,表达式中有几个()就有几个响应的子串
    private static String REG_EXP_DATE_FLIGHT="[\\d]{1,2}[\\u4200-\\u9fa5][\\d]{1,2}[\\u4200-\\u9fa5]";

    private static String REG_EXP_NUM_FLIGHT="[a-zA-Z]{1,2}[\\d]{4,}";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5a9f5c6a");
        Log.d(TAG, "onCreate--->");
        initIat();

        //初始化组件   后期修改为，程序一进入的时候进行初始化
        mToast = Toast.makeText(IatBasicActivity.this,"",Toast.LENGTH_SHORT);

//        String content="我要查3月20号,CZ1234航班";
//        Pattern pattern1=Pattern.compile(REG_EXP_DATE_FLIGHT);
//        Matcher matcher1=pattern1.matcher(content);
//        while(matcher1.find()){//从目标字符串中依次取出特定的子串,对应正则表达式里面的()
//            int start=matcher1.start();
//            int end=matcher1.end();
//            String temp=content.substring(start,end);
//            Log.d("temptext","temptextDate="+temp);
//        }
//
//        Pattern pattern2=Pattern.compile(REG_EXP_NUM_FLIGHT);
//        Matcher matcher2=pattern2.matcher(content);
//        while(matcher2.find()){//从目标字符串中依次取出特定的子串,对应正则表达式里面的()
//            int start=matcher2.start();
//            int end=matcher2.end();
//            String temp=content.substring(start,end);
//            Log.d("temptext","temptextNum="+temp);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(null!=mIat){
            //退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }

    /**
     * 显示toast
     * @param content  待显示的内容
     */
    public void showTip(String content){
        mToast.setText(content);
        mToast.show();
    }

    /**
     * 初始化 EditText 在需要调用语音接口中的类的初始化方法中调用该方法
     *
     * @param mContent
     */
    public void initIatData(EditText mContent) {
        this.mContent = mContent;
    }

    /**
     * 初始化语音识别中的对象
     */
    private void initIat() {
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(this, mInitListener);
        mSharedPreferences = getSharedPreferences(PRIVATE_SETTING,
                Activity.MODE_PRIVATE);

    }

    /**
     * 初始化监听器
     */
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            printResult(results, mContent);

            Log.d(TAG, results.getResultString());
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }
    };
    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
            //mRecognitionProgressView.play();
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            printResult(results, mContent);
            Log.d(TAG, results.getResultString());
            if (isLast) {
                // TODO 最后的结果
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
            // mRecognitionProgressView.onRmsChanged(volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    /**
     * 参数设置
     * @param
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        String lag = mSharedPreferences.getString("iat_language_preference",
                "mandarin");//普通话
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");//美式英文
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");//简体中文
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        }
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }


    /**
     * 触发点击事件时的方法
     */
    public void clickMethod() {
       // mContent.setText(null);// 清空显示内容
       // mIatResults.clear();
        // 设置参数
        setParam();
//        boolean isShowDialog = mSharedPreferences.getBoolean(getString(R.string.pref_key_iat_show), true);
//        if (isShowDialog) {
//            // 显示听写对话框
//            mIatDialog.setListener(mRecognizerDialogListener);
//            mIatDialog.show();
//            showTip(getString(R.string.text_begin));
//        } else {
//            // 不显示听写对话框
//            ret = mIat.startListening(mRecognizerListener);
//            if (ret != ErrorCode.SUCCESS) {
//                Log.d(TAG, "听写失败,错误码：" + ret);
//            } else {
//                showTip(getString(R.string.text_begin));
//            }
//        }
        ret=mIat.startListening(mRecognizerListener);
    }

    /**
     * 显示结果,并赋值到EditText文本框中
     *
     * @param results
     * @param editText
     */
    private void printResult(RecognizerResult results, EditText editText) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        String content = resultBuffer.toString();
        Log.d(TAG, content);
        content = content.replace("。", "".trim());           //去掉最后面的 。
        editText.setText(content);
        editText.setSelection(content.length());


    }

}
