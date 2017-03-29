package furtureisnow.audio_manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Properties;
import java.util.Enumeration;
import java.lang.System;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Init Text View
        tProp = new TextView [Prop.NUM_PROP.ordinal()];
        tProp[Prop.ULTRA_SOUND_MIC.ordinal()] =
                (TextView) findViewById(R.id.tULTRA_MIC);
        tProp[Prop.ULTRA_SOUND_SPK.ordinal()] =
                (TextView) findViewById(R.id.tULTRA_SPK);
        tProp[Prop.UNPROCESSED_AUDIO_SOURCE.ordinal()] =
                (TextView) findViewById(R.id.tUNPROCESSED);
        tProp[Prop.OUT_SAMPLE_RATE.ordinal()] =
                (TextView) findViewById(R.id.tFPB);
        tProp[Prop.OUT_FRAMES_PER_BUFFER.ordinal()] =
                (TextView) findViewById(R.id.tSAMPLE_RATE);
        tStream = (TextView) findViewById(R.id.tMUSIC);
        tDevices = (TextView) findViewById(R.id.text_devices);
        tHeadset = (TextView) findViewById(R.id.text_headset);

        //Register setting content observer
        this.getApplicationContext().getContentResolver()
                .registerContentObserver(Settings.System.CONTENT_URI, true,
                        new SettingObserver(new Handler()));

        //Register intent for HEADSET PLUG
        this.getApplicationContext()
                .registerReceiver(headsetReceiver,
                        new IntentFilter(Intent.ACTION_HEADSET_PLUG));

        // String to store property results
        mDutProperties = new String [Prop.NUM_PROP.ordinal()];
        mAm = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        loadProperty(mAm);
        loadStreamVol(mAm);
        mAudioDeviceInfo = mAm.getDevices(AudioManager.GET_DEVICES_ALL);
        String temp = new String();
        for (int i = 0; i < mAudioDeviceInfo.length; i++) {
            temp = temp + "Device =  " + mAudioDeviceInfo[i].getType() +
            " Name = " + mAudioDeviceInfo[i].getProductName() +
            " SampleRate  =  " + intArrayToString(mAudioDeviceInfo[i].getSampleRates()) +
            " Channel = " + intArrayToString(mAudioDeviceInfo[i].getChannelCounts()) + "\n";

        }
        Log.d(TAG,temp);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    private final BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                parseHeadset();
        }
    };
    private void parseHeadset() {
        mAudioDeviceInfo = mAm.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        String temp = new String();
        for (int i = 0; i < mAudioDeviceInfo.length; i++) {
            boolean found = false;
            switch (mAudioDeviceInfo[i].getType()) {
                case AudioDeviceInfo.TYPE_LINE_ANALOG:
                    temp = "LINEOUT";
                    found = true;
                    break;
                case AudioDeviceInfo.TYPE_WIRED_HEADPHONES:
                    temp = "HEADPHONES";
                    found = true;
                    break;
                case AudioDeviceInfo.TYPE_WIRED_HEADSET:
                    temp = "HEADSET";
                    found = true;
                    break;
                default:
                    temp = "No device";
                    break;
            }
            if (found)
                break;
        }

        tHeadset.setText("Audio Jack devices  = " + temp);
    }
    private void route_to_earpiece(boolean state){
        if (state) {
            mAm.setMode(AudioManager.MODE_IN_COMMUNICATION);
            mAm.setSpeakerphoneOn(false);
        } else {
            mAm.setMode(AudioManager.MODE_NORMAL);
        }
    }
    public void onButtonClicked(View view){
        mAm.setStreamVolume(AudioManager.STREAM_MUSIC, 11, AudioManager.FLAG_SHOW_UI);
    }
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkbox_earpiece:
                if (checked)
                    route_to_earpiece(true);
                else
                    route_to_earpiece(false);
                break;
            // TODO: Others Check box
        }
    }

    private String intArrayToString(int sampleRate[]){
        String s  = new String();

        s = "[";
        for (int i = 0; i < sampleRate.length; i++){
            s = s + sampleRate[i];
            if (i < (sampleRate.length - 1))
                s = s + ",";
        }
        s = s + "]";
        return s;
    }

    public enum Prop {
        ULTRA_SOUND_MIC,
        ULTRA_SOUND_SPK,
        UNPROCESSED_AUDIO_SOURCE,
        OUT_SAMPLE_RATE,
        OUT_FRAMES_PER_BUFFER,
        NUM_PROP
    }
    private void loadProperty(AudioManager am) {
        for (int i = 0; i < Prop.NUM_PROP.ordinal(); i++) {
            mDutProperties[i] = am.getProperty(nameOfProperty[i]);
            Log.d(TAG, nameOfProperty[i] + " = " + mDutProperties[i]);
            if (mDutProperties[i] != null)
                tProp[i].setText(mDutProperties[i]);
            else
                tProp[i].setText("null");
        }
    }
    private void loadStreamVol(AudioManager am) {
        tStream.setText("" + am.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    private void loadAudioMode() {
        tDevices.setText("Audio Mode  = " + mAm.getMode());
    }
    class SettingObserver extends ContentObserver {
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public SettingObserver(Handler handler) {
            super(handler);
        }
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            loadStreamVol(mAm);
            loadAudioMode();
        }
    }

    private String[] nameOfProperty = {
            "android.media.property.SUPPORT_MIC_NEAR_ULTRASOUND",
            "android.media.property.SUPPORT_SPEAKER_NEAR_ULTRASOUND",
            "android.media.property.SUPPORT_AUDIO_SOURCE_UNPROCESSED",
            "android.media.property.OUTPUT_FRAMES_PER_BUFFER",
            "android.media.property.OUTPUT_SAMPLE_RATE"
    };
    private String[] namesOfStream = {
            "STREAM_VOICE_CALL",
            "STREAM_SYSTEM",
            "STREAM_RING",
            "STREAM_MUSIC",
            "STREAM_ALARM",
            "STREAM_NOTIFICATION",
            "STREAM_BLUETOOTH_SCO",
            "STREAM_ENFORCED_AUDIBLE",
            "STREAM_DTMF",
            "STREAM_TTS",
            "ACCESSIBILITY"
    };
    private TextView tProp[];
    private TextView tStream;
    private TextView tDevices;
    private TextView tHeadset;
    private static final String TAG = "dumpAudioProp";
    private String mDutProperties [];
    private AudioManager mAm;
    private AudioDeviceInfo mAudioDeviceInfo[];

}
