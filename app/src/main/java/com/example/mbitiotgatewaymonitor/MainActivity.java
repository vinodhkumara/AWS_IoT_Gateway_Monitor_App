package com.example.mbitiotgatewaymonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;
import com.google.gson.JsonObject;

import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //static final String LOG_TAG = MainActivity.class.getCanonicalName();
    static final String LOG_TAG = "VINO_IOT: MainActivity";
    String JSON_STRING = "{\"temperature_sensor\":{\"temperature\":\"35\",\"humidity\":\"65\"}}";

    // --- Constants to modify per your configuration ---

    //Pool ID
    // ap-south-1:b092fadd-63e0-4f0b-9b85-3d2161e606fd --> [Vino Account] VinoAndroidTempIoT  --> vino_android_temp_iot
    // ap-south-1:85a0a4f8-4e50-4797-9880-fa429fa8af2a --> [Vino Account] SmartTemperature  --> smart_temp_app_policy
    // ap-south-1:66e164f1-6a52-492a-9fa9-bb34c9c5d42e --> [MBit Account] Android IoT Temperature Monitor --> IoT_Temperature_Monitor_MB_policy

    // IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com
    // [MBit Account] a2ffow0ere9y5e-ats.iot.ap-south-1.amazonaws.com
    // [Vino Account] a16ncb1mld04um-ats.iot.ap-south-1.amazonaws.com
    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a2ffow0ere9y5e-ats.iot.ap-south-1.amazonaws.com";
    // Name of the AWS IoT policy to attach to a newly created certificate
    private static final String AWS_IOT_POLICY_NAME = "IoT_Temperature_Monitor_MB_policy";

    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.AP_SOUTH_1;
    // Filename of KeyStore file on the filesystem
    private static final String KEYSTORE_NAME = "iot_keystore";
    // Password for the private key in the KeyStore
    private static final String KEYSTORE_PASSWORD = "password";
    // Certificate and key aliases in the KeyStore
    private static final String CERTIFICATE_ID = "default";

    EditText txtSubcribe;
    EditText txtTopic;
    EditText txtMessage;

    TextView tvLastMessage;
    TextView tvClientId;
    TextView tvStatus;
    TextView tvTemperature;
    TextView tvHumidity;
    TextView tvAltitude;
    TextView tvPressure;
    TextView tvAccelerometer;

    Button btnConnect;

    Spinner subSpinner,pubSpinner;

    AWSIotClient mIotAndroidClient;
    AWSIotMqttManager mqttManager;
    String clientId;
    String keystorePath;
    String keystoreName;
    String keystorePassword;

    KeyStore clientKeyStore = null;
    String certificateId;

    //Toast mAppToast = new Toast(getApplicationContext());
    List<String> mTimeHistoryList = new ArrayList<String>();
    List<String> mPressureHistoryList = new ArrayList<String>();
    List<String> mAccelHistoryList = new ArrayList<String>();
    List<String> mAltitudeHistoryList = new ArrayList<String>();
    List<String> mTempHistoryList = new ArrayList<String>();
    List<String> mHumidityHistoryList = new ArrayList<String>();


    public void connectClick(final View view) {
        Log.d(LOG_TAG, "clientId = " + clientId);

        try {
            mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status,
                                            final Throwable throwable) {
                    Log.d(LOG_TAG, "Status = " + String.valueOf(status));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText(status.toString());
                            //Toast.makeText(getApplicationContext(), "Connected to Server !!", Toast.LENGTH_LONG).show();
                            if (throwable != null) {
                                Log.e(LOG_TAG, "Connection error in connecting .. ", throwable);
                            }
                        }
                    });
                }
            });
            Log.d(LOG_TAG, "Connection is success !!");
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error.", e);
            Toast.makeText(getApplicationContext(), "Error in Connecting to Server...Try Again !!", Toast.LENGTH_LONG).show();
            tvStatus.setText("Error! " + e.getMessage());
        }
    }

    public void subscribeClick(final View view) {
        final String topic = subSpinner.getSelectedItem().toString(); //txtSubcribe.getText().toString();

        Log.d(LOG_TAG, "topic = " + topic);

        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String message = new String(data, "UTF-8");
                                        Log.d(LOG_TAG, "Message arrived:");
                                        Log.d(LOG_TAG, "   Topic: " + topic);
                                        Log.d(LOG_TAG, " Message: " + message);

                                        parseTheData(message);
                                    } catch (UnsupportedEncodingException e) {
                                        Log.e(LOG_TAG, "Message encoding error.", e);
                                    }
                                }
                            });
                        }
                    });
            Toast.makeText(getApplicationContext(), "Successfully subscribed the topic !!", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "Subscription is success !!");
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Subscribe error ..Check the connection!!", Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "Subscription error.", e);
        }
    }

    public void unSubscribeClick (final View view) {

        final String topic = subSpinner.getSelectedItem().toString(); //txtSubcribe.getText().toString();

        Log.d(LOG_TAG, "topic = " + topic);

        try {
            mqttManager.unsubscribeTopic(topic);
            Toast.makeText(getApplicationContext(), "Successfully UnSubscribed the topic !!", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "UnSubscription is success !!");
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "UnSubscribe error ..Check the connection!!", Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "UnSubscription error.", e);
        }
    }

    public void publishClick(final View view) {
        final String topic = pubSpinner.getSelectedItem().toString(); //txtTopic.getText().toString();
        final String msg = txtMessage.getText().toString();

        try {
            mqttManager.publishString(msg, topic, AWSIotMqttQos.QOS0);
            Toast.makeText(getApplicationContext(), "Successfully published the topic !!", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "Publish is success !!");

        } catch (Exception e) {
            Log.e(LOG_TAG, "Publish error.", e);
            Toast.makeText(getApplicationContext(), "Publish error ..Check the Connection !!", Toast.LENGTH_LONG).show();
        }
    }

    public void historyClick(final View view) {
        Log.d(LOG_TAG, "historyClick");
        Intent intent = new Intent(MainActivity.this, HistoryScreen.class);

        intent.putExtra("time_list", (Serializable) mTimeHistoryList);
        intent.putExtra("pressure_list", (Serializable) mPressureHistoryList);
        intent.putExtra("accel_list", (Serializable) mAccelHistoryList);
        intent.putExtra("altitude_list", (Serializable) mAltitudeHistoryList);        ;
        intent.putExtra("temp_list", (Serializable) mTempHistoryList);
        intent.putExtra("humidity_list", (Serializable) mHumidityHistoryList);

        //intent.putExtras(bundle);
        startActivity(intent);
    }

    public void disconnectClick(final View view) {
        Log.d(LOG_TAG, "disconnectClick");
        try {
            mqttManager.disconnect();
            Log.d(LOG_TAG, "Disconnect is success !!");
            Toast.makeText(getApplicationContext(), "Disconnected successfully with server !!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Disconnect error.", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "OnCreate");
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /*View mToastView = mAppToast.getView();
        mToastView.setBackgroundColor(Color.BLACK);*/

        //txtMessage = findViewById(R.id.txtMessage);
        tvStatus = findViewById(R.id.tvStatus);

        tvPressure = findViewById(R.id.pressure_text);
        tvAccelerometer = findViewById(R.id.accel_text);
        tvAltitude = findViewById(R.id.altitude_text);
        tvTemperature = findViewById(R.id.temp_text);
        tvHumidity = findViewById(R.id.humidity_text);

        btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setEnabled(false);

        //Spinner initialiazation
        subSpinner = (Spinner) findViewById(R.id.sub_spinner);
        //pubSpinner = (Spinner) findViewById(R.id.pub_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> subSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.subscribe_topics, android.R.layout.simple_spinner_item);
        /*ArrayAdapter<CharSequence> pubSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.publish_topics, android.R.layout.simple_spinner_item); */
        // Specify the layout to use when the list of choices appears
        subSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //pubSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        subSpinner.setAdapter(subSpinnerAdapter);
        //pubSpinner.setAdapter(pubSpinnerAdapter);

        // MQTT client IDs are required to be unique per AWS IoT account.
        // This UUID is "practically unique" but does not _guarantee_
        // uniqueness.
        clientId = UUID.randomUUID().toString();
        //tvClientId.setText(clientId);

        //ap-south-1:65ca81d9-a836-4912-8403-65b8b3536437
        //ap-south-1:85a0a4f8-4e50-4797-9880-fa429fa8af2a
        // Initialize the AWS Cognito credentials provider
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                Log.d(LOG_TAG, "OnCreate");
                initIoTClient();
            }

            @Override
            public void onError(Exception e) {
                Log.e(LOG_TAG, "onError: ", e);
            }
        });
    }

    void initIoTClient() {
        Log.d(LOG_TAG, "initIoTClient");
        Region region = Region.getRegion(MY_REGION);

        // MQTT Client
        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT);

        // Set keepalive to 10 seconds.  Will recognize disconnects more quickly but will also send
        // MQTT pings every 10 seconds.
        mqttManager.setKeepAlive(10);

        // Set Last Will and Testament for MQTT.  On an unclean disconnect (loss of connection)
        // AWS IoT will publish this message to alert other clients.
        AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament("my/lwt/topic",
                "Android client lost connection", AWSIotMqttQos.QOS0);
        mqttManager.setMqttLastWillAndTestament(lwt);

        // IoT Client (for creation of certificate if needed)
        mIotAndroidClient = new AWSIotClient(AWSMobileClient.getInstance());
        mIotAndroidClient.setRegion(region);

        keystorePath = getFilesDir().getPath();
        Log.d(LOG_TAG, "keystorePath Path : " + keystorePath);
        keystoreName = KEYSTORE_NAME;
        keystorePassword = KEYSTORE_PASSWORD;
        certificateId = CERTIFICATE_ID;

        // To load cert/key from keystore on filesystem
        try {
            Log.d(LOG_TAG, "AWSIotKeystoreHelper try");
            if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, keystoreName)) {
                if (AWSIotKeystoreHelper.keystoreContainsAlias(certificateId, keystorePath,
                        keystoreName, keystorePassword)) {
                    Log.i(LOG_TAG, "Certificate " + certificateId
                            + " found in keystore - using for MQTT.");
                    // load keystore from file into memory to pass on connection
                    clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                            keystorePath, keystoreName, keystorePassword);
                    /* initIoTClient is invoked from the callback passed during AWSMobileClient initialization.
                    The callback is executed on a background thread so UI update must be moved to run on UI Thread. */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnConnect.setEnabled(true);
                        }
                    });
                } else {
                    Log.i(LOG_TAG, "Key/cert " + certificateId + " not found in keystore.");
                }
            } else {
                Log.i(LOG_TAG, "Keystore " + keystorePath + "/" + keystoreName + " not found.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "An error occurred retrieving cert/key from keystore.", e);
        }

        if (clientKeyStore == null) {
            Log.i(LOG_TAG, "Cert/key was not found in keystore - creating new key and certificate.");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(LOG_TAG, "Thread to create key and certificate");
                        // Create a new private key and certificate. This call
                        // creates both on the server and returns them to the
                        // device.
                        CreateKeysAndCertificateRequest createKeysAndCertificateRequest =
                                new CreateKeysAndCertificateRequest();
                        createKeysAndCertificateRequest.setSetAsActive(true);
                        final CreateKeysAndCertificateResult createKeysAndCertificateResult;
                        createKeysAndCertificateResult =
                                mIotAndroidClient.createKeysAndCertificate(createKeysAndCertificateRequest);
                        Log.i(LOG_TAG,
                                "Cert ID: " +
                                        createKeysAndCertificateResult.getCertificateId() +
                                        " created.");

                        // store in keystore for use in MQTT client
                        // saved as alias "default" so a new certificate isn't
                        // generated each run of this application
                        AWSIotKeystoreHelper.saveCertificateAndPrivateKey(certificateId,
                                createKeysAndCertificateResult.getCertificatePem(),
                                createKeysAndCertificateResult.getKeyPair().getPrivateKey(),
                                keystorePath, keystoreName, keystorePassword);

                        // load keystore from file into memory to pass on
                        // connection
                        clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                                keystorePath, keystoreName, keystorePassword);

                        // Attach a policy to the newly created certificate.
                        // This flow assumes the policy was already created in
                        // AWS IoT and we are now just attaching it to the
                        // certificate.
                        AttachPrincipalPolicyRequest policyAttachRequest =
                                new AttachPrincipalPolicyRequest();
                        policyAttachRequest.setPolicyName(AWS_IOT_POLICY_NAME);
                        policyAttachRequest.setPrincipal(createKeysAndCertificateResult
                                .getCertificateArn());
                        //mIotAndroidClient.attachPrincipalPolicy(policyAttachRequest);
                        mIotAndroidClient.attachPrincipalPolicy(policyAttachRequest);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(LOG_TAG, "Connect Btn enable");
                                btnConnect.setEnabled(true);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(LOG_TAG,
                                "Exception occurred when generating new private key and certificate.",
                                e);
                    }
                }
            }).start();
        }
    }

    /* {
            "deviceid" : "iot123",
            "temperature" : 54.98,
            "humidity" : 32.43,
            "coords" : {
                "latitude" : 47.615694,
                 "longitude" : -122.3359976
            }
        }
    */
    public void displayData(String msg) {
        Log.d(LOG_TAG, "displayData");
        String mTempData = null;
        String mCoords = null;
        try {
            // get JSONObject from JSON file
            JSONObject obj = new JSONObject(msg);
            // fetch JSONObject named temperature
            //JSONObject employee = obj.getJSONObject("temperature");
            //JSONObject temp = obj.get
            // get temperature value
            mTempData = obj.getString("temperature");
            Log.d(LOG_TAG, "displayData mTempData = " + mTempData);
            JSONObject coodsobj = obj.getJSONObject("coords");
            mCoords = coodsobj.getString("latitude");
            Log.d(LOG_TAG, "displayData mCoords = " + mCoords);
            //tvLastMessage.setText(mTempData);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void addToHistoryList(String msg) {
        Log.d(LOG_TAG, "addToHistoryList");
        String mHistoryTemp = null;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mDateFormat = new SimpleDateFormat("dd-MM-yyyy  'at'  HH:mm:ss");
        mHistoryTemp = mDateFormat.format(calendar.getTime()) + " : " + msg;
        Log.d(LOG_TAG, "addToHistoryList mHistoryTemp :" + mHistoryTemp);
        mTempHistoryList.add(0,mHistoryTemp);
        //printHistoryList(mTempHistoryList);
    }

    void printHistoryList(List mHistoryList) {
        Log.d(LOG_TAG, "printHistoryList");
        /*for (String list : mHistoryList) {
            Log.d(LOG_TAG, "List :" + mHistoryList .get(i));
            //System.out.println(mHistoryList .get(i));
        } */
        ListIterator<String> lItr = mHistoryList.listIterator();
        // hasNext() returns true if the list has more elements
        while (lItr.hasNext())
        {
            // next() returns the next element in the iteration
            Log.d(LOG_TAG, "List :" + lItr.next());
            //System.out.println(lItr.next());
        }
    }

    public void parseTheData(String msg) {
        Log.d(LOG_TAG, "parseTheData");
        String mTemperature = null;
        String mHumidity = null;
        String mPressure = null;
        String mAltitude = null;
        String mAccel = null;
        String mTime = null;

        String xMagnitude = null;
        String yMagnitude = null;
        String zMagnitude = null;

        /*Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mDateFormat = new SimpleDateFormat("dd-MM-yyyy  'at'  HH:mm:ss");
        mTime = mDateFormat.format(calendar.getTime());
        Log.d(LOG_TAG, "parseTheData mTime = " + mTime);*/

        /*try {
            // get JSONObject from JSON file
            JSONObject obj = new JSONObject(msg);

            //get the data from JSON object
            mTime = obj.getString("Eventtimestamp");
            Log.d(LOG_TAG, "parseTheData mTime = " + mTime);
            mPressure = obj.getString("Pressure");
            Log.d(LOG_TAG, "parseTheData mPressure = " + mPressure);
            mAccel = obj.getString("xMagnitude") + "x, " + obj.getString("yMagnitude") + "y, " + obj.getString("zMagnitude") + "z";
            Log.d(LOG_TAG, "parseTheData mAccel = " + mAccel);
            mAltitude = obj.getString("Altitude");
            Log.d(LOG_TAG, "parseTheData mAltitude = " + mAltitude);
            mTemperature = obj.getString("Temperature");
            Log.d(LOG_TAG, "parseTheData mTempData = " + mTemperature);
            mHumidity = obj.getString("Humidity");
            Log.d(LOG_TAG, "parseTheData mHumidity = " + mHumidity);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(LOG_TAG, "parseTheData : adding the data to list");
        mTimeHistoryList.add(0,mTime);
        mPressureHistoryList.add(0,mPressure);
        mAccelHistoryList.add(0,mAccel);
        mAltitudeHistoryList.add(0,mAltitude);
        mTempHistoryList.add(0,mTemperature);
        mHumidityHistoryList.add(0,mHumidity); */

        /*** Latest Implementation **/
        try {
            JSONObject mainObj= new JSONObject(msg);
            JSONArray jsonarray = mainObj.getJSONArray("sensor_data");
            int lenght = jsonarray.length();

            Log.d(LOG_TAG, "JSON Array length:" + lenght);

            //loop through each object
            for (int index =0; index < jsonarray.length(); index++) {
                Log.d(LOG_TAG, "****INDEX:****" + index);

                JSONObject jsonParameterObj = jsonarray.getJSONObject(index);

                if (jsonParameterObj.has("Eventtimestamp")) {
                    mTime = jsonParameterObj.getString("Eventtimestamp");
                } else {
                    mTime = "None";
                }
                Log.d(LOG_TAG, "parseTheData mTime = " + mTime);

                if (jsonParameterObj.has("xMagnitude")) {
                    xMagnitude = jsonParameterObj.getString("xMagnitude");
                } else {
                    xMagnitude = "None";
                }
                Log.d(LOG_TAG, "parseTheData xMagnitude = " + xMagnitude);

                if (jsonParameterObj.has("yMagnitude")) {
                    yMagnitude = jsonParameterObj.getString("yMagnitude");
                } else {
                    yMagnitude = "None";
                }
                Log.d(LOG_TAG, "parseTheData yMagnitude = " + yMagnitude);

                if (jsonParameterObj.has("zMagnitude")) {
                    zMagnitude = jsonParameterObj.getString("zMagnitude");
                } else {
                    mTime = "None";
                }
                Log.d(LOG_TAG, "parseTheData zMagnitude = " + zMagnitude);

                mAccel = xMagnitude + "x, " + yMagnitude + "y, " + zMagnitude + "z";

                Log.d(LOG_TAG, "parseTheData mAccel = " + mAccel);

                if (jsonParameterObj.has("Pressure")) {
                    mPressure = jsonParameterObj.getString("Pressure");
                } else {
                    mPressure = "None";
                }
                Log.d(LOG_TAG, "parseTheData mPressure = " + mPressure);

                if (jsonParameterObj.has("Altitude")) {
                    mAltitude = jsonParameterObj.getString("Altitude");
                } else {
                    mAltitude = "None";
                }
                Log.d(LOG_TAG, "parseTheData mAltitude = " + mAltitude);

                if (jsonParameterObj.has("Temperature")) {
                    mTemperature = jsonParameterObj.getString("Temperature");
                } else {
                    mTemperature = "None";
                }
                Log.d(LOG_TAG, "parseTheData mTempData = " + mTemperature);

                if (jsonParameterObj.has("Humidity")) {
                    mHumidity = jsonParameterObj.getString("Humidity");
                } else {
                    mHumidity = "None";
                }
                Log.d(LOG_TAG, "parseTheData mHumidity = " + mHumidity);

                Log.d(LOG_TAG, "parseTheData : adding the data to list");
                mTimeHistoryList.add(0,mTime);
                mPressureHistoryList.add(0,mPressure);
                mAccelHistoryList.add(0,mAccel);
                mAltitudeHistoryList.add(0,mAltitude);
                mTempHistoryList.add(0,mTemperature);
                mHumidityHistoryList.add(0,mHumidity);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Utils.printDataList(mTimeHistoryList,mPressureHistoryList,mAccelHistoryList,mAltitudeHistoryList,mTempHistoryList,mHumidityHistoryList);

        displayAllData (mPressure,mAccel,mAltitude,mTemperature,mHumidity);
    }

    private void displayAllData (String mPress, String mAcce, String mAlti, String mTemp, String mHum) {
        Log.d(LOG_TAG, "displayTheData");

        tvPressure.setText(mPress + " hPa");
        tvAccelerometer.setText(mAcce);
        tvAltitude.setText(mAlti + " m");
        tvTemperature.setText(mTemp + " \u2103");
        tvHumidity.setText(mHum + " %RH");
    }

    /*private void printDataList (List mTimeHistoryList, List mPressureHistoryList, List mAccelHistoryList,
                                List mAltitudeHistoryList, List mTempHistoryList, List mHumidityHistoryList) {
        Log.d(LOG_TAG, "printDataList");

        ListIterator<String> lItr1 = mTimeHistoryList.listIterator();
        // hasNext() returns true if the list has more elements
        while (lItr1.hasNext())
        {
            // next() returns the next element in the iteration
            Log.d(LOG_TAG, "Time List :" + lItr1.next());
            //System.out.println(lItr.next());
        }

        ListIterator<String> lItr2 = mPressureHistoryList.listIterator();
        // hasNext() returns true if the list has more elements
        while (lItr2.hasNext())
        {
            // next() returns the next element in the iteration
            Log.d(LOG_TAG, "Pressure List :" + lItr2.next());
            //System.out.println(lItr.next());
        }

        ListIterator<String> lItr3 = mAccelHistoryList.listIterator();
        // hasNext() returns true if the list has more elements
        while (lItr3.hasNext())
        {
            // next() returns the next element in the iteration
            Log.d(LOG_TAG, "Accel List :" + lItr3.next());
            //System.out.println(lItr.next());
        }

        ListIterator<String> lItr4 = mAltitudeHistoryList.listIterator();
        // hasNext() returns true if the list has more elements
        while (lItr4.hasNext())
        {
            // next() returns the next element in the iteration
            Log.d(LOG_TAG, "Altitude List :" + lItr4.next());
            //System.out.println(lItr.next());
        }

        ListIterator<String> lItr5 = mTempHistoryList.listIterator();
        // hasNext() returns true if the list has more elements
        while (lItr5.hasNext())
        {
            // next() returns the next element in the iteration
            Log.d(LOG_TAG, "Temp List :" + lItr5.next());
            //System.out.println(lItr.next());
        }

        ListIterator<String> lItr6 = mHumidityHistoryList.listIterator();
        // hasNext() returns true if the list has more elements
        while (lItr6.hasNext())
        {
            // next() returns the next element in the iteration
            Log.d(LOG_TAG, "Humidity List :" + lItr6.next());
            //System.out.println(lItr.next());
        }
    } */
}

