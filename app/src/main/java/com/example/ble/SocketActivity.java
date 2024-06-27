package com.example.ble;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class SocketActivity extends AppCompatActivity {
    String a;
    int b;
    connectthread lianjie;
    TextView receive1;
    TextView receive2;
    Socket socket=null;
    Button connect;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        EditText ip=findViewById(R.id.ip);
        EditText port=findViewById(R.id.port);
        EditText out=findViewById(R.id.out);
        receive1=findViewById(R.id.receive1);
        receive2=findViewById(R.id.receive2);
        connect=findViewById(R.id.connect);
        Button send=findViewById(R.id.send);


        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                a=ip.getText().toString();
                String c=port.getText().toString();
                if("".equals(a)||"".equals(c)){
                    Toast.makeText(SocketActivity.this,"请输入ip和端口号",Toast.LENGTH_SHORT).show();
                }
                else{b=Integer.valueOf(c);
                    lianjie=new connectthread();
                    lianjie.start();}

            }
        });



        //发送
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //子线程中进行网络操作
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(socket!=null){
                            try {
                                String text=out.getText().toString();
                                lianjie.outputStream.write(text.getBytes());
                            } catch (UnknownHostException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();

                            }catch (IOException e) {
                                e.printStackTrace();
                            }}else{
                            runOnUiThread(new Runnable()//不允许其他线程直接操作组件，用提供的此方法可以
                            {
                                public void run()
                                {
                                    // TODO Auto-generated method stub
                                    Toast.makeText(SocketActivity.this,"请先建立连接",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();

            }
        });






    }
    //子线程中进行网络相关操作
    class connectthread extends Thread {

        OutputStream outputStream=null;
        InputStream inputStream=null;
        @Override
        public void run() {

            //连接
            try {
                socket=new Socket(a, b);
                runOnUiThread(new Runnable()//不允许其他线程直接操作组件，用提供的此方法可以
                {
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        Toast.makeText(SocketActivity.this,"连接成功",Toast.LENGTH_SHORT).show();

                    }
                });
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                runOnUiThread(new Runnable()//不允许其他线程直接操作组件，用提供的此方法可以
                {
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        Toast.makeText(SocketActivity.this,"连接失败",Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable()//不允许其他线程直接操作组件，用提供的此方法可以
                {
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        Toast.makeText(SocketActivity.this,"连接失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
            if(socket!=null){
                //获取输出流对象
                try {
                    outputStream=socket.getOutputStream();
                    outputStream.write(123);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try{
                    while (true)
                    {
                        final byte[] buffer = new byte[1024];//创建接收缓冲区
                        inputStream = socket.getInputStream();
                        final int len = inputStream.read(buffer);//数据读出来，并且返回数据的长度
                        runOnUiThread(new Runnable()//不允许其他线程直接操作组件，用提供的此方法可以
                        {
                            @SuppressLint("SetTextI18n")
                            public void run()
                            {
                                // TODO Auto-generated method stub

                                //{"1on":106.6085886367,"1at":26.3835885117,"rtksta":2,"speed":0.107416,"HCSDS":17,"a1ti":1189.69}
                                //{"S1":0,"S2":0,"S3":0,"S4":0,"r1":0,"r2":0,"r3":0,"r4":0,"r5":0,"r6":0,"r7":0,"r8":0}
//                                receive.append(new String(buffer,0,len)+"\r\n");
//                                receive2.setText(new String(buffer,0,len));
//                                Toast.makeText(SocketActivity.this,new String(buffer,StandardCharsets.UTF_8),Toast.LENGTH_SHORT).show();


                                // 将字节数组转换为字符串
                                String jsonString = new String(buffer, StandardCharsets.UTF_8);
                                try {
                                    // 解析 JSON 字符串
                                    JSONObject jsonObject = new JSONObject(jsonString);

                                    if (jsonObject.has("lon"))
                                    {
                                        // 获取JSON中的字段
                                        String lon = jsonObject.getString("lon");
                                        String lat = jsonObject.getString("lat");

                                        // 使用解析出来的数据
                                        receive1.setText(lon + "   " + lat);
                                    }
                                    else if (jsonObject.has("S1"))
                                    {
                                        // 获取JSON中的字段
                                        String S1 = jsonObject.getString("S1");
                                        String S2 = jsonObject.getString("S2");

                                        // 使用解析出来的数据
                                        receive2.setText(S1 + "   " + S2);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
                catch (IOException e) {

                }}
        };
    }}



