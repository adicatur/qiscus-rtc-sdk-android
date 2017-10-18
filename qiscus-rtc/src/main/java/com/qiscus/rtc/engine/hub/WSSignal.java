package com.qiscus.rtc.engine.hub;

import android.util.Log;

import com.qiscus.rtc.engine.util.LooperExecutor;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

/**
 * Created by fitra on 2/10/17.
 */

public class WSSignal implements HubSignal, WSChannel.WSChannelEvents {
    private static final String TAG = WSSignal.class.getSimpleName();

    private final LooperExecutor executor;
    private final SignalParameters parameters;

    private Thread heartbeat;
    private SignalEvents events;
    private WSChannel channel;

    public WSSignal(SignalEvents events, SignalParameters parameters, LooperExecutor executor) {
        this.events = events;
        this.parameters = parameters;
        this.executor = executor;
        executor.requestStart();
        channel = new WSChannel(executor, this);
    }

    @Override
    public void connect() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                channel.connect();
            }
        });
    }

    @Override
    public void acceptCall() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                channel.acceptCall();
            }
        });
    }

    @Override
    public void rejectCall() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                channel.rejectCall();
            }
        });
    }

    @Override
    public void onWebsocketOpen() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                channel.state = WSChannel.WSState.CONNECTED;
                channel.register(parameters.clientId);
            }
        });
    }

    @Override
    public void sendOffer(final SessionDescription sdp) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                channel.sendOffer(sdp);
            }
        });
    }

    @Override
    public void sendAnswer(final SessionDescription sdp) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                channel.sendAnswer(sdp);
            }
        });
    }

    @Override
    public void trickleCandidate(final IceCandidate candidate) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                channel.sendTrickle(candidate);
            }
        });
    }

    @Override
    public void ping() {
        heartbeat = new Thread(new Runnable() {
            @Override
            public void run() {
                while (channel.state == WSChannel.WSState.CONNECTED || channel.state == WSChannel.WSState.LOGGEDIN) {
                    try {
                        channel.ping();
                        Thread.sleep(30 * 1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        heartbeat.start();
    }

    @Override
    public void close() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                channel.close();
            }
        });

        executor.requestStop();

        if (heartbeat.isAlive()) {
            heartbeat.interrupt();
        }
    }

    @Override
    public void onWebsocketMessage(String msg) {
        try {
            JSONObject object = new JSONObject(msg);

            if (object.has("response")) {
                String response = object.getString("response");
                String strData = object.getString("data");
                JSONObject data = new JSONObject(strData);

                if (response.equals("register")) {
                    boolean success = data.getBoolean("success");

                    if (success) {
                        if (parameters.initiator) {
                            channel.createRoom(parameters.roomId);
                        } else {
                            channel.joinRoom(parameters.roomId);
                        }
                    } else {
                        String message = data.getString("message");
                        Log.e(TAG, message);

                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                channel.close();
                            }
                        });
                    }
                } else if (response.equals("room_create") || response.equals("room_join")) {
                    boolean success = data.getBoolean("success");

                    if (success) {
                        channel.state = WSChannel.WSState.LOGGEDIN;
                        events.onLoggedinToRoom();
                    } else {
                        String message = data.getString("message");
                        Log.e(TAG, message);

                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                channel.close();
                            }
                        });
                    }
                }
            } else if (object.has("event")) {
                String event = object.getString("event");
                String sender = object.getString("sender");
                String strData = object.getString("data");
                JSONObject data = new JSONObject(strData);

                if (event.equals("user_new")) {
                    if (sender.equals(parameters.target)) {
                        channel.sync(parameters.target);
                    }
                } else if (event.equals("user_leave")) {
                    if (sender.equals(parameters.target)) {
                        close();
                    }
                } else if (event.equals("room_data_private")) {
                    if (data.has("event")) {
                        String evt = data.getString("event");

                        if (evt.equals("call_sync")) {
                            channel.ack(sender);
                        } else if (evt.equals("call_ack")) {
                            events.onPNReceived();
                        } else if (evt.equals("call_accept")) {
                            events.onCallAccepted();
                        } else if (evt.equals("call_reject")) {
                            events.onCallRejected();
                        } else if (evt.equals("call_cancel")) {
                            events.onCallCanceled();
                        }
                    } else if (data.has("type")) {
                        String type = data.getString("type");

                        if (type.equals("offer")) {
                            String description = data.getString("sdp");
                            SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("offer"), description);
                            events.onSDPOffer(sdp);
                        } else if (type.equals("answer")) {
                            String description = data.getString("sdp");
                            SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("answer"), description);
                            events.onSDPAnswer(sdp);
                        } else if (type.equals("candidate")) {
                            IceCandidate candidate = new IceCandidate(data.getString("sdpMid"), data.getInt("sdpMLineIndex"), data.getString("candidate"));
                            events.onICECandidate(candidate);
                        }
                    }
                } else {
                    Log.e(TAG, "Unknown event.");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebsocketClose() {
        channel.state = WSChannel.WSState.CLOSED;
        events.onClose();
    }

    @Override
    public void onWebsocketError(String description) {
        //
    }
}

