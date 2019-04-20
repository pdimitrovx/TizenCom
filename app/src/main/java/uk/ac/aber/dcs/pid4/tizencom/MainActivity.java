package uk.ac.aber.dcs.pid4.tizencom;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private Button start_Btn, connect_test_Btn;
    private boolean isServiceBound = false;
    private Service_SAP SAPservice = null;
private boolean start_Btn_clicked = false;
    Service_SAP mConsumerService = null;



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        Spinner sensor_selector = findViewById(R.id.sensor_spinner);
        Spinner time_selector = findViewById(R.id.time_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter_sensor = ArrayAdapter.createFromResource(this,
                R.array.sensors_array, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapter_time = ArrayAdapter.createFromResource(this,
                R.array.time_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter_sensor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_time.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the sensor_selector
        sensor_selector.setAdapter(adapter_sensor);
        time_selector.setAdapter(adapter_time);

        //set up the start button
        start_Btn = (Button) findViewById(R.id.start_btn);
//onmCLIck listener
        start_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "TEsting connection...", Toast.LENGTH_SHORT).show();
               // Intent gameIntent = new Intent(getActivity(), MainActivity.class);
               // startActivity(gameIntent);

                // ((MainActivityVocab)getActivity()).setViewPager(1);
            }
        });

        //setup the connection test button
        connect_test_Btn = (Button) findViewById(R.id.connectTest);

        // Bind service
        isServiceBound = bindService(new Intent(MainActivity.this, Service_SAP.class), mConnection, Context.BIND_AUTO_CREATE);
    }


    public void mOnClick(View v) {
        switch (v.getId()) {
            case R.id.connectTest: {
                if (isServiceBound == true && SAPservice != null) {
                    SAPservice.findPeers();
                   start_Btn_clicked = false;
                }
                break;
            }
            case R.id.start_btn: {
                if (isServiceBound == true && start_Btn_clicked == false && SAPservice != null) {
                    if (SAPservice.sendData("Hello Message!") != -1) {
                        //todo wtf is it sendButtonClicked = true;
                    }else {
                        //todo wtfff sendButtonClicked = false;
                    }
                }
                break;
            }
            default:
        }
    }
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
             mConsumerService = ((Service_SAP.LocalBinder) service).getService();
            //updateTextView("onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mConsumerService = null;
            isServiceBound = false;
            //updateTextView("onServiceDisconnected");
        }
    };

}
//added the service connection thing that is needed in order to set up the sercive bound thing. Explain taht
//added         android:onClick="mOnClick" to each button so the trigger that method that has a switch case statement in it!
