package com.example.login;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.provider.ContactsContract;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.net.Uri;
import android.annotation.SuppressLint;
import android.widget.Toast;
import android.Manifest;
import android.app.AlertDialog;
import android.content.SharedPreferences;
public class Hamburger extends AppCompatActivity {
    Button add_emergency;
    Button remove_emergency;
    Button view_emergency;
    Button logout;
    Button dismiss;
    DBHelper MyDB;
    static final int PICK_CONTACT=1;
    private static final String TAG="DEBUG_HA";
    protected void onCreate(Bundle savedInstanceState) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hamburger);
        Log.d(TAG, "onCreate-Want hamburger?");
        add_emergency = (Button) findViewById(R.id.add_emergency);
        remove_emergency = (Button) findViewById(R.id.remove_emergency);
        view_emergency = (Button)findViewById(R.id.view_emergency);
        logout = (Button) findViewById(R.id.logout);
        dismiss = (Button) findViewById(R.id.dismiss);
        MyDB = new DBHelper(this);
        add_emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent,PICK_CONTACT);
            }
        });
        remove_emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences spp = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                String delun = spp.getString("loginuser","");
                MyDB.deletedata(delun);
                Toast.makeText(Hamburger.this, "ALL EMERGENCY CONTACTS DELETED!!!", Toast.LENGTH_SHORT).show();
            }
        });
        view_emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                String un = sp.getString("loginuser","");
                Cursor res=MyDB.getdata(un);
                if(res.getCount()==0){
                    Toast.makeText(Hamburger.this,"No Contacts Exists",Toast.LENGTH_SHORT).show();
                }
                StringBuffer buffer=new StringBuffer();
                while(res.moveToNext()){
                    buffer.append("Name:"+res.getString(0)+"\n");
                    buffer.append("Contact:"+res.getString(1)+"\n\n");
                }
                AlertDialog.Builder builder=new AlertDialog.Builder(Hamburger.this);
                builder.setCancelable(true);
                builder.setTitle("Emergency Contacts");
                builder.setMessage(buffer.toString());
                builder.show();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    @SuppressLint("Range")
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (PICK_CONTACT) :
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c =  managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String id =c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        @SuppressLint("Range")
                        String hasPhone =c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,
                                    null, null);
                            phones.moveToFirst();
                            @SuppressLint("Range")
                            String cName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                            String cNumber = phones.getString(phones.getColumnIndex("data1"));
                            SharedPreferences sh = getSharedPreferences("MySharedPref",MODE_PRIVATE);
                            String user_name = sh.getString("loginuser","");
                            Boolean checkinsertdata=MyDB.insertuserdata(cName,cNumber,user_name);
                            if(checkinsertdata==true)
                                Toast.makeText(Hamburger.this,"One contact added",Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(Hamburger.this,"Contact not added",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }
}