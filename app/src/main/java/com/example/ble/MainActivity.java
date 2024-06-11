package com.example.ble;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.OutputStream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static int counter = 0;
    private static String RxBuff = "";
    private static byte[] RxData;
    private static int totalBytesReceived = 0;
    static int count=0;



    //跳转蓝牙按钮
    Button goBluetoothButton=null;
    //发送数据按钮
    Button TxButt=null;
    private static TextView ReceiveTextView=null;
    private static TextView TimeViewText=null;
    private static TextView LongitudeViewText=null;
    private static TextView LatitudeViewText=null;
    private static TextView CourseAngleViewText=null;
    private static TextView SpeedViewText=null;
    private static TextView Pin_TranslateViewText=null;
    private static TextView HCSDSViewText=null;
    private static TextView HDOPViewText=null;
    private static TextView AltitudeViewText=null;
    private static TextView PositioningModeViewText=null;







    Intent intent=null;
    //滚轮数据
    public static int[] wheelData=new int[]{0,0};
    public static OutputStream outputStream=null;//获取输出数据
    int num=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        //获取控件ID
        ReceiveTextView=findViewById(R.id.ReceiveTextView);
        TimeViewText=findViewById(R.id.TimeViewText);
        LongitudeViewText=findViewById(R.id.LongitudeViewText);
        LatitudeViewText=findViewById(R.id.LatitudeViewText);
        CourseAngleViewText=findViewById(R.id.CourseAngleViewText);
        SpeedViewText=findViewById(R.id.SpeedViewText);
        Pin_TranslateViewText=findViewById(R.id.Pin_TranslateViewText);
        HCSDSViewText=findViewById(R.id.HCSDSViewText);
        HDOPViewText=findViewById(R.id.HDOPViewText);
        AltitudeViewText=findViewById(R.id.AltitudeViewText);
        PositioningModeViewText=findViewById(R.id.PositioningModeViewText);



        //获取按钮ID
        goBluetoothButton=(Button) findViewById(R.id.goBluetoothButton);
        goBluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent=new Intent(MainActivity.this,BluetoothActivity.class);
                startActivity(intent);
            }
        });

        //获取按钮ID
        TxButt=(Button) findViewById(R.id.TxButt);
        TxButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num+=2;
                wheelData[0] = num;
                wheelData[1] = num+1;

                byte[] data = {65, 66, 67, 68, 69}; // 字节数组
                try {
                    outputStream.write(data);
                } catch (IOException e) {
                    e.printStackTrace(); // 这里可以处理异常，比如打印异常信息或者做其他适当的处理
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //软件退出后清空，断开蓝牙操作
        BluetoothActivity.connectThread.cancel();
        BluetoothActivity.connectedThread.cancel();
    }

    /*
    AB 10 13 1D 41 2B 87 16 D9 CE F7 B3 3F 00 00 00 E0 E9 CF 70 40 18 06 05 44 47 65 F5 54 3D C6 C4 40 84 B8 B4 12 13 7E A4 40 00 00 00 00 00 80 87 C3 40 30 4C A6 0A 06 93 93 40 01 C3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 EB 90
     */

    // 定义处理接收到的数据的方法
    @SuppressLint("SetTextI18n")
    public static void processReceivedData(byte[] buffer, int bytes) {
        // 确保 RxData 的大小足够存储所有接收到的数据
        if (RxData == null) {
            RxData = new byte[1024 * 3]; // 假设每次接收的数据大小不超过 bytes
        }

        // 将接收到的数据存储到 RxData 中
        System.arraycopy(buffer, 0, RxData, totalBytesReceived, bytes);
        totalBytesReceived += bytes;

        // 如果接收了三次数据
        if (count++ >= 3) {
            count=0;
            // 将前两次数据拼接起来
            byte[] concatenatedData = new byte[1024 * 2];
            System.arraycopy(RxData, 0, concatenatedData, 0, 1024 * 2);

            // 显示拼接的数据在 ReceiveTextView 中
            // 这里假设 ReceiveTextView 是用来显示文本的视图
            // 你需要根据你的实际情况来更新文本视图的内容
            // 使用示例
            int index = findCharacter(RxData, (byte) 0xAB); // 在 RxData 中查找字符 'A'

            int temp=RxData[index + 77] & 0xFF;
            if(temp == 0x90){

            }
            else {
                index=-1;
            }
            if (index != -1) {
                // 找到了目标字符，进行相应的操作
                System.out.println("Found 'A' at index: " + index);
                // 获取 'A' 及其后面的数据
                byte[] dataAfterA = Arrays.copyOfRange(RxData, index, index+78);
                // 将字节数组转换为字符串
                String stringAfterA = new String(dataAfterA);
                // 在界面上显示字符串

                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < 78; i++) {
                    int t = RxData[index + i] & 0xFF;
                    stringBuilder.append("0x").append(String.format("%02X", t)).append("   ");
                }
                ReceiveTextView.setText(stringBuilder.toString());

                StringBuilder TimestringBuilder = new StringBuilder();
                for (int i = 0; i < 3; i++) {
                    int t = RxData[index + i + 1] & 0xFF;
                    TimestringBuilder.append(String.format("%02d", t)).append(" ");
                }
                //显示时间
                TimeViewText.setText("时间："+TimestringBuilder.toString());

                // 需要从RxData中某个index开始提取8个字节
                double result = bytesToDouble(RxData, 25+index);
                LongitudeViewText.setText("经度："+result);

                // 需要从RxData中某个index开始提取8个字节
                result = bytesToDouble(RxData, 33+index);
                LatitudeViewText.setText("纬度："+result);

                // 需要从RxData中某个index开始提取8个字节
                result = bytesToDouble(RxData, 13+index);
                CourseAngleViewText.setText("航向角："+result);

                // 需要从RxData中某个index开始提取8个字节
                result = bytesToDouble(RxData, 5+index);
                SpeedViewText.setText("速度："+result*0.5144);

                char a = (char)RxData[index + 4];
                Pin_TranslateViewText.setText("定位状态："+a);

                char m = (char)RxData[index + 24];
                PositioningModeViewText.setText("定位模式："+m);

                int n = (int)RxData[index + 41];
                HCSDSViewText.setText("卫星数量："+n);

                // 需要从RxData中某个index开始提取8个字节
                result = bytesToDouble(RxData, 42+index);
                HDOPViewText.setText("水平精度因子："+result);

                // 需要从RxData中某个index开始提取8个字节
                result = bytesToDouble(RxData, 50+index);
                AltitudeViewText.setText("海拔高度："+result);


            } else {
                // 没有找到目标字符
                System.out.println("Character 'A' not found.");
            }

            // 重置 RxData 和 totalBytesReceived
            System.arraycopy(RxData, 1024 * 2, RxData, 0, 1024);
            totalBytesReceived = 0;
            RxData=null;
        }
    }
    public static int findCharacter(byte[] data, byte target) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == target) {
                return i; // 找到目标字符，返回其索引
            }
        }
        return -1; // 如果没有找到目标字符，返回 -1
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){

        int my_item = item.getItemId();
        if(my_item == R.id.NetworkViewItem){
            intent=new Intent(MainActivity.this,NetworkMapActivity.class);
            startActivity(intent);
        } else if (my_item == R.id.TheIdleLoginItem) {
            intent=new Intent(MainActivity.this,SocketActivity.class);
            startActivity(intent);
        }

        return true;
    }
//    private static double bytesToDouble(byte[] bytes, int index) {
//        ByteBuffer buffer = ByteBuffer.wrap(bytes, index, 8);
//        return buffer.getDouble();
//    }
    private static double bytesToDouble(byte[] bytes, int index) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, index, 8);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // 如果数据是小端字节序
        return buffer.getDouble();
    }



}