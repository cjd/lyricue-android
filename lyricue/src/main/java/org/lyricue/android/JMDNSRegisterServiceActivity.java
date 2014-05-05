package org.lyricue.android;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

public class JMDNSRegisterServiceActivity extends Activity {
    private static final String TAG = Lyricue.class.getSimpleName();
    private static JmDNS mJmDNS = null;
    private static boolean running = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.JMDNS_Activity);

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    WifiManager wifi = (WifiManager) JMDNSRegisterServiceActivity.this
                            .getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiinfo = wifi.getConnectionInfo();
                    int intaddr = wifiinfo.getIpAddress();
                    byte[] byteaddr = new byte[]{(byte) (intaddr & 0xff),
                            (byte) (intaddr >> 8 & 0xff),
                            (byte) (intaddr >> 16 & 0xff),
                            (byte) (intaddr >> 24 & 0xff)};

                    mJmDNS = JmDNS.create(InetAddress.getByAddress(byteaddr));

                    ServiceInfo mServiceInfo = ServiceInfo.create(
                            "_lyricue._tcp.local.", "Lyricue Display", 2346,
                            "data=android");
                    mJmDNS.registerService(mServiceInfo);
                    Log.w(TAG,
                            "registerService:" +
                                    mServiceInfo.toString()
                    );
                    while (running) {
                        Thread.sleep(1);
                    }
                    mJmDNS.unregisterService(mServiceInfo);
                    Log.w(TAG,
                            "unregisterService:" +
                                    mServiceInfo.toString()
                    );
                    mJmDNS.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
