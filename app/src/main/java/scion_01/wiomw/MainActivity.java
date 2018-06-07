package scion_01.wiomw;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button scan;
    private TextView prev;
    private String deviceIp;
    private String deviceMac;
    private String line = "";
    private BufferedReader localBufferdReader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scan = findViewById(R.id.scanWifi);
        prev = findViewById(R.id.previous);


        scan.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //sweetAlertDialog = new SweetAlertDialog(MainActivity.this);



        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        deviceIp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        deviceMac = getMacAddr();
        Log.e("Ip: ", deviceIp);
        Log.e("Mac", deviceMac);
        try {
            networkDiscovery();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            enterPrevEntries();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void networkDiscovery() throws InterruptedException {
        Runtime runtime= Runtime.getRuntime();

        for(int i=1;i<254;i++) {
            Process mIpAddrProcess = null;
            try {
                mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 192.168.0."+String.valueOf(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
            int mExitValue = mIpAddrProcess.waitFor();
            System.out.println(" mExitValue " + mExitValue);
            if (mExitValue == 0) {

            }
        }


    }

    private void enterPrevEntries() throws IOException {

        try {
            localBufferdReader = new BufferedReader(new FileReader(new File("/proc/net/arp")));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while ((line = localBufferdReader.readLine()) == null) {
            localBufferdReader.close();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            localBufferdReader = new BufferedReader(new FileReader(new File("/proc/net/arp")));
        }
        prev.setText("");
        do {
            String[] ipmac = line.split("[ ]+");
            if (!ipmac[0].matches("IP")) {
                String ip = ipmac[0];
                String mac = ipmac[3];
                if(!mac.equals("00:00:00:00:00:00"))
                prev.setText(prev.getText().toString()+"\n"+"IP: "+" "+ip+"   "+"MAC: "+mac);
            }
        } while ((line = localBufferdReader.readLine()) != null);
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    // res1.append(Integer.toHexString(b & 0xFF) + ":");

                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
            Log.d("Exception: ", ex.toString());
        }
        return "";
    }
}
