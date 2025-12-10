package com.example.adbtool;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String ACTION_USB_PERMISSION = "com.example.adbtool.USB_PERMISSION";
    private TextView txtLog;
    private UsbManager usbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLog = findViewById(R.id.txtLog);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        Button btnCheckAdb = findViewById(R.id.btnCheckAdb);
        Button btnCheckFastboot = findViewById(R.id.btnCheckFastboot);
        Button btnFlashImg = findViewById(R.id.btnFlashImg);

        btnCheckAdb.setOnClickListener(v -> checkDevices("ADB"));
        btnCheckFastboot.setOnClickListener(v -> checkDevices("Fastboot"));
        btnFlashImg.setOnClickListener(v -> flashImg());

        registerReceiver(usbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
    }

    private void checkDevices(String type) {
        log("Checking for " + type + " devices...");
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        
        if (deviceList.isEmpty()) {
            log("No USB devices found.");
            return;
        }

        for (UsbDevice device : deviceList.values()) {
            log("Found Device: " + device.getDeviceName() + " (Vendor: " + device.getVendorId() + ")");
            
            // Simple check (in real app, check interfaces/class)
            if (!usbManager.hasPermission(device)) {
                log("Requesting permission for " + device.getDeviceName());
                PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
                usbManager.requestPermission(device, permissionIntent);
            } else {
                log("Has permission for " + device.getDeviceName());
                // Here you would detect ADB vs Fastboot interface
            }
        }
    }

    private void flashImg() {
        log("Flash IMG clicked. (Functionality logic placeholder)");
        // Implement Fastboot flash logic here
    }

    private void log(String message) {
        runOnUiThread(() -> {
            txtLog.append("\n" + message);
        });
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                           log("Permission granted for " + device.getDeviceName());
                        }
                    } else {
                        log("Permission denied for device " + device);
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbReceiver);
    }
}
