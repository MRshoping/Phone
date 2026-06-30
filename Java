package com.example.devicemanager;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, SensorEventListener {

    TextView tvDeviceName, tvModel, tvAndroidVersion, tvBattery, tvStorage, tvRAM;
    TextView tvWifi, tvWifiName, tvWifiIP, tvWifiSpeed, tvWifiStrength;
    TextView tvBluetooth, tvBluetoothDevice, tvBluetoothMAC;
    TextView tvMobileData, tvMobileNetwork, tvMobileSignal, tvIMEI, tvSIM, tvMobileNumber;
    TextView tvCamera, tvMic, tvGPS, tvSensor, tvScreenBrightness, tvStatus;
    TextView tvUptime, tvTemperature, tvCharging, tvSerialNumber, tvManufacturer;
    TextView tvTotalContacts, tvStorageInternal, tvStorageExternal;

    Button btnCamera, btnMic, btnWifi, btnBluetooth, btnMobileData, btnFlash, btnRefresh;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    Camera camera;
    MediaRecorder mediaRecorder;
    WifiManager wifiManager;
    BluetoothAdapter bluetoothAdapter;
    ConnectivityManager connectivityManager;
    TelephonyManager telephonyManager;
    SubscriptionManager subscriptionManager;
    SensorManager sensorManager;
    Sensor accelerometer;

    boolean isCameraOn = false, isMicOn = false, isFlashOn = false;
    android.os.Handler handler = new android.os.Handler();
    Runnable autoRefresh;

    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadAllInfo();
            tvStatus.setText("📶 WiFi Updated!");
        }
    };

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadAllInfo();
            tvStatus.setText("📡 Bluetooth Updated!");
        }
    };

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadAllInfo();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(25, 25, 25, 25);
        mainLayout.setBackgroundColor(0xFF1E1E2F);

        TextView title = new TextView(this);
        title.setText("📱 My Complete Device Manager");
        title.setTextSize(24);
        title.setTextColor(0xFFFFFFFF);
        title.setGravity(View.TEXT_ALIGNMENT_CENTER);
        title.setPadding(0, 0, 0, 20);
        mainLayout.addView(title);

        // SECTION 1: DEVICE IDENTITY
        sectionTitle(mainLayout, "📱 Device Identity");
        tvDeviceName = addTextView(mainLayout, "📱 Device Name: --");
        tvModel = addTextView(mainLayout, "📟 Model: --");
        tvManufacturer = addTextView(mainLayout, "🏭 Manufacturer: --");
        tvSerialNumber = addTextView(mainLayout, "🔢 Serial Number: --");
        tvAndroidVersion = addTextView(mainLayout, "🤖 Android Version: --");
        tvUptime = addTextView(mainLayout, "⏱️ Uptime: --");
        tvTemperature = addTextView(mainLayout, "🌡️ Temperature: --");
        tvCharging = addTextView(mainLayout, "⚡ Charging: --");

        // SECTION 2: SIM & MOBILE NUMBER
        sectionTitle(mainLayout, "📱 SIM & Mobile Number");
        tvMobileNumber = addTextView(mainLayout, "📱 Mobile Number: --");
        tvIMEI = addTextView(mainLayout, "📱 IMEI: --");
        tvSIM = addTextView(mainLayout, "📱 SIM Status: --");
        tvMobileNetwork = addTextView(mainLayout, "📱 Network: --");
        tvMobileSignal = addTextView(mainLayout, "📱 Signal: --");

        // SECTION 3: SYSTEM INFO
        sectionTitle(mainLayout, "🖥️ System Info");
        tvBattery = addTextView(mainLayout, "🔋 Battery: --");
        tvStorage = addTextView(mainLayout, "💾 Storage: --");
        tvStorageInternal = addTextView(mainLayout, "💾 Internal Storage: --");
        tvStorageExternal = addTextView(mainLayout, "💾 External Storage: --");
        tvRAM = addTextView(mainLayout, "🧠 RAM: --");
        tvScreenBrightness = addTextView(mainLayout, "☀️ Brightness: --");

        // SECTION 4: CONTACTS
        sectionTitle(mainLayout, "👤 Contacts");
        tvTotalContacts = addTextView(mainLayout, "👤 Total Contacts: --");

        // SECTION 5: NETWORK & CONNECTIVITY
        sectionTitle(mainLayout, "📶 Network & Connectivity");
        tvWifi = addTextView(mainLayout, "📶 WiFi: --");
        tvWifiName = addTextView(mainLayout, "📶 WiFi Network: --");
        tvWifiIP = addTextView(mainLayout, "📶 IP Address: --");
        tvWifiSpeed = addTextView(mainLayout, "📶 Speed: --");
        tvWifiStrength = addTextView(mainLayout, "📶 Signal Strength: --");
        tvBluetooth = addTextView(mainLayout, "📡 Bluetooth: --");
        tvBluetoothDevice = addTextView(mainLayout, "📡 Connected Device: --");
        tvBluetoothMAC = addTextView(mainLayout, "📡 MAC Address: --");
        tvMobileData = addTextView(mainLayout, "📱 Mobile Data: --");

        // SECTION 6: CAMERA & AUDIO
        sectionTitle(mainLayout, "📷 Camera & Audio");
        tvCamera = addTextView(mainLayout, "📷 Camera: --");
        tvMic = addTextView(mainLayout, "🎤 Mic: --");

        surfaceView = new SurfaceView(this);
        surfaceView.setLayoutParams(new LinearLayout.LayoutParams(500, 300));
        surfaceView.setPadding(0, 10, 0, 10);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        mainLayout.addView(surfaceView);

        // SECTION 7: SENSORS
        sectionTitle(mainLayout, "🧭 Sensors");
        tvGPS = addTextView(mainLayout, "📍 GPS: --");
        tvSensor = addTextView(mainLayout, "📳 Accelerometer: --");

        // SECTION 8: BUTTONS
        LinearLayout row1 = buttonRow(mainLayout);
        btnCamera = addButton(row1, "📷 Camera ON", 0xFFFF6B6B);
        btnMic = addButton(row1, "🎤 Mic ON", 0xFFFFA94D);
        mainLayout.addView(row1);

        LinearLayout row2 = buttonRow(mainLayout);
        btnWifi = addButton(row2, "📶 WiFi ON", 0xFF4ECDC4);
        btnBluetooth = addButton(row2, "📡 Bluetooth ON", 0xFFA29BFE);
        mainLayout.addView(row2);

        LinearLayout row3 = buttonRow(mainLayout);
        btnMobileData = addButton(row3, "📱 Data ON", 0xFFFFD93D);
        btnFlash = addButton(row3, "🔦 Flash OFF", 0xFFFF6B6B);
        mainLayout.addView(row3);

        LinearLayout row4 = buttonRow(mainLayout);
        btnRefresh = addButton(row4, "🔄 Refresh", 0xFF845EC2);
        mainLayout.addView(row4);

        tvStatus = new TextView(this);
        tvStatus.setText("✅ Ready");
        tvStatus.setTextColor(0xFF4CAF50);
        tvStatus.setTextSize(14);
        tvStatus.setGravity(View.TEXT_ALIGNMENT_CENTER);
        tvStatus.setPadding(0, 20, 0, 0);
        mainLayout.addView(tvStatus);

        setContentView(mainLayout);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        subscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        checkPermissions();
        loadAllInfo();

        autoRefresh = new Runnable() {
            @Override
            public void run() {
                loadAllInfo();
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(autoRefresh);

        btnCamera.setOnClickListener(v -> toggleCamera());
        btnMic.setOnClickListener(v -> toggleMic());
        btnWifi.setOnClickListener(v -> toggleWifi());
        btnBluetooth.setOnClickListener(v -> toggleBluetooth());
        btnMobileData.setOnClickListener(v -> toggleMobileData());
        btnFlash.setOnClickListener(v -> toggleFlash());
        btnRefresh.setOnClickListener(v -> {
            tvStatus.setText("🔄 Refreshing...");
            loadAllInfo();
            tvStatus.setText("✅ Refreshed!");
        });
    }

    private void sectionTitle(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(18);
        tv.setTextColor(0xFF4CAF50);
        tv.setPadding(0, 20, 0, 10);
        parent.addView(tv);
    }

    private TextView addTextView(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(13);
        tv.setPadding(0, 3, 0, 3);
        parent.addView(tv);
        return tv;
    }

    private LinearLayout buttonRow(LinearLayout parent) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);
        return row;
    }

    private Button addButton(LinearLayout row, String text, int color) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setBackgroundColor(color);
        btn.setTextColor(0xFFFFFFFF);
        btn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        btn.setPadding(10, 15, 10, 15);
        btn.setTextSize(12);
        row.addView(btn);
        return btn;
    }

    private void loadAllInfo() {
        String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
        tvDeviceName.setText("📱 Device Name: " + deviceName);
        tvModel.setText("📟 Model: " + Build.MODEL);
        tvManufacturer.setText("🏭 Manufacturer: " + Build.MANUFACTURER);
        tvSerialNumber.setText("🔢 Serial Number: " + Build.getSerial());
        tvAndroidVersion.setText("🤖 Android Version: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");

        long uptime = System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        tvUptime.setText("⏱️ Uptime: " + days + "d " + (hours % 24) + "h " + (minutes % 60) + "m");

        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int status = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS);
        tvCharging.setText("⚡ Charging: " + (status == BatteryManager.BATTERY_STATUS_CHARGING ? "✅ Yes" : "❌ No"));

        int temp = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_TEMPERATURE);
        tvTemperature.setText("🌡️ Temperature: " + (temp / 10.0) + "°C");

        int battery = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        tvBattery.setText("🔋 Battery: " + battery + "%");

        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long totalBytes = stat.getTotalBytes();
        long availableBytes = stat.getAvailableBytes();
        long usedBytes = totalBytes - availableBytes;
        tvStorage.setText("💾 Storage: " + formatSize(usedBytes) + " / " + formatSize(totalBytes));

        File internal = getFilesDir();
        tvStorageInternal.setText("💾 Internal Storage: " + formatSize(internal.getTotalSpace() - internal.getFreeSpace()) + " / " + formatSize(internal.getTotalSpace()));

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File external = Environment.getExternalStorageDirectory();
            tvStorageExternal.setText("💾 External Storage: " + formatSize(external.getTotalSpace() - external.getFreeSpace()) + " / " + formatSize(external.getTotalSpace()));
        } else {
            tvStorageExternal.setText("💾 External Storage: ❌ Not Available");
        }

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        long totalRAM = mi.totalMem;
        long usedRAM = totalRAM - mi.availMem;
        tvRAM.setText("🧠 RAM: " + formatSize(usedRAM) + " / " + formatSize(totalRAM));

        try {
            int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            tvScreenBrightness.setText("☀️ Brightness: " + brightness + " / 255");
        } catch (Exception e) {
            tvScreenBrightness.setText("☀️ Brightness: --");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            String mobileNumber = telephonyManager.getLine1Number();
            if (mobileNumber == null || mobileNumber.isEmpty()) {
                if (subscriptionManager != null) {
                    List<SubscriptionInfo> subs = subscriptionManager.getActiveSubscriptionInfoList();
                    if (subs != null && !subs.isEmpty()) {
                        for (SubscriptionInfo sub : subs) {
                            String number = sub.getNumber();
                            if (number != null && !number.isEmpty()) {
                                mobileNumber = number;
                                break;
                            }
                        }
                    }
                }
            }
            tvMobileNumber.setText("📱 Mobile Number: " + (mobileNumber != null && !mobileNumber.isEmpty() ? mobileNumber : "❌ Not Available"));

            String imei = telephonyManager.getImei();
            tvIMEI.setText("📱 IMEI: " + (imei != null ? imei : "❌ Not Available"));

            int simState = telephonyManager.getSimState();
            tvSIM.setText("📱 SIM Status: " + getSimStateName(simState));

            String networkOperator = telephonyManager.getNetworkOperatorName();
            tvMobileNetwork.setText("📱 Network: " + (networkOperator != null && !networkOperator.isEmpty() ? networkOperator : "Unknown"));
        } else {
            tvMobileNumber.setText("📱 Mobile Number: ⚠️ Permission Required");
            tvIMEI.setText("📱 IMEI: ⚠️ Permission Required");
            tvSIM.setText("📱 SIM Status: ⚠️ Permission Required");
            tvMobileNetwork.setText("📱 Network: ⚠️ Permission Required");
        }

        int signal = telephonyManager.getNetworkType();
        tvMobileSignal.setText("📱 Signal: " + getNetworkTypeName(signal));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            int contactCount = getContactCount();
            tvTotalContacts.setText("👤 Total Contacts: " + contactCount);
        } else {
            tvTotalContacts.setText("👤 Total Contacts: ⚠️ Permission Required");
        }

        if (wifiManager.isWifiEnabled()) {
            tvWifi.setText("📶 WiFi: ✅ ON");
            btnWifi.setText("📶 WiFi OFF");
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getSSID() != null) {
                String ssid = wifiInfo.getSSID().replace("\"", "");
                tvWifiName.setText("📶 WiFi Network: 📡 " + ssid);
                tvWifiIP.setText("📶 IP Address: " + intToIp(wifiInfo.getIpAddress()));
                tvWifiSpeed.setText("📶 Speed: " + wifiInfo.getLinkSpeed() + " Mbps");
                int rssi = wifiInfo.getRssi();
                int strength = WifiManager.calculateSignalLevel(rssi, 5);
                tvWifiStrength.setText("📶 Signal Strength: " + strength + "/5");
            } else {
                tvWifiName.setText("📶 WiFi Network: ❌ Not Connected");
            }
        } else {
            tvWifi.setText("📶 WiFi: ❌ OFF");
            btnWifi.setText("📶 WiFi ON");
            tvWifiName.setText("📶 WiFi Network: ⚠️ WiFi OFF");
        }

        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                tvBluetooth.setText("📡 Bluetooth: ✅ ON");
                btnBluetooth.setText("📡 Bluetooth OFF");
                tvBluetoothMAC.setText("📡 MAC Address: " + bluetoothAdapter.getAddress());

                if (bluetoothAdapter.getBondedDevices() != null && !bluetoothAdapter.getBondedDevices().isEmpty()) {
                    StringBuilder deviceNames = new StringBuilder();
                    for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                            deviceNames.append(device.getName()).append(", ");
                        }
                    }
                    if (deviceNames.length() > 0) {
                        tvBluetoothDevice.setText("📡 Connected Device: 🔗 " + deviceNames.substring(0, deviceNames.length() - 2));
                    } else {
                        tvBluetoothDevice.setText("📡 Connected Device: ❌ None");
                    }
                } else {
                    tvBluetoothDevice.setText("📡 Connected Device: ❌ None");
                }
            } else {
                tvBluetooth.setText("📡 Bluetooth: ❌ OFF");
                btnBluetooth.setText("📡 Bluetooth ON");
                tvBluetoothDevice.setText("📡 Connected Device: ⚠️ BT OFF");
            }
        }

        NetworkInfo mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnectedOrConnecting()) {
            tvMobileData.setText("📱 Mobile Data: ✅ ON");
            btnMobileData.setText("📱 Data OFF");
        } else {
            tvMobileData.setText("📱 Mobile Data: ❌ OFF");
            btnMobileData.setText("📱 Data ON");
        }

        tvCamera.setText("📷 Camera: " + (isCameraOn ? "✅ ON" : "❌ OFF"));
        tvMic.setText("🎤 Mic: " + (isMicOn ? "✅ ON" : "❌ OFF"));

        boolean gps = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        tvGPS.setText("📍 GPS: " + (gps ? "✅ ON" : "❌ OFF"));
    }

    private int getContactCount() {
        int count = 0;
        try {
            Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                count = cursor.getCount();
                cursor.close();
            }
        } catch (Exception e) {
            count = 0;
        }
        return count;
    }

    private void toggleCamera() {
        if (!isCameraOn) {
            try {
                camera = Camera.open();
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                isCameraOn = true;
                btnCamera.setText("📷 Camera OFF");
                tvStatus.setText("📷 Camera ON");
                loadAllInfo();
            } catch (Exception e) {
                Toast.makeText(this, "Camera Error", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
                isCameraOn = false;
                btnCamera.setText("📷 Camera ON");
                tvStatus.setText("📷 Camera OFF");
                loadAllInfo();
            }
        }
    }

    private void toggleMic() {
        if (!isMicOn) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile("/dev/null");
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                isMicOn = true;
                btnMic.setText("🎤 Mic OFF");
                tvStatus.setText("🎤 Mic ON");
                loadAllInfo();
            } catch (Exception e) {
                Toast.makeText(this, "Mic Error", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isMicOn = false;
                btnMic.setText("🎤 Mic ON");
                tvStatus.setText("🎤 Mic OFF");
                loadAllInfo();
            }
        }
    }

    private void toggleWifi() {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
            tvStatus.setText("📶 WiFi OFF");
        } else {
            wifiManager.setWifiEnabled(true);
            tvStatus.setText("📶 WiFi ON");
        }
        loadAllInfo();
    }

    private void toggleBluetooth() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
                tvStatus.setText("📡 Bluetooth OFF");
            } else {
                bluetoothAdapter.enable();
                tvStatus.setText("📡 Bluetooth ON");
            }
        }
        loadAllInfo();
    }

    private void toggleMobileData() {
        startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
        tvStatus.setText("📱 Open Settings to toggle Data");
    }

    private void toggleFlash() {
        if (camera != null) {
            Camera.Parameters params = camera.getParameters();
            if (!isFlashOn) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(params);
                isFlashOn = true;
                btnFlash.setText("🔦 Flash OFF");
                tvStatus.setText("🔦 Flash ON");
            } else {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);
                isFlashOn = false;
                btnFlash.setText("🔦 Flash ON");
                tvStatus.setText("🔦 Flash OFF");
            }
        } else {
            Toast.makeText(this, "Camera must be ON for Flash", Toast.LENGTH_SHORT).show();
        }
    }

    private String intToIp(int ip) {
        return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
    }

    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", size / Math.pow(1024, exp), pre);
    }

    private String getNetworkTypeName(int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_GPRS: return "GPRS (2G)";
            case TelephonyManager.NETWORK_TYPE_EDGE: return "EDGE (2G)";
            case TelephonyManager.NETWORK_TYPE_UMTS: return "UMTS (3G)";
            case TelephonyManager.NETWORK_TYPE_HSDPA: return "HSDPA (3G)";
            case TelephonyManager.NETWORK_TYPE_HSUPA: return "HSUPA (3G)";
            case TelephonyManager.NETWORK_TYPE_HSPA: return "HSPA (3G)";
            case TelephonyManager.NETWORK_TYPE_LTE: return "LTE (4G)";
            case TelephonyManager.NETWORK_TYPE_NR: return "5G";
            default: return "Unknown (" + type + ")";
        }
    }

    private String getSimStateName(int state) {
        switch (state) {
            case TelephonyManager.SIM_STATE_READY: return "✅ Ready";
            case TelephonyManager.SIM_STATE_ABSENT: return "❌ No SIM";
            case TelephonyManager.SIM_STATE_PIN_REQUIRED: return "🔒 PIN Required";
            case TelephonyManager.SIM_STATE_PUK_REQUIRED: return "🔒 PUK Required";
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED: return "🔒 Network Locked";
            case TelephonyManager.SIM_STATE_NOT_READY: return "⏳ Not Ready";
            default: return "Unknown";
        }
    }

    private void checkPermissions() {
        String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE
        };
        List<String> list = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                list.add(perm);
            }
        }
        if (!list.isEmpty()) {
            ActivityCompat.requestPermissions(this, list.toArray(new String[0]), 100);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            tvSensor.setText("📳 Accelerometer: X=" + String.format("%.2f", x) + " Y=" + String.format("%.2f", y) + " Z=" + String.format("%.2f", z));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.release();
            camera = null;
        }
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (sensorManager != null) sensorManager.unregisterListener(this);
        handler.removeCallbacks(autoRefresh);
        unregisterReceiver(wifiReceiver);
        unregisterReceiver(bluetoothReceiver);
        unregisterReceiver(batteryReceiver);
    }

    @Override public void surfaceCreated(SurfaceHolder holder) {}
    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    @Override public void surfaceDestroyed(SurfaceHolder holder) {}
}
