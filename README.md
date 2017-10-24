# Qiscus RTC SDK Android

<p align="center"><br/><img src="https://github.com/qiscus/qiscus-rtc-sdk-android/blob/master/screenshoot/calling.png" width="37%" /><br/></p>

Qiscus RTC SDK is a product that makes adding voice calling to mobile apps easy. It handles all the complexity of signaling and audio management while providing you the freedom to create a stunning user interface.
On this example we use our simple websocket push notification for handle call notification. We highly recommend that you implement a better push notification for increasing call realiability, for example GCM, FCM, MQTT, or other standard messaging protocol.

# Quick Start

Below is a step-by-step guide on setting up the Qiscus RTC SDK for the first time

### Dependency

Add to your project build.gradle

```groovy
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```

```groovy
dependencies {
  compile 'com.github.qiscus:qiscus-rtc-sdk-android:0.2'
}
```

### Permission

Add to your project AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

# Authentication

### Init Qiscus

Init Qiscus at your application

Parameters:
* context: context

```java
public class SampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        QiscusRTC.init(this);
    }
}
```

### Register User

Before user can start call each other, they must register the user to our server

Parameters:
* username: String
* displayName: String
* avatar: String

```java
QiscusRTC.register(txtUsername.getText().toString(), txtUsername.getText().toString(), "http://dk6kcyuwrpkrj.cloudfront.net/wp-content/uploads/sites/45/2014/05/avatar-blank.jpg");
QiscusRTC.setSession();
```

Start call object:
* roomId: String
* callAs: Enum QiscusRTC.CallAs.CALLER / QiscusRTC.CallAs.CALLEE
* callType: Enum QiscusRTC.CallType.VOICE / QiscusRTC.CallType.VIDEO
* callerUsername: String
* calleeUsername: String
* callerDisplayName: String
* calleeDisplayName: String

### Start Call

####Start voice call

```java
QiscusRTC.CallActivityBuilder.buildCallWith(etRoomId.getText().toString())
                            .setCallAs(QiscusRTC.CallAs.CALLER)
                            .setCallType(QiscusRTC.CallType.VOICE)
                            .setCallerUsername(QiscusRTC.getUser())
                            .setCalleeUsername(etTargetUsername.getText().toString())
                            .setCalleeDisplayName(etTargetUsername.getText().toString())
                            .setCalleeDisplayAvatar("http://dk6kcyuwrpkrj.cloudfront.net/wp-content/uploads/sites/45/2014/05/avatar-blank.jpg")
                            .show(this);
```
####Start video call

```java
QiscusRTC.CallActivityBuilder.buildCallWith(etRoomId.getText().toString())
                            .setCallAs(QiscusRTC.CallAs.CALLER)
                            .setCallType(QiscusRTC.CallType.VIDEO)
                            .setCallerUsername(QiscusRTC.getUser())
                            .setCalleeUsername(etTargetUsername.getText().toString())
                            .setCalleeDisplayName(etTargetUsername.getText().toString())
                            .setCalleeDisplayAvatar("http://dk6kcyuwrpkrj.cloudfront.net/wp-content/uploads/sites/45/2014/05/avatar-blank.jpg")
                            .show(this);
```

### Custom your call

You can custom your call notification, icon and callback button action with ```QiscusRTC.Call.getCallConfig()```

```java
QiscusRTC.Call.getCallConfig()
                .setBackgroundDrawble(R.drawable.bg_call)
                .setOngoingNotificationEnable(true)
                .setLargeOngoingNotifIcon(R.drawable.ic_call_white_24dp);
```

That's it! You just need 3 steps to build voice call in your apps.

### Example

- [Basic example](https://github.com/qiscus/qiscus-rtc-sdk-android/blob/master/app/src/main/java/com/qiscus/rtc/sample/MainActivity.java)
