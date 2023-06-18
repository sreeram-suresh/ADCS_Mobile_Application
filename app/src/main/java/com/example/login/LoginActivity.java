package com.example.login;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.SharedPreferences;

public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    Button btnlogin;
    LocationManager locationManager;
    BluetoothAdapter bluetoothAdapter;

    DBHelper DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            OnGPS();
        }
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled())
        {
            OnBluetooth();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_FINE_LOCATION},1);

        username = (EditText) findViewById(R.id.lusername);
        password = (EditText) findViewById(R.id.lpassword);
        btnlogin = (Button) findViewById(R.id.lbtnsignin);
        DB = new DBHelper(this);

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String user = username.getText().toString();
                String pass = password.getText().toString();

                if(user.equals("")||pass.equals(""))
                    Toast.makeText(LoginActivity.this, "Please fill all the details!!!", Toast.LENGTH_SHORT).show();
                else{
                    Boolean checkuserpass = DB.checkusernamepassword(user,pass);
                    if(checkuserpass==true){
                        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
                        SharedPreferences.Editor myEdit = sharedPreferences.edit();
                        myEdit.putString("loginuser",user);
                        myEdit.apply();
                        Toast.makeText(LoginActivity.this, "Sign in SUCCESSFUL!!!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                    }else{
                        Toast.makeText(LoginActivity.this, "Invalid CREDENTIALS!!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void OnBluetooth() {

        final AlertDialog.Builder builder= new AlertDialog.Builder(this);

        builder.setMessage("Please Enable Bluetooth").setCancelable(false).setPositiveButton("ENABLE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            }
        });
        final AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }
    private void OnGPS() {

        final AlertDialog.Builder builder= new AlertDialog.Builder(this);

        builder.setMessage("Please Enable GPS").setCancelable(false).setPositiveButton("ENABLE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        final AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }
}