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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView mTextMessage; //navigation stuff (used to display status app)



    private Button start_Btn, connect_test_Btn;
    private boolean isServiceBound = false;
    private boolean start_Btn_clicked = false;
    private Service_SAP mConsumerService = null;
    private static MessageAdapter mMessageAdapter; //todo mesasge stuff, describe ite etc
    private ListView mMessageListView; //todo message stuff


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start_Btn_clicked = false;

        mTextMessage = (TextView) findViewById(R.id.app_status);

        // Bind service
        isServiceBound = bindService(new Intent(MainActivity.this,
                Service_SAP.class), mConnection, Context.BIND_AUTO_CREATE);


        //todo rename and tidy list view
        mMessageListView = findViewById(R.id.lvMessage);
        mMessageAdapter = new MessageAdapter();
        mMessageListView.setAdapter(mMessageAdapter);

        //todo navigatio drawer stuff, mayeb replace with fragment
        v = findViewById(R.id.app_status);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //Initialize spinners
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
        start_Btn = findViewById(R.id.start_btn);

        //setup the connection test button
        connect_test_Btn = findViewById(R.id.findPeerAgentBtn);

    }


    public void mOnClick(View v) {
        switch (v.getId()) {
            case R.id.findPeerAgentBtn: {
                if (isServiceBound == true && mConsumerService != null) {
                    mConsumerService.findPeers();
                    start_Btn_clicked = false;
                }
                break;
            }
            case R.id.start_btn: {
                if (isServiceBound == true && start_Btn_clicked == false && mConsumerService != null) {
                    if (mConsumerService.sendData("Hello Message!") != -1) {
                        //todo wtf is it sendButtonClicked = true;
                        start_Btn_clicked = true;
                    } else {
                        //todo wtfff sendButtonClicked = false;
                        start_Btn_clicked = false;
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
            updateTextView("onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mConsumerService = null;
            isServiceBound = false;
            updateTextView("onServiceDisconnected");
        }
    };

    public static void addMessage(String data) {
        mMessageAdapter.addMessage(new Message(data));
    }

    public static void updateTextView(final String str) {
        mTextView.setText(str);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        //todo, tidy this code up and deploy fragments / tabs or remove alltogether

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


    //todo message nested class have a look
    static final class Message {
        String data;

        public Message(String data) {
            super();
            this.data = data;
        }
    }

    //todo MESSAGE STUFF URGENT
    class MessageAdapter extends BaseAdapter {
        private static final int MAX_MESSAGES_TO_DISPLAY = 20;
        private List<Message> mMessages;

        public MessageAdapter() {
            mMessages = Collections.synchronizedList(new ArrayList<Message>());
        }

        void addMessage(final Message msg) {
            // Runnable newTask = (new Runnable() {
            //  @Override
            //  public void run() {
            if (mMessages.size() == MAX_MESSAGES_TO_DISPLAY) {
                mMessages.remove(0);
                mMessages.add(msg);
            } else {
                mMessages.add(msg);
            }
            notifyDataSetChanged();
            mMessageListView.setSelection(getCount() - 1);
            //   }
            //});
        }

        void clear() {
            // runOnUiThread(new Runnable() {
            ///  @Override
            //public void run() {
            mMessages.clear();
            notifyDataSetChanged();
            // }
            // });
        }

        @Override
        public int getCount() {
            return mMessages.size();
        }

        @Override
        public Object getItem(int position) {
            return mMessages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View messageRecordView = null;
            if (inflator != null) {
                messageRecordView = inflator.inflate(R.layout.message, null); //todo sort out what message does, item in list?
                TextView tvData = (TextView) messageRecordView.findViewById(R.id.tvData);
                Message message = (Message) getItem(position);
                tvData.setText(message.data);
            }
            return messageRecordView;
        }
    }
}
//added the service connection thing that is needed in order to set up the sercive bound thing. Explain taht
//added         android:onClick="mOnClick" to each button so the trigger that method that has a switch case statement in it!
