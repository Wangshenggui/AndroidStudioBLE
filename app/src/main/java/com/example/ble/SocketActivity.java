package com.example.ble;

import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class SocketActivity extends AppCompatActivity {

    EditText IPEdit;//ip编辑框对象
    EditText portEdit;//端口编辑框对象
    EditText dataEdit;//数据编辑框对象
    Button ConnectButton;//连接服务器按钮对象
    EditText editText;//接收的数据显示编辑框对象
    TextView testTextView;//JSON解析测试


    Socket Socket = null;//Socket
    boolean buttontitle = true;//定义一个逻辑变量，用于判断连接服务器按钮状态
    boolean RD = false;//用于控制读数据线程是否执行

    OutputStream OutputStream = null;//定义数据输出流，用于发送数据
    InputStream InputStream = null;//定义数据输入流，用于接收数据


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);

        IPEdit = (EditText) findViewById(R.id.IPEdit);//获取ip地址编辑框对象
        portEdit = (EditText) findViewById(R.id.portEdit);//获取端口编辑框对象
        dataEdit = (EditText) findViewById(R.id.dataEdit);//获取欲发送的数据编辑框对象
        ConnectButton = (Button) findViewById(R.id.ConnectButton);//获取连接服务器按钮对象
        editText = (EditText) findViewById(R.id.editText);//获取接收数据显示编辑框对象
        testTextView = (TextView) findViewById(R.id.testTextView);

        //自动连接
        link(new View(this));
    }

    //连接服务器按钮按下
    public void link(View view) {
        //判断按钮状态
        if (buttontitle == true) {
            //如果按钮没有被按下，则按钮状态改为按下
            buttontitle = false;
            //读数据线程可以执行
            RD = true;
            //并创建一个新的线程，用于初始化socket
            Connect_Thread Connect_thread = new Connect_Thread();
            Connect_thread.start();
            //改变按钮标题
            ConnectButton.setText("断 开 连 接");
        } else {
            //如果按钮已经被按下，则改变按钮标题
            ConnectButton.setText("连 接 服 务 器");
            //储存状态的变量反转
            buttontitle = true;
            try {
                //取消socket
                Socket.close();
                //socket设置为空
                Socket = null;
                //读数据线程不执行
                RD = false;
            } catch (Exception e) {
                //如果想写的严谨一点，可以自行改动
                e.printStackTrace();
            }
        }
    }

    //用线程创建Socket连接
    class Connect_Thread extends Thread {
        public void run() {
            //定义一个变量用于储存ip
            InetAddress ipAddress;
            try {
                //判断socket的状态，防止重复执行
                if (Socket == null) {
                    //如果socket为空则执行
                    //获取输入的IP地址
                    ipAddress = InetAddress.getByName(IPEdit.getText().toString());
                    //获取输入的端口
                    int port = Integer.valueOf(portEdit.getText().toString());
                    //新建一个socket
                    Socket = new Socket(ipAddress, port);
                    //获取socket的输入流和输出流
                    InputStream = Socket.getInputStream();
                    OutputStream = Socket.getOutputStream();

                    //新建一个线程读数据
                    ThreadReadData t1 = new ThreadReadData();
                    t1.start();
                }
            } catch (Exception e) {
                //如果有错误则在这里返回
                e.printStackTrace();
            }
        }
    }

    //用线程执行读取服务器发来的数据
    class ThreadReadData extends Thread {
        public void run() {
            //定义一个变量用于储存服务器发来的数据
            String textdata;
            //根据RD变量的值判断是否执行读数据
            while (RD) {
                try {
                    //定义一个字节集，存放输入的数据，缓存区大小为2048字节
                    final byte[] ReadBuffer = new byte[2048];
                    //用于存放数据量
                    final int ReadBufferLengh;

                    //从输入流获取服务器发来的数据和数据宽度
                    //ReadBuffer为参考变量，在这里会改变为数据
                    //输入流的返回值是服务器发来的数据宽度
                    ReadBufferLengh = InputStream.read(ReadBuffer);

                    //验证数据宽度，如果为-1则已经断开了连接
                    if (ReadBufferLengh == -1) {
                        //重新归位到初始状态
                        RD = false;
                        Socket.close();
                        Socket = null;
                        buttontitle = true;
                        ConnectButton.setText("连 接 服 务 器");
                    } else {
                        //先恢复成GB2312编码
                        textdata = new String(ReadBuffer, 0, ReadBufferLengh, "GB2312");//原始编码数据
                        //转为UTF-8编码后显示在编辑框中
                        editText.setText(new String(textdata.getBytes(), "UTF-8"));

                        try {
                            JSONObject jsonObject = new JSONObject(textdata);

                            String lng = jsonObject.getString("lng");

                            testTextView.setText("你干嘛" + lng);
                        } catch (JSONException e) {
                            // 处理解析错误
                            e.printStackTrace();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //信息框按钮按下事件
    public DialogInterface.OnClickListener click1 = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    };
}