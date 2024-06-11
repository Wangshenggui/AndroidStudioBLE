package com.example.ble.BtThread;

import android.bluetooth.BluetoothSocket;

import com.example.ble.MainActivity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

//连接了蓝牙设备建立通信之后的数据交互线程类
public class ConnectedThread extends Thread{

    BluetoothSocket bluetoothSocket=null;
    InputStream inputStream=null;//获取输入数据

    int[] lastData=new int[]{0,0};
    public ConnectedThread(BluetoothSocket bluetoothSocket){
        this.bluetoothSocket=bluetoothSocket;
        //先新建暂时的Stream
        InputStream inputTemp=null;
        OutputStream outputTemp=null;
        try {
            inputTemp=this.bluetoothSocket.getInputStream();
            outputTemp=this.bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            try {
                bluetoothSocket.close();//出错就关闭线程吧
            } catch (IOException ex) {}
        }
        inputStream=inputTemp;
        MainActivity.outputStream=outputTemp;
    }

    public void run() {
        try {
            // Wrap the InputStream with BufferedInputStream for better performance
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            byte[] buffer = new byte[1024]; // Buffer to store the incoming data
            int bytesRead; // Bytes read in each iteration

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the BufferedInputStream
                bytesRead = bufferedInputStream.read(buffer);

                // If bytesRead is -1, it means the end of the stream has been reached
                if (bytesRead == -1) {
                    break; // Exit the loop
                }

                // Now you have the received data in buffer up to bytesRead,
                // you can process it as needed
                // For example, you can send it to the UI for display or perform any other actions
                // Example: Send the received data to MainActivity for processing
                MainActivity.processReceivedData(buffer, bytesRead);
            }

        } catch (IOException e) {
            // If an exception occurs, print the stack trace
            e.printStackTrace();
        }
    }




    public void btWriteInt(int[] intData){
        for(int sendInt:intData){
            try {
                MainActivity.outputStream.write(sendInt);
            } catch (IOException e) {}
        }
    }

    //自定义的发送字符串的函数
    public void btWriteString(String string){
        for(byte sendData:string.getBytes()){
            try {
                MainActivity.outputStream.write(sendData);//outputStream发送字节的函数
            } catch (IOException e) {}
        }
    }

    //自定义的关闭Socket线程的函数
    public void cancel(){
        try {
            bluetoothSocket.close();
        } catch (IOException e) {}
    }
}
