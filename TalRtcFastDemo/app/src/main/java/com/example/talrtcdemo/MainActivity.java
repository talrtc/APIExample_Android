package com.example.talrtcdemo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.eaydu.omni.RTCEngine;
import com.eaydu.omni.listener.RTCConnectionStateType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";


    private RTCEngine mRTCEngine;
    public String muserId = "112233";
    private String mroomId = "111111"; //输入自己定义的房间号
    /**
     * 替换成申请到的appID
     */
    private String mAppId = "d46144";

    private FrameLayout videoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoLayout = findViewById(R.id.frameLayout);
        Random rand = new Random();
        muserId = String.valueOf(rand.nextInt(100000));

        mRTCEngine = new RTCEngine(MainActivity.this, mListener);
        mRTCEngine.init(RTCEngine.EngineType.OMNI, Long.parseLong(muserId), mAppId, mroomId);//非token鉴权
//        mRTCEngine.setVideoEncoderConfiguration(480, 640, 15, 200, RTCEngine.RTC_ORIENTATION_MODE.RTC_ORIENTATION_MODE_ADAPTIVE);
//        mRTCEngine.setRecordingAudioParameters(16000, 1);
        SurfaceView view = mRTCEngine.createRendererView();
        view.setZOrderOnTop(true);
        view.setZOrderMediaOverlay(true);
        mRTCEngine.setupLocalVideo(view);

        addVideo("pusher", view);
        mRTCEngine.enableLocalVideo(true);
        mRTCEngine.startPreview();
        int ret = mRTCEngine.joinRoom();
//        Log.d("xxx", "joinRoom----->" + ret);
    }

    private RTCEngine.IRtcEngineEventListener mListener = new RTCEngine.IRtcEngineEventListener() {
        @Override
        public void localUserJoindWithUid(long l) {

        }

        @Override
        public void remotefirstVideoRecvWithUid(long l) {

        }

        @Override
        public void remotefirstAudioRecvWithUid(long l) {

        }

        /**
         * 远端用户加入房间
         * @param l
         */
        @Override
        public void remoteUserJoinWitnUid(final long l) {

            final String viewTag = String.valueOf(l);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (null != mRTCEngine) {
                        SurfaceView view = mRTCEngine.createRendererView();
                        Log.e(TAG, "setupRemoteVideo tag: " + viewTag + " uid: " + l);

                        if (null == view)
                            return;
                        addVideo(viewTag, view);// 每次重进房间后的绑定是必须的. 而且每次最好新建view. 否则可能会有上闪的残留.
                        mRTCEngine.setupRemoteVideo(view, l);
                    }

                }
            });
        }

        @Override
        public void didOfflineOfUid(final long uid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String userid = String.valueOf(uid);
                    if (String.valueOf(uid).equals(muserId)) {
                    } else {
                        removeRemoteVideo(userid);
                    }

                }
            });
        }

        @Override
        public void didAudioMuted(long l, boolean b) {

        }

        @Override
        public void didVideoMuted(long l, boolean b) {

        }

        @Override
        public void didOccurError(RTCEngine.RTCEngineErrorCode rtcEngineErrorCode) {

        }

        @Override
        public void connectionChangedToState(RTCConnectionStateType rtcConnectionStateType, String s) {

        }

        @Override
        public void reportAudioVolumeOfSpeaker(long l, int i) {

        }

        @Override
        public void onRemoteVideoStateChanged(long l, int i) {

        }

        @Override
        public void onOnceLastMileQuality(RTCEngine.RTC_LASTMILE_QUALITY rtc_lastmile_quality) {

        }

        @Override
        public void reportRtcStats(RTCEngine.ReportRtcStats reportRtcStats) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRTCEngine != null) {
            mRTCEngine.leaveRoom();
            mRTCEngine.destory();
        }
        mRTCEngine = null;
    }

    public List<BtClass> btStatuss = new ArrayList<>();

    class BtClass {
        public String uid;
        public boolean isMute_audio;
        public boolean isMute_video;
    }

    final int numberPerRow = 2;
    private void removeRemoteVideo(String uid) {
        // 处理列表显示的小视频画面
        for (int i = videoLayout.getChildCount() - 1; i >= 0; i--) {
            View childAt = videoLayout.getChildAt(i);
            if (("" + uid).equals(childAt.getTag() + "")) {
                // 视频布局列表移除指定uid的视频画面
                videoLayout.removeView(childAt);
                // 更新画面
                updateFrameLayout();
                Log.d(TAG, "remove view: " + uid);
            }
        }
    }
    private void updateFrameLayout() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float Interval = 2.0f;
        Interval = numberPerRow;

        int size = videoLayout.getChildCount();
        int pw = (int) ((displayMetrics.widthPixels > displayMetrics.heightPixels ? displayMetrics.heightPixels : displayMetrics.widthPixels) / Interval);
        int ph = pw;
        for (int i = 0; i < size; i++) {

            View view = videoLayout.getChildAt(i);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(pw, ph);

            layoutParams.leftMargin = (i % (int) (Interval)) * pw;
            layoutParams.topMargin = (i / (int) (Interval)) * ph;

            videoLayout.updateViewLayout(view, layoutParams);
            videoLayout.requestLayout();
        }
    }
    private void addVideo(String userId, View videoView) {
        BtClass btClass = new BtClass();
        btClass.uid = userId;
        btStatuss.add(btClass);
        Log.d(TAG, "addVideo   userId:" + userId + " muserId: " + muserId);

        videoView.setClickable(true);

        videoView.setTag(userId);

        // 如果容器已存在同一个用户的画面，则先删除再添加到前面
        for (int i = videoLayout.getChildCount() - 1; i >= 0; i--) {
            ViewGroup parentOld = (ViewGroup) videoLayout.getChildAt(i);
            if (("" + userId).equals(parentOld.getTag() + "")) {
                ViewGroup.LayoutParams lpParentOld = parentOld.getLayoutParams();
                View videoViewOld = parentOld.getChildAt(0);
                ViewGroup.LayoutParams lpVideoViewOld = videoViewOld.getLayoutParams();
                parentOld.removeView(videoViewOld);
                parentOld.addView(videoView, 0, lpVideoViewOld);
                videoLayout.removeView(parentOld);
                videoLayout.addView(parentOld, i, lpParentOld);
                return;
            }
        }

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        int size = videoLayout.getChildCount();
        int pw = (int) (displayMetrics.widthPixels * 1.0 / numberPerRow);
        int ph = pw;

        // 创建包含视频画面和统计信息的容器
        RelativeLayout parent = new RelativeLayout(getApplicationContext());
        parent.setTag(userId);
        FrameLayout.LayoutParams lpParent = new FrameLayout.LayoutParams(pw, ph);
        lpParent.leftMargin = (size % numberPerRow) * pw;
        lpParent.topMargin = (size / numberPerRow) * ph;

        // 添加显示视频画面
        RelativeLayout.LayoutParams lpVideo = new RelativeLayout.LayoutParams(pw, ph);
        parent.addView(videoView, lpVideo);

        // 添加显示video统计信息的容器
//        LinearLayout videoStatusContainer = new LinearLayout(getApplicationContext());
//        RelativeLayout.LayoutParams lpVideoStatus = new RelativeLayout.LayoutParams(pw, ph);
//        parent.addView(videoStatusContainer, lpVideoStatus);
//

        // 将包含视频画面和统计信息的容器添加到视频列表容器中
        videoLayout.addView(parent, 0, lpParent);

        videoView.requestLayout();
    }

}