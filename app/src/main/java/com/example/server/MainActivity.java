package com.example.server;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ServerSocket serverSocket;
    private Socket tempClientSocket;
    Thread serverThread = null;
    public static final int SERVER_PORT = 3003;
    private LinearLayout msgList;
    private Handler handler;
    private int greenColor;
    private int redColor;
    private EditText edMessage;
    private ImageView start_server;
    private ImageView post_score;
    private ImageView post_data;
    private String[] data;
    private String[] answerData;
    private int on_off_flag=0;
    private int score;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Server");
        greenColor = ContextCompat.getColor(this, R.color.green);
        redColor = ContextCompat.getColor(this, R.color.red);
        handler = new Handler();
        msgList = (LinearLayout) findViewById(R.id.msgList);
        start_server = (ImageView) findViewById(R.id.start_server);
        post_data = (ImageView) findViewById(R.id.post_data);
        post_score = (ImageView) findViewById(R.id.post_score);


        data=new String[]{"What's the Capital City Of America ?  ","Tokyo  ","Washington  ","cairo  ",

                "What is always in Front of you but can't be seen   ","GPA  ","The Future  ","Your Crush  ",

                "which ocean is the largest ocean in the world ?  ","Pacific Ocean  ","Indian ocean  ","atlantic ocean  ",

                "how many countries in asia ?   ","48  ","34  ","67  ",

                "For The Equation: (X+2X+3X=12) X=?   ","2  ","7  ","12  ","start"

        };



        answerData=new String[4];





        start_server.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if(on_off_flag==0)
                {
                    start_server.setImageResource(R.drawable.power_off);
                    on_off_flag=1;
                    msgList.removeAllViews();
                    showMessage("Server Started.", Color.BLACK);
                    serverThread = new Thread(new ServerThread());
                    serverThread.start();
                    return;
                }

                if(on_off_flag==1)
                {

                    start_server.setImageResource(R.drawable.power_on);
                    on_off_flag=0;
                    //sendStatus("disconnect");
                    showMessage("Server Disconnected.", Color.RED);
                }





            }
        });


        post_data.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                sendExam(data);

            }
        });


        post_score.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                sendScore(String.valueOf(score));
                score=0;

            }
        });

    }

    public TextView textView(String message, int color) {
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }
        TextView tv = new TextView(this);
        tv.setTextColor(color);
        tv.setText(message + " [" + getTime() +"]");
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }

    public void showMessage(final String message, final int color) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                msgList.addView(textView(message, color));
            }
        });
    }

    /**SENDING EXAM TO CLIENT*********************************************************/
    private void sendExam(final String[] message) {
        try {
            if (null != tempClientSocket) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PrintWriter out = null;
                        try {
                            out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(tempClientSocket.getOutputStream())),
                                    true);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        for (int i=0;i<message.length;i++)
                        {
                            out.println(message[i]);
                            showMessage("Server : " + message[i], Color.BLUE);
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    private void sendScore(final String score) {/**SENDING SCORE TO CLIENT*********************************************************/
        try {
            if (null != tempClientSocket) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PrintWriter out = null;
                        try {
                            out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(tempClientSocket.getOutputStream())),
                                    true);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        out.println(score);
                        showMessage(score+":",Color.BLUE);

                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }




    /**SENDING Status TO CLIENT*********************************************************/
   /* private void sendStatus(final String status) {
        try {
            if (null != tempClientSocket) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PrintWriter out = null;
                        try {
                            out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(tempClientSocket.getOutputStream())),
                                    true);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        out.println(status);
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/



    class ServerThread implements Runnable {

        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                //findViewById(R.id.start_server).setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Error Starting Server : " + e.getMessage(), Color.RED);
            }
            if (null != serverSocket) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        socket = serverSocket.accept();
                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showMessage("Error Communicating to Client :" + e.getMessage(), Color.RED);
                    }
                }
            }
        }
    }

    class CommunicationThread implements Runnable {


        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            tempClientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Error Connecting to Client!!", Color.RED);
            }
            showMessage("Connected to Client!!", greenColor);
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    for(int i=0;i<answerData.length;i++)
                    {
                        answerData[i]= input.readLine();/**READING FROM CLIENT*********************************************************/

                        if (answerData[i]=="disconnect")
                        {
                            Thread.interrupted();
                            showMessage("Client : Disconnected", redColor);
                            //break;
                        }
                        else
                        {

                            if(
                                answerData[i].equals(data[2]) ||
                                answerData[i].equals(data[6]) ||
                                answerData[i].equals(data[9]) ||
                                answerData[i].equals(data[13])||
                                answerData[i].equals(data[17])

                            )

                            {

                                showMessage("Correct Answer from Client : " + answerData[i], greenColor);
                                score++;
                                Log.d("SCORE", "SCORE:"+"  "+score);

                            }



                            else
                            {
                                showMessage("Wrong Answer From Client : " + answerData[i], redColor);
                            }

                        }



                    }




                } catch (IOException e) {
                    e.printStackTrace();
                }


               // Log.d("SCORE AFTER ", "SCORE:"+"  "+score);
                //sendScore(String.valueOf(score));

            }
        }



    }





    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != serverThread) {
            //sendMessage("Disconnect");
            serverThread.interrupt();
            serverThread = null;
        }
    }
}