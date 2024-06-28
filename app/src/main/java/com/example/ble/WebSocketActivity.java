package com.example.ble;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketActivity extends AppCompatActivity {

    private static final String TAG = "WebSocketActivity";
    private WebSocket webSocket;
    private TextView RxtextView;
    Button SendDataButton;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_socket);

        RxtextView = findViewById(R.id.RxtextView);
        SendDataButton = (Button)findViewById(R.id.SendDataButton);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("ws://8.137.81.229:8880") // 使用提供的 WebSocket URL
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d(TAG, "WebSocket 连接已打开");
//                webSocket.send("{\"giuty\":34}");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "收到消息: " + text);
                mainHandler.post(() -> RxtextView.setText("Rx: " + text));
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                Log.d(TAG, "收到二进制消息: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket 正在关闭: " + code + " / " + reason);
                webSocket.close(code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket 已关闭: " + code + " / " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e(TAG, "WebSocket 错误: " + t.getMessage(), t);
                if (response != null) {
                    Log.e(TAG, "WebSocket 错误响应: " + response);
                }
            }
        });

        SendDataButton.setOnClickListener(new View.OnClickListener() {
            private int counter = 0;

            @Override
            public void onClick(View v) {
                // Increment the counter
                counter++;

                // Create a JSON object with the incremented counter value
                String json = "{\"giuty\":" + counter + "}";

                // Send the JSON string to the WebSocket server
                if (webSocket != null) {
                    webSocket.send(json);
                }
            }
        });


        // 关闭客户端将在onDestroy中处理
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Activity destroyed");
        }
    }
}
