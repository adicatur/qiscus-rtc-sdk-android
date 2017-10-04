package com.qiscus.rtc.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.qiscus.rtc.QiscusRTC;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private EditText etTargetUsername;
    private EditText etRoomId;
    private Button btnStartCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        if (!QiscusRTC.hasSession()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initView() {
        etTargetUsername = (EditText) findViewById(R.id.target_username);
        etRoomId = (EditText) findViewById(R.id.room_id);
        btnStartCall = (Button) findViewById(R.id.btn_call);
        btnStartCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etTargetUsername.getText().toString().isEmpty() && !etRoomId.getText().toString().isEmpty()) {
                    QiscusRTC.CallActivityBuilder.buildCallWith(etRoomId.getText().toString())
                            .setCallAs(QiscusRTC.CallAs.CALLER)
                            .setCallType(QiscusRTC.CallType.VOICE)
                            .setUniqeIdCallee(etTargetUsername.getText().toString())
                            .setUniqeIdCaller(QiscusRTC.getUser())
                            .setCalleeDisplayName(etTargetUsername.getText().toString())
                            .setCalleeDisplayAvatar("http://dk6kcyuwrpkrj.cloudfront.net/wp-content/uploads/sites/45/2014/05/avatar-blank.jpg")
                            .show(MainActivity.this);
                } else {
                    Toast.makeText(MainActivity.this, "Target user and room id required", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
