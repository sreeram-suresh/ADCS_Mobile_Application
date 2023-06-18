package com.example.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
public class HomeActivity extends AppCompatActivity {
    DBHelper DB;
    private static final String TAG="DEBUG_HA";
    Button buttonConnect;
    Button buttonStart;
    Button clearScreen;
    Button hamburger;
    TextView tvMAReceivedMessage;
    TextView tvMAMessage;
    Spinner spinnerBTPairedDevices;
    LocationManager locationManager;
    String latitude, longitude,st,link;
    static final UUID MY_UUID= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothSocket BTSocket=null;
    BluetoothAdapter BTAdapter=null;
    Set<BluetoothDevice> BTPairedDevices=null;
    BluetoothDevice BTDevice=null;
    classBTInitDataCommunication cBTInitSendReceive=null;
    boolean bBTConnected=false;
    static public final int BT_CON_STATUS_NOT_CONNECTED=0;
    static public final int BT_CON_STATUS_CONNECTING=1;
    static public final int BT_CON_STATUS_CONNECTED=2;
    static public final int BT_CON_STATUS_FAILED=3;
    static public final int BT_CON_STATUS_CONNECTION_LOST=4;
    static int iBTConnectionStatus=BT_CON_STATUS_NOT_CONNECTED;
    static final int BT_STATE_LISTENING=1;
    static final int BT_STATE_CONNECTING=2;
    static final int BT_STATE_CONNECTED=3;
    static final int BT_STATE_CONNECTION_FAILED=4;
    static final int BT_STATE_MESSAGE_RECEIVED=5;
    final Handler animhandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        animhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvMAMessage.setText("Please connect to Vehicle");
            }
        }, 4000);
        Log.d(TAG, "onCreate-Start");
        tvMAReceivedMessage=findViewById(R.id.idMATextViewReceivedMessage);
        tvMAMessage=findViewById(R.id.idMATextViewMessage);
        tvMAReceivedMessage.setMovementMethod(new ScrollingMovementMethod());
        buttonConnect=findViewById(R.id.idMAButtonConnect);
        buttonStart=findViewById(R.id.idMAButtonStartCMD);
        clearScreen=findViewById(R.id.idMAButtonClearScreen);
        hamburger=findViewById(R.id.idMAHamburger);
        spinnerBTPairedDevices=findViewById(R.id.idMASpinnerBTPairedDevices);
        DB=new DBHelper(this);
        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        hamburger.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d(TAG,"Hamburger Clicked!");
                Intent intent = new Intent(getApplicationContext(), Hamburger.class);
                startActivity(intent);
            }
        });
        clearScreen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d(TAG,"ClearScreen Button Clicked");
                tvMAReceivedMessage.setText("");
            }
        });
        buttonConnect.setOnClickListener(new View.OnClickListener(){
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view){
                Log.d(TAG,"Connect Button Clicked");
                if(bBTConnected==false)
                {
                    if (spinnerBTPairedDevices.getSelectedItemId() == 0) {
                        Log.d(TAG, "Please select BT Device.");
                        Toast.makeText(getApplicationContext(), "Please Select BT Device", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String sSelectedDevice = spinnerBTPairedDevices.getSelectedItem().toString();
                    Log.d(TAG, "Selected Device=" + sSelectedDevice);
                    for (BluetoothDevice BTDev : BTPairedDevices) {
                        if (sSelectedDevice.equals(BTDev.getName())) {
                            tvMAMessage.setText("Connecting......");
                            BTDevice = BTDev;
                            Log.d(TAG, "Selected Device UUID=" + BTDevice.getAddress());
                            cBluetoothConnect cBTConnect=new cBluetoothConnect(BTDevice);
                            cBTConnect.start();
                        }
                    }
                }
                else {
                    Log.d(TAG, "Disconnecting BTConnection.");
                    if (BTSocket != null && BTSocket.isConnected())
                    {
                        try {
                            BTSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "BTDisconnect Error:" + e.getMessage());
                        }
                    }
                    buttonConnect.setText("Connect");
                    bBTConnected = false;
                }
            }
        });
        buttonStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d(TAG,"Start Button Clicked");
                sendMessage("go");
            }
        });
    }
    private String getLocation() {

        if (ActivityCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this,

                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else
        {
            Location LocationGps= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location LocationNetwork=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location LocationPassive=locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (LocationGps !=null)
            {
                double lat=LocationGps.getLatitude();
                double longi=LocationGps.getLongitude();

                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);
                st="\nLocation: "+"https://maps.google.com/?q="+latitude+","+longitude;
                return st;
            }
            else if (LocationNetwork !=null)
            {
                double lat=LocationNetwork.getLatitude();
                double longi=LocationNetwork.getLongitude();

                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);
                st="\nLocation: "+"https://maps.google.com/?q="+latitude+","+longitude;
                return st;
            }
            else if (LocationPassive !=null)
            {
                double lat=LocationPassive.getLatitude();
                double longi=LocationPassive.getLongitude();

                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);
                st="\nLocation: "+"https://maps.google.com/?q="+latitude+","+longitude;
                return st;
            }
            else
            {
                st="Please contact them.";
            }
        }
        return st;
    }
    public class cBluetoothConnect extends Thread
    {
        private BluetoothDevice device;
        @SuppressLint("MissingPermission")
        public cBluetoothConnect(BluetoothDevice BTDevice)
        {
            Log.i(TAG,"classBTConnect-start");
            device=BTDevice;
            try {
                BTSocket=device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch(Exception exp)
            {
                Log.e(TAG,"classBTConnect-exp:"+exp.getMessage());
            }
        }
        @SuppressLint("MissingPermission")
        public void run()
        {
            try{
                BTSocket.connect();
                Message message= Message.obtain();
                message.what=BT_STATE_CONNECTED;
                handler.sendMessage(message);

            } catch(IOException e){
                e.printStackTrace();
                Message message=Message.obtain();
                message.what=BT_STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }
    public class classBTInitDataCommunication extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private InputStream inputStream=null;
        private OutputStream outputStream=null;

        public classBTInitDataCommunication(BluetoothSocket socket)
        {
            Log.i(TAG,"classBTInitDataCommunication-start");
            bluetoothSocket=socket;

            try {
                inputStream=bluetoothSocket.getInputStream();
                outputStream=bluetoothSocket.getOutputStream();
            } catch(IOException e){
                e.printStackTrace();
                Log.e(TAG,"classBTInitDataCommunication-start-exp:"+e.getMessage());
            }
        }
        public void run()
        {
            byte[] buffer=new byte[1024];
            int bytes;
            while(BTSocket.isConnected())
            {
                try {
                    bytes=inputStream.read(buffer);
                    handler.obtainMessage(BT_STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch(IOException e){
                    e.printStackTrace();
                    Log.e(TAG,"BT Disconnect from device end, exp:"+e.getMessage());
                    iBTConnectionStatus=BT_CON_STATUS_CONNECTION_LOST;
                    try {
                        //disconnect bluetooth
                        Log.d(TAG,"Disconnecting BTConnection");
                        if(BTSocket!=null && BTSocket.isConnected())
                        {
                            BTSocket.close();
                        }
                        buttonConnect.setText("Connect");
                        tvMAMessage.setText("Please connect to Vehicle");
                        bBTConnected=false;
                    } catch(IOException ex){
                        ex.printStackTrace();
                    }
                }
            }
        }
        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    Handler handler=new Handler(new Handler.Callback(){
        public boolean handleMessage(Message msg){
            switch(msg.what)
            {
                case BT_STATE_LISTENING:
                    Log.d(TAG,"BT_STATE_LISTENING");
                    break;
                case BT_STATE_CONNECTING:
                    iBTConnectionStatus=BT_CON_STATUS_CONNECTING;
                    buttonConnect.setText("Connecting");
                    Log.d(TAG,"BT_STATE_CONNECTING");
                    break;
                case BT_STATE_CONNECTED:
                    iBTConnectionStatus=BT_CON_STATUS_CONNECTED;
                    Log.d(TAG,"BT_STATE_CONNECTED");
                    animhandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tvMAMessage.setText("Connected to vehicle");
                            buttonConnect.setText("Disconnect");
                        }
                    }, 3000);
                    cBTInitSendReceive=new classBTInitDataCommunication(BTSocket);
                    cBTInitSendReceive.start();
                    bBTConnected=true;
                    break;
                case BT_STATE_CONNECTION_FAILED:
                    iBTConnectionStatus=BT_CON_STATUS_FAILED;
                    Log.d(TAG,"BT_STATE_FAILED");
                    bBTConnected=false;
                    break;
                case BT_STATE_MESSAGE_RECEIVED:
                    byte[] readBuff=(byte[]) msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    Log.d(TAG,"Message Receive("+tempMsg.length()+"data:"+tempMsg);
                    if(tempMsg.charAt(0)=='e') {
                        link=getLocation();
                        tvMAReceivedMessage.append("\nEmergency");
                        SharedPreferences spp = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                        String un = spp.getString("loginuser","");
                        SmsManager smsManager=SmsManager.getDefault();
                        Cursor res=DB.getdata(un);
                        while(res.moveToNext()){
                            smsManager.sendTextMessage(res.getString(1),null,"EMERGENCY!\n"+un+" is currently intoxicated.\n" +
                                    "Please check to ensure their safety "+link,null,null);
                        }
                        Toast.makeText(HomeActivity.this, "Emergency message has been sent to all the available emergency contacts", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            return true;
        }
    });
    public void sendMessage(String sMessage)
    {
        if(BTSocket!=null && iBTConnectionStatus==BT_CON_STATUS_CONNECTED)
        {
            if(BTSocket.isConnected())
            {
                try{
                    cBTInitSendReceive.write(sMessage.getBytes());
                    tvMAReceivedMessage.append("\r\n ->" +sMessage);
                }catch(Exception exp){
                    Toast.makeText(this, "Can't send MESSAGE!!!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else{
            Toast.makeText(this, "Please connect to bluetooth device.", Toast.LENGTH_SHORT).show();
            tvMAReceivedMessage.append("\r\n Not Connected to Bluetooth");
        }
    }
    @SuppressLint("MissingPermission")
    void getBTPairedDevices()
    {
        Log.d(TAG,"getPairedDevices Started");
        BTAdapter=BluetoothAdapter.getDefaultAdapter();
        if(BTAdapter==null)
        {
            Log.e(TAG,"getPairedBTDevices, BTAdapter null");
            return;
        }
        else if(!BTAdapter.isEnabled())
        {
            Log.e(TAG,"getBTPairedDevices, BT not enabled.");
            return;
        }
        BTPairedDevices=BTAdapter.getBondedDevices();
        Log.e(TAG,"getBTPairedDevices, Paired Devices count="+BTPairedDevices.size());
        for(BluetoothDevice BTDev:BTPairedDevices)
        {
            Log.d(TAG,BTDev.getName()+","+BTDev.getAddress());
        }
    }
    @SuppressLint("MissingPermission")
    void populateSpinnerWithBTPairedDevices()
    {
        ArrayList<String> alPairedDevices=new ArrayList<>();
        alPairedDevices.add("Select");
        for(BluetoothDevice BTDev: BTPairedDevices)
        {
            alPairedDevices.add(BTDev.getName());
        }
        final ArrayAdapter<String> aaPairedDevices=new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,alPairedDevices);
        aaPairedDevices.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinnerBTPairedDevices.setAdapter(aaPairedDevices);
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume-Start");
        getBTPairedDevices();
        populateSpinnerWithBTPairedDevices();
    }
}