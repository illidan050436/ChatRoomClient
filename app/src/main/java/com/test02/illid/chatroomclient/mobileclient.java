package com.test02.illid.chatroomclient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class mobileclient extends AppCompatActivity {
    // global declaration
    private Button RegisterButton, SendButton, ExitButton;
    private static EditText InputText, OutputText;
    private static final String SERVERIP = "172.16.1.27";
    private static final int SERVERPORT = 9054;
    private Thread mThread = null;
    private Socket mSocket = null;
    private BufferedReader mBufferedReader = null;
    private PrintWriter mPrintWriter = null;
    private static String mStrMSG = "";   //!!!!!!!!!!!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobileclient);

        RegisterButton = (Button)findViewById(R.id.button);
        SendButton = (Button)findViewById(R.id.button2);
        ExitButton = (Button)findViewById(R.id.button3);
        InputText = (EditText)findViewById(R.id.editText);
        OutputText = (EditText)findViewById(R.id.editText2);

        // Register user name
        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    new ConnectSocketTask().execute();
                }catch(Exception e) {
                    Log.e("RegisterButton", e.toString());
                }
            }
        });
        // Send the message to the server
        SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // get the content in the input edit text
                    String str = InputText.getText().toString() + "\n";
                    // send the message to the server
                    mPrintWriter.print(str);
                    mPrintWriter.flush();
                }catch(Exception e) {
                    Log.e("SendButton", e.toString());
                }
            }
        });
        // Exit the chat room by sending an exit instruction
        ExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // send the special client exit instruction to the server
                    String str = "client_exit";
                    mPrintWriter.print(str);
                    mPrintWriter.flush();
                }catch(Exception e) {
                    Log.e("ExitButton", e.toString());
                }
            }
        });
        // start the thread to listen the messaages from the server
        mThread = new Thread(mRunnable);
        mThread.start();
    }
    // thread used to listen the messages from the server
    protected Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            while(true) {
                try {
                    if (mBufferedReader != null) {
                        if ((mStrMSG = mBufferedReader.readLine()) != null) {
                            mStrMSG += "\n"; // change the message line
                            mHandler.sendMessage(mHandler.obtainMessage());// send message
                        }
                    }
                }catch(Exception e) {
                    Log.e("mRunnable", e.toString());
                }
            }
        }
    };
    // refresh the output edit text, can't refresh the UI in the main thread due to protection
    protected static Handler mHandler = new Handler() {
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            // refresh
            try{
                OutputText.append(mStrMSG); // append the chat history
            }catch(Exception e){
                Log.e("mHandler", e.toString());
            }
        }
    };

    //
    private class ConnectSocketTask extends AsyncTask {
        //protected Bitmap doInBackground(String... urls) {
        //    return loadImageFromNetwork(urls[0]);
        //}
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                // create the client socket
                mSocket = new Socket(SERVERIP, SERVERPORT);
                // obtain the socket stream reader and writer
                mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                mPrintWriter = new PrintWriter(mSocket.getOutputStream(), true);
            } catch (Exception e) {
                Log.e("Task", e.toString());
            }
            return null;
        }
    }
}