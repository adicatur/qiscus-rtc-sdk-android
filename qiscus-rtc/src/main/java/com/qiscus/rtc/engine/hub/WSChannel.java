package com.qiscus.rtc.engine.hub;

import android.util.Log;

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.WritableCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.qiscus.rtc.engine.util.LooperExecutor;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by fitra on 2/10/17.
 */

public class WSChannel {
    private static final String TAG = WSChannel.class.getSimpleName();
    private static String SERVER = "wss://rtc.qiscus.com/signal";

    private final LooperExecutor executor;
    private final WSChannelEvents event;

    private WebSocket websocket;
    private String room_id;
    private String client_id;
    private String target_id;

    public enum WSState {
        NEW, CONNECTED, LOGGEDIN, CLOSED, ERROR
    }

    public WSState state;

    public interface WSChannelEvents {
        public void onWebsocketOpen();
        public void onWebsocketMessage(final String message);
        public void onWebsocketClose();
        public void onWebsocketError(final String description);
    }

    public WSChannel(LooperExecutor executor, WSChannelEvents event) {
        this.executor = executor;
        this.event = event;
        state = WSState.NEW;
    }

    public static String saveChannelUrl(String url) {
        SERVER = url;
        return SERVER;
    }

    public void connect() {
        TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        SSLContext ctx = null;

        try {
            ctx = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        try {
            ctx.init(null, new TrustManager[] { tm }, null);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
        AsyncHttpClient.getDefaultInstance().getSSLSocketMiddleware().setSSLContext(ctx);
        AsyncHttpClient.getDefaultInstance().getSSLSocketMiddleware().setTrustManagers(new TrustManager[] { tm });
        AsyncHttpClient.getDefaultInstance().websocket(SERVER, "", new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception e, WebSocket ws) {
                if (e != null) {
                    Log.d("Debugging", e.toString());
                }

                websocket = ws;
                websocket.setWriteableCallback(new WritableCallback() {
                    @Override
                    public void onWriteable() {
                        Log.d("Debugging", "Writable");
                    }
                });
                websocket.setPongCallback(new WebSocket.PongCallback() {
                    @Override
                    public void onPongReceived(String data) {
                        Log.d("Debugging", "Pong");
                    }
                });
                websocket.setDataCallback(new DataCallback() {
                    @Override
                    public void onDataAvailable(DataEmitter emitter, ByteBufferList buffer) {
                        Log.d("Debugging", "Data");
                    }
                });
                websocket.setEndCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        Log.d("Debugging", "Complete");
                    }
                });
                websocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(final String message) {
                        Log.d(TAG, "WSS->C: " + message);

                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                if (state == WSState.CONNECTED || state == WSState.LOGGEDIN) {
                                    event.onWebsocketMessage(message);
                                }
                            }
                        });
                    }
                });
                websocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception e) {
                        if (e != null) {
                            //
                        } else {
                            Log.d(TAG, "Websocket connection closed. State: " + state);

                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    if (state != WSState.CLOSED) {
                                        state = WSState.CLOSED;
                                        event.onWebsocketClose();
                                    }
                                }
                            });
                        }
                    }
                });

                event.onWebsocketOpen();
            }
        });
    }

    public void register(String clientId) {
        if (state != WSState.CONNECTED) {
            Log.e(TAG, "Hub register in state " + state);
            return;
        }

        this.client_id = clientId;

        try {
            JSONObject object = new JSONObject();
            JSONObject data = new JSONObject();
            object.put("request", "register");
            data.put("username", client_id);
            object.put("data", data.toString());

            Log.d(TAG, "C->WSS: " + object.toString());

            websocket.send(object.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Hub register error: " + e.getMessage());
        }
    }

    public void createRoom(String roomId) {
        if (state != WSState.CONNECTED) {
            Log.e(TAG, "Hub create room in state " + state);
            return;
        }

        this.room_id = roomId;

        try {
            JSONObject object = new JSONObject();
            JSONObject data = new JSONObject();
            object.put("request", "room_create");
            object.put("room", room_id);
            data.put("max_participant", 2);
            object.put("data", data.toString());

            Log.d(TAG, "C->WSS: " + object.toString());

            websocket.send(object.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Hub create room error: " + e.getMessage());
        }
    }

    public void joinRoom(String roomId) {
        if (state != WSState.CONNECTED) {
            Log.e(TAG, "Hub join room in state " + state);
            return;
        }

        this.room_id = roomId;

        try {
            JSONObject object = new JSONObject();
            object.put("request", "room_join");
            object.put("room", room_id);

            Log.d(TAG, "C->WSS: " + object.toString());

            websocket.send(object.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Hub join room error: " + e.getMessage());
        }
    }

    public void sync(String target) {
        if (state != WSState.LOGGEDIN) {
            Log.e(TAG, "Hub sync call in state " + state);
            return;
        }

        this.target_id = target;

        try {
            JSONObject object = new JSONObject();
            JSONObject data = new JSONObject();
            object.put("request", "room_data");
            object.put("room", room_id);
            object.put("recipient", target_id);
            data.put("event", "call_sync");
            object.put("data", data.toString());

            Log.d(TAG, "C->WSS: " + object.toString());

            websocket.send(object.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Hub sync call error: " + e.getMessage());
        }
    }

    public void ack(String target) {
        if (state != WSState.LOGGEDIN) {
            Log.e(TAG, "Hub ack call in state " + state);
            return;
        }

        this.target_id = target;

        try {
            JSONObject object = new JSONObject();
            JSONObject data = new JSONObject();
            object.put("request", "room_data");
            object.put("room", room_id);
            object.put("recipient", target_id);
            data.put("event", "call_ack");
            object.put("data", data.toString());

            Log.d(TAG, "C->WSS: " + object.toString());

            websocket.send(object.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Hub ack call error: " + e.getMessage());
        }
    }

    public void acceptCall() {
        if (state != WSState.LOGGEDIN) {
            Log.e(TAG, "Hub accept call in state " + state);
            return;
        }

        try {
            JSONObject object = new JSONObject();
            JSONObject data = new JSONObject();
            object.put("request", "room_data");
            object.put("room", room_id);
            object.put("recipient", target_id);
            data.put("event", "call_accept");
            object.put("data", data.toString());

            Log.d(TAG, "C->WSS: " + object.toString());

            websocket.send(object.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Hub accept call error: " + e.getMessage());
        }
    }

    public void rejectCall() {
        if (state != WSState.LOGGEDIN) {
            Log.e(TAG, "Hub reject call in state " + state);
            return;
        }

        try {
            JSONObject object = new JSONObject();
            JSONObject data = new JSONObject();
            object.put("request", "room_data");
            object.put("room", room_id);
            object.put("recipient", target_id);
            data.put("event", "call_reject");
            object.put("data", data.toString());

            Log.d(TAG, "C->WSS: " + object.toString());

            websocket.send(object.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Hub reject call error: " + e.getMessage());
        }
    }

    public void ping() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (state != WSState.LOGGEDIN) {
                    Log.e(TAG, "Hub ping in state " + state);
                    return;
                }

                try {
                    JSONObject object = new JSONObject();
                    object.put("request", "ping");
                    object.put("clientId", client_id);

                    Log.d(TAG, "C->WSS: " + object.toString());

                    websocket.send(object.toString());
                } catch (JSONException e) {
                    Log.e(TAG, "Hub ping error: " + e.getMessage());
                }
            }
        });
    }

    public void sendOffer(SessionDescription sdp) {
        if (state != WSState.LOGGEDIN) {
            Log.e(TAG, "Hub send offer in state " + state);
            return;
        }

        try {
            JSONObject object = new JSONObject();
            JSONObject data = new JSONObject();
            object.put("request", "room_data");
            object.put("room", room_id);
            object.put("recipient", target_id);
            data.put("type", "offer");
            data.put("sdp", sdp.description);
            object.put("data", data.toString());

            Log.d(TAG, "C->WSS: " + object.toString());

            websocket.send(object.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Hub send offer error: " + e.getMessage());
        }
    }

    public void sendAnswer(SessionDescription sdp) {
        if (state != WSState.LOGGEDIN) {
            Log.e(TAG, "Hub send answer in state " + state);
            return;
        }

        try {
            JSONObject object = new JSONObject();
            JSONObject data = new JSONObject();
            object.put("request", "room_data");
            object.put("room", room_id);
            object.put("recipient", target_id);
            data.put("type", "answer");
            data.put("sdp", sdp.description);
            object.put("data", data.toString());

            Log.d(TAG, "C->WSS: " + object.toString());

            websocket.send(object.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Hub send answer error: " + e.getMessage());
        }
    }

    public void sendTrickle(IceCandidate cnd) {
        if (state != WSState.LOGGEDIN) {
            Log.w(TAG, "Hub send trickle in state " + state);
            return;
        }

        try {
            JSONObject object = new JSONObject();
            JSONObject data = new JSONObject();
            object.put("request", "room_data");
            object.put("room", room_id);
            object.put("recipient", target_id);
            data.put("type", "candidate");
            data.put("sdpMid", cnd.sdpMid);
            data.put("sdpMLineIndex", cnd.sdpMLineIndex);
            data.put("candidate", cnd.sdp);
            object.put("data", data.toString());

            Log.d(TAG, "C->WSS: " + object.toString());

            websocket.send(object.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Hub send trickle error: " + e.getMessage());
        }
    }

    public void close() {
        state = WSState.CLOSED;

        if (websocket != null) {
            websocket.close();
        }
    }
}

