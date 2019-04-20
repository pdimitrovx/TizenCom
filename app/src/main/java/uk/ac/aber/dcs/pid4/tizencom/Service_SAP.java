package uk.ac.aber.dcs.pid4.tizencom;

import java.io.IOException;

import android.content.Intent;
import android.os.Handler;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;
import android.util.Log;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.*;

public class Service_SAP extends SAAgent {
    private static final String TAG = "HelloMessage(C)";

    private final IBinder mBinder = new LocalBinder();
    Handler mHandler = new Handler();
    private SAMessage mMessage = null;
    private SAPeerAgent mSAPeerAgent = null;
    private Toast mToast;

    public Service_SAP() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SA mAccessory = new SA();
        try {
            mAccessory.initialize(this);
        } catch (SsdkUnsupportedException e) {
            // try to handle SsdkUnsupportedException
            if (processUnsupportedException(e) == true) {
                return;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            /*
             * Your application can not use Samsung Accessory SDK. Your application should work smoothly
             * without using this SDK, or you may want to notify user and close your application gracefully
             * (release resources, stop Service_SAP threads, close UI thread, etc.)
             */
            stopSelf();
        }

        mMessage = new SAMessage(this) {

            @Override
            protected void onSent(SAPeerAgent peerAgent, int id) {

                Log.d(TAG, "onSent(), id: " + id + ", ToAgent: " + peerAgent.getPeerId());
                String val = "" + id + " SUCCESS ";
                displayToast("ACK Received: " + val, Toast.LENGTH_SHORT);
            }

            @Override
            protected void onError(SAPeerAgent peerAgent, int id, int errorCode) {

                Log.d(TAG, "onError(), id: " + id + ", ToAgent: " + peerAgent.getPeerId() + ", errorCode: " + errorCode);
                String result = null;
                switch (errorCode) {
                    case ERROR_PEER_AGENT_UNREACHABLE:
                        result = " FAILURE" + "[ " + errorCode + " ] : PEER_AGENT_UNREACHABLE ";
                        break;
                    case ERROR_PEER_AGENT_NO_RESPONSE:
                        result = " FAILURE" + "[ " + errorCode + " ] : PEER_AGENT_NO_RESPONSE ";
                        break;
                    case ERROR_PEER_AGENT_NOT_SUPPORTED:
                        result = " FAILURE" + "[ " + errorCode + " ] : ERROR_PEER_AGENT_NOT_SUPPORTED ";
                        break;
                    case ERROR_PEER_SERVICE_NOT_SUPPORTED:
                        result = " FAILURE" + "[ " + errorCode + " ] : ERROR_PEER_SERVICE_NOT_SUPPORTED ";
                        break;
                    case ERROR_SERVICE_NOT_SUPPORTED:
                        result = " FAILURE" + "[ " + errorCode + " ] : ERROR_SERVICE_NOT_SUPPORTED ";
                        break;
                    case ERROR_UNKNOWN:
                        result = " FAILURE" + "[ " + errorCode + " ] : UNKNOWN ";
                        break;
                }
                String val = "" + id + result;
                displayToast("NAK Received: " + val, Toast.LENGTH_SHORT);
                //MainActivity.updateButtonState(false);
            }

            @Override
            protected void onReceive(SAPeerAgent peerAgent, byte[] message) {
                String dataVal = new String(message);
                addMessage("Received: ", dataVal);
                //MainActivity.updateButtonState(false);
            }
        };
    }

    @Override
    public void onDestroy() {
        mSAPeerAgent = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    protected void onFindPeerAgentsResponse(SAPeerAgent[] peerAgents, int result) {
        if ((result == SAAgent.PEER_AGENT_FOUND) && (peerAgents != null)) {
            Toast.makeText(getApplicationContext(), "PEERAGENT_FOUND", Toast.LENGTH_LONG).show();
            for (SAPeerAgent peerAgent : peerAgents) {
                mSAPeerAgent = peerAgent;
            }
        } else if (result == SAAgent.FINDPEER_DEVICE_NOT_CONNECTED) {
            Toast.makeText(getApplicationContext(), "FINDPEER_DEVICE_NOT_CONNECTED", Toast.LENGTH_LONG).show();
        } else if (result == SAAgent.FINDPEER_SERVICE_NOT_FOUND) {
            Toast.makeText(getApplicationContext(), "FINDPEER_SERVICE_NOT_FOUND", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.NoPeersFound, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onError(SAPeerAgent peerAgent, String errorMessage, int errorCode) {
        super.onError(peerAgent, errorMessage, errorCode);
    }

    @Override
    protected void onPeerAgentsUpdated(SAPeerAgent[] peerAgents, int result) {
        final SAPeerAgent[] peers = peerAgents;
        final int status = result;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (peers != null) {
                    if (status == SAAgent.PEER_AGENT_AVAILABLE) {
                        Toast.makeText(getApplicationContext(), "PEER_AGENT_AVAILABLE", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "PEER_AGENT_UNAVAILABLE", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    public class LocalBinder extends Binder {
        public Service_SAP getService() {
            return Service_SAP.this;
        }
    }

    public void findPeers() {
        findPeerAgents();
    }

    public int sendData(String message) {
        int tid;

        if (mSAPeerAgent == null) {
            Toast.makeText(getApplicationContext(), "Try to find PeerAgent!", Toast.LENGTH_SHORT).show();
            return -1;
        }
        if (mMessage != null) {
            try {
                tid = mMessage.send(mSAPeerAgent, message.getBytes());
                addMessage("Sent: ", message);
                return tid;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                return -1;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                return -1;
            }
        }
        return -1;
    }

    private boolean processUnsupportedException(SsdkUnsupportedException e) {
        e.printStackTrace();
        int errType = e.getType();
        if (errType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED
                || errType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED) {
            /*
             * Your application can not use Samsung Accessory SDK. You application should work smoothly
             * without using this SDK, or you may want to notify user and close your app gracefully (release
             * resources, stop Service_SAP threads, close UI thread, etc.)
             */
            stopSelf();
        } else if (errType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED) {
            Log.e(TAG, "You need to install Samsung Accessory SDK to use this application.");
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED) {
            Log.e(TAG, "You need to update Samsung Accessory SDK to use this application.");
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED) {
            Log.e(TAG, "We recommend that you update your Samsung Accessory SDK before using this application.");
            return false;
        }
        return true;
    }

    public void clearToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    private void displayToast(String str, int duration) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), str, duration);
        mToast.show();
    }

    private void updateTextView(final String str) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
               // MainActivity.updateTextView(str);
            }
        });
    }

    private void addMessage(final String prefix, final String data) {
        final String strToUI = prefix.concat(data);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
               //MainActivity.addMessage(strToUI);
            }
        });
    }
}

//REemove this generated constructor
//finish accessoryservices by adding the package.service
//update the manifest
//Start on SAA agent class (here)