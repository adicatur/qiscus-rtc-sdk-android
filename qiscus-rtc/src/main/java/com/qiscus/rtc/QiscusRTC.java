package com.qiscus.rtc;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.qiscus.rtc.data.config.CallConfig;
import com.qiscus.rtc.data.local.LocalDataManager;
import com.qiscus.rtc.data.model.QiscusRTCAccount;
import com.qiscus.rtc.data.model.QiscusRTCCall;
import com.qiscus.rtc.data.model.QiscusRTCSession;
import com.qiscus.rtc.ui.call.activity.QiscusCallActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by fitra on 2/10/17.
 */


public class QiscusRTC {
    private static final String TAG = QiscusRTC.class.getSimpleName();

    public static Application getAppInstance() {
        return appInstance;
    }

    public static Context getApplicationContext() {
        return applicationContext;
    }

    private static Application appInstance;
    private static volatile Context applicationContext;
    private static volatile Handler applicationHandler;
    private static QiscusRTCAccount account;
    private static QiscusRTCSession session;
    private static ScheduledThreadPoolExecutor taskExecutor;

    private QiscusRTC() {
        //
    }

    /**
     * Use this method to start an Activity for call with other user.
     *
     * @param roomCallId generated manually of roomCallId from main app.
     * @return Call Activity builder
     */
    public static RequiredRoomId buildCallWith(String roomCallId) {
        return new CallActivityBuilder(roomCallId);
    }

    public interface RequiredRoomId {
        RequiredCallAs setCallAs(QiscusRTC.CallAs callAs);
    }

    public interface RequiredCallAs {
        RequiredCallType setCallType(QiscusRTC.CallType callType);
    }

    public interface RequiredCallType {
        RequiredUniqeIdCallee setUniqeIdCallee(String uniqueIdCallee);
    }

    public interface RequiredUniqeIdCallee {
        OptionalMethod setCalleeDisplayName(String calleeDisplayName);
        OptionalMethod setCalleeDisplayAvatar(String calleeDisplayAvatar);
        OptionalMethod setCallerDisplayName(String callerDisplayName);
        OptionalMethod setCallerDisplayAvatar(String callerDisplayAvatar);
        OptionalMethod setUniqeIdCaller(String uniqueIdCaller);
        QiscusRTC show(Context context);
    }

    public interface OptionalMethod {
        OptionalMethod setCalleeDisplayName(String calleeDisplayName);
        OptionalMethod setCalleeDisplayAvatar(String calleeDisplayAvatar);
        OptionalMethod setCallerDisplayName(String callerDisplayName);
        OptionalMethod setCallerDisplayAvatar(String callerDisplayAvatar);
        OptionalMethod setUniqeIdCaller(String uniqueIdCaller);
        QiscusRTC show(Context context);
    }

    public static class CallActivityBuilder extends QiscusRTC implements RequiredRoomId, RequiredCallAs, RequiredCallType, RequiredUniqeIdCallee,
            OptionalMethod {
        private String roomCallId;
        private QiscusRTC.CallAs callAs;
        private String callerDisplayName;
        private String callerDisplayAvatar;
        private QiscusRTC.CallType callType;
        private String calleeDisplayName;
        private String calleeDisplayAvatar;
        private String uniqueIdCaller;
        private String uniqueIdCallee;

        private CallActivityBuilder(String roomCallId) {
            this.roomCallId = roomCallId;
            this.callerDisplayAvatar = "";
            this.callerDisplayName = "";
            this.calleeDisplayAvatar = "";
            this.calleeDisplayName="";
            this.uniqueIdCaller="";
        }

        /**
         * Set the setCallAs of call activity.
         *
         * @param callAs person as caller or callee.
         * @return builder
         */
        @Override
        public RequiredCallAs setCallAs(CallAs callAs) {
            this.callAs = callAs;
            return this;
        }

        /**
         * Set the setCallType of call activity.
         *
         * @param callType type of call video or voice.
         * @return builder
         */
        @Override
        public RequiredCallType setCallType(CallType callType) {
            this.callType = callType;
            return this;
        }

        /**
         * Set the setUniqeIdCaller of call activity.
         *
         * uniqueIdCaller is email or number phone from callee.
         * @return builder
         */
        @Override
        public OptionalMethod setUniqeIdCaller(String uniqueIdCaller) {
            this.uniqueIdCaller = uniqueIdCaller;
            return this;
        }

        /**
         * Set the uniqeId of call activity. Default id "Call"
         *
         * @param uniqueIdCallee is email or number phone from caller (optional)
         * @return builder
         */
        @Override
        public RequiredUniqeIdCallee setUniqeIdCallee(String uniqueIdCallee) {
            this.uniqueIdCallee = uniqueIdCallee;
            return this;
        }

        /**
         * Set the setCallerDisplayName of call activity.
         *
         * @param callerDisplayName display avatar callee in call screen
         * @return builder
         */
        @Override
        public OptionalMethod setCallerDisplayName(String callerDisplayName) {
            this.callerDisplayName = callerDisplayName;
            return this;
        }

        /**
         * Set the setCallerAvatar of call activity.
         *
         * @param callerDisplayAvatar display avatar callee in call screen
         * @return builder
         */
        @Override
        public OptionalMethod setCallerDisplayAvatar(String callerDisplayAvatar) {
            this.callerDisplayAvatar = callerDisplayAvatar;
            return this;
        }

        /**
         * Set the setCalleeAvatar of call activity.
         *
         * @param calleeDisplayAvatar display avatar callee in call screen
         * @return builder
         */
        @Override
        public OptionalMethod setCalleeDisplayAvatar(String calleeDisplayAvatar) {
            this.calleeDisplayAvatar = calleeDisplayAvatar;
            return this;
        }

        /**
         * Set the setCalleeDisplayName of call activity.
         *
         * @param calleeDisplayName display avatar callee in call screen
         * @return builder
         */
        @Override
        public OptionalMethod setCalleeDisplayName(String calleeDisplayName) {
            this.calleeDisplayName = calleeDisplayName;
            return this;
        }

        /**
         * show the Call activity intent
         *
         * @param context  Context for start the Activity
         */
        @Override
        public QiscusRTC show(Context context) {
            QiscusRTCCall callData = new QiscusRTCCall();
            callData.setRoomId(roomCallId);
            callData.setCallAs(callAs);
            callData.setCallType(callType);
            callData.setTargetUser(uniqueIdCallee);
            callData.setCallerDisplayName(callerDisplayName);
            callData.setCallerAvatar(callerDisplayAvatar);
            callData.setCallerUsername(uniqueIdCaller);
            callData.setCalleeDisplayName(calleeDisplayName);
            callData.setCalleeAvatar(calleeDisplayAvatar);

            if (!LocalDataManager.getInstance().isContainCallSession(roomCallId)) {
                LocalDataManager.getInstance().addCallSession(roomCallId);
                Intent intent = new Intent(QiscusCallActivity.generateIntent(context, callData));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

            return this;
        }
    }

    /**
     * The first method you need to be invoke to using qiscusrtc sdk.
     * Call this method from your Application class.
     *
     * @param instance - Application instance
     */
    public static void init(Application instance) {
        appInstance = instance;
        applicationContext = appInstance.getApplicationContext();
        applicationHandler = new Handler(applicationContext.getMainLooper());
        taskExecutor = new ScheduledThreadPoolExecutor(5);
        session = new QiscusRTCSession(applicationContext);
    }

    /**
     * Needed to run something at background thread handler
     *
     * @return ScheduledExecutorService instance
     */
    public static ScheduledThreadPoolExecutor getTaskExecutor() {
        return taskExecutor;
    }

    /**
     * Use this method to check user session
     *
     * @return isRegistered
     */
    public static Boolean hasSession() {
        return session.isRegistered();
    }

    /**
     * Use this method to set user session
     */
    public static void setSession() {
        session.register(account);
    }

    /**
     * Use this method to get user name
     *
     * @return String username
     */
    public static String getUser() {
        return session.getName();
    }

    /**
     * Use this method set user. It will be registered automatically.
     *
     * @param username - String username
     * @param displayName - String display name
     * @param avatarUrl - String avatar url
     */
    public static void register(String username, String displayName, String avatarUrl) {
        account = new QiscusRTCAccount(username, avatarUrl);
        account.setAvatarUrl(avatarUrl);
        account.setDisplayName(displayName);

        JSONObject data = new JSONObject();
        JSONObject jsonObject = new JSONObject();

        try {
            data.put("username", username);
            jsonObject.put("request", "register");
            jsonObject.put("data", data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Use this method set user. It will be registered automatically.
     *
     * Call only when user already has a session.
     */
    public static void register() {
        JSONObject data = new JSONObject();
        JSONObject jsonObject = new JSONObject();

        try {
            data.put("username", session.getName());
            data.put("force", true);
            jsonObject.put("request", "register");
            jsonObject.put("data", data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Call Class
     */
    public static class Call {
        private static Call callInstance;
        private static CallConfig callConfig;

        private CallType callType = CallType.VOICE;
        private boolean accepted = false;
        private boolean delivered = false;

        /**
         * Get QiscusRTC call instance.
         *
         * @return {@link Call}
         */
        @NonNull
        public static Call getInstance() {
            if (callInstance == null) {
                callInstance = new Call();
            }

            if (callConfig == null) {
                callConfig = new CallConfig();
            }

            return callInstance;
        }

        /**
         * Get username.
         *
         * @return username
         */
        @NonNull
        public String getUsername() {
            return session.getName();
        }

        /**
         * Check username has set or not.
         *
         * @return true if username has been set
         */
        public boolean hasSetUserName() {
            return session.getName() != null;
        }

        /**
         * Set call type, default call type is VOICE.
         *
         * @param callType - {@link CallType} VIDEO or VOICE
         */
        public void setCallType(@NonNull CallType callType) {
            this.callType = callType;
        }

        /**
         * Set call type, default call type is VOICE.
         *
         * @param accepted - boolean true / file
         */
        public void setCallAccepted(boolean accepted) {
            this.accepted = accepted;
        }

        /**
         * Get call type.
         *
         * @return boolean true / false
         */
        @NonNull
        public boolean getCallAccepted() {
            return accepted;
        }

        /**
         * Set call type, default call type is VOICE.
         *
         * @param delivered - boolean true / file
         */
        public void setCallDelivered(boolean delivered) {
            this.delivered = delivered;
        }

        /**
         * Get call type.
         *
         * @return boolean true / false
         */
        @NonNull
        public boolean getCallDelivered() {
            return delivered;
        }

        /**
         * Get call type.
         *
         * @return {@link CallType} VIDEO or VOICE
         */
        @NonNull
        public CallType getCallType() {
            return callType;
        }

        /**
         * Get call configuration.
         *
         * @return {@link CallConfig}
         */
        @NonNull
        public static CallConfig getCallConfig() {
            if (callConfig == null) {
                callConfig = new CallConfig();
            }

            return callConfig;
        }
    }

    /**
     * CallType Enum
     */
    public enum CallType {
        VOICE,
        VIDEO
    }

    /**
     * CallAs Enum
     */
    public enum CallAs {
        CALLER,
        CALLEE
    }

    public enum CallEvent {
        REJECT,
        CANCEL,
        END,
        PN_RECEIVED,
        INCOMING
    }

    public static class CallEventData {
        private CallEvent callEvent;
        private String avatarUrl;
        private String calleeDisplayName;

        public void setCalleeDisplayName(String calleeDisplayName) {
            this.calleeDisplayName = calleeDisplayName;
        }

        public String getCalleeDisplayName() {
            return calleeDisplayName;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setCallEvent(CallEvent callEvent) {
            this.callEvent = callEvent;
        }

        public CallEvent getCallEvent() {
            return callEvent;
        }
    }
}
