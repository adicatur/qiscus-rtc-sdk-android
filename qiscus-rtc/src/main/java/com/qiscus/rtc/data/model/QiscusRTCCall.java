package com.qiscus.rtc.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.qiscus.rtc.QiscusRTC;

/**
 * Created by fitra on 2/10/17.
 */

public class QiscusRTCCall implements Parcelable {
    private QiscusRTC.CallAs callAs = QiscusRTC.CallAs.CALLER;
    private QiscusRTC.CallType callType = QiscusRTC.CallType.VIDEO;
    private String roomId;
    private String callerUsername;
    private String callerDisplayName;
    private String callerAvatar;
    private String calleeUsername;
    private String calleeDisplayName;
    private String calleeAvatar;

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setCallerUsername(String callerUsername) {
        this.callerUsername = callerUsername;
    }

    public String getCallerUsername() {
        return callerUsername;
    }

    public void setCallerDisplayName(String callerDisplayName) {
        this.callerDisplayName = callerDisplayName;
    }

    public String getCallerDisplayName() {
        return callerDisplayName;
    }

    public void setCallerAvatar(String callerAvatar) {
        this.callerAvatar = callerAvatar;
    }

    public String getCallerAvatar() {
        return callerAvatar;
    }

    public void setCalleeUsername(String calleeUsername) {
        this.calleeUsername = calleeUsername;
    }

    public String getCalleeUsername() {
        return calleeUsername;
    }

    public void setCalleeDisplayName(String calleeDisplayName) {
        this.calleeDisplayName = calleeDisplayName;
    }

    public String getCalleeDisplayName() {
        return calleeDisplayName;
    }

    public void setCalleeAvatar(String calleeAvatar) {
        this.calleeAvatar = calleeAvatar;
    }

    public String getCalleeAvatar() {
        return calleeAvatar;
    }

    public void setCallAs(QiscusRTC.CallAs callAs) {
        this.callAs = callAs;
    }

    public QiscusRTC.CallAs getCallAs() {
        return callAs;
    }

    public void setCallType(QiscusRTC.CallType callType) {
        this.callType = callType;
    }

    public QiscusRTC.CallType getCallType() {
        return callType;
    }

    public QiscusRTCCall() {
        //
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.callAs == null ? -1 : this.callAs.ordinal());
        dest.writeInt(this.callType == null ? -1 : this.callType.ordinal());
        dest.writeString(this.roomId);
        dest.writeString(this.callerUsername);
        dest.writeString(this.callerDisplayName);
        dest.writeString(this.callerAvatar);
        dest.writeString(this.calleeUsername);
        dest.writeString(this.calleeDisplayName);
        dest.writeString(this.calleeAvatar);
    }

    protected QiscusRTCCall(Parcel in) {
        int tmpCallAs = in.readInt();
        this.callAs = tmpCallAs == -1 ? null : QiscusRTC.CallAs.values()[tmpCallAs];
        int tmpCallType = in.readInt();
        this.callType = tmpCallType == -1 ? null : QiscusRTC.CallType.values()[tmpCallType];
        this.roomId = in.readString();
        this.callerUsername = in.readString();
        this.callerDisplayName = in.readString();
        this.callerAvatar = in.readString();
        this.calleeUsername = in.readString();
        this.calleeDisplayName = in.readString();
        this.calleeAvatar = in.readString();
    }

    public static final Creator<QiscusRTCCall> CREATOR = new Creator<QiscusRTCCall>() {
        @Override
        public QiscusRTCCall createFromParcel(Parcel source) {
            return new QiscusRTCCall(source);
        }

        @Override
        public QiscusRTCCall[] newArray(int size) {
            return new QiscusRTCCall[size];
        }
    };

    @Override
    public String toString() {
        return "CallData{" +
                "callAs=" + callAs +
                ", callType=" + callType +
                ", roomId='" + roomId + '\'' +
                ", callerDisplayName='" + callerDisplayName + '\'' +
                ", callerAvatar='" + callerAvatar + '\'' +
                ", calleeUsername='" + calleeUsername + '\'' +
                ", callerUsername='" + callerUsername + '\'' +
                ", calleeDisplayName='" + calleeDisplayName + '\'' +
                ", calleeAvatar='" + calleeAvatar + '\'' +
                '}';
    }
}
