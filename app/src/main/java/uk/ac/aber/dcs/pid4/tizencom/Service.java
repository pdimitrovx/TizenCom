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
public class Service extends SAAgent {


    protected Service(String s, Class<? extends SASocket> aClass) {
        super(s, aClass);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
//REemove this generated constructor
//finish accessoryservices by adding the package.service
//update the manifest
//Start on SAA agent class (here)