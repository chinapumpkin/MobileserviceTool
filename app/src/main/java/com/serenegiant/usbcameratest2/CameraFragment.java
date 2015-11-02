package com.serenegiant.usbcameratest2;
/*
 * UVCCamera
 * library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: CameraFragment.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
 * Files in the jni/libjpeg, jni/libusb, jin/libuvc, jni/rapidjson folder may have a different license, see the respective files.
*/

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.samples.com.gcmquickstart.QuickstartPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ToggleButton;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.rtsp.RtspClient;

import fi.oulu.tol.vgs4msc.MainService;
import fi.oulu.tol.vgs4msc.User;
import fi.tol.oulu.vgs4msc.R;

public class CameraFragment extends Fragment {
	/* private static final boolean DEBUG = true;    // set false when releasing
    private static final String TAG = "VGSActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    // for thread pool
    private static final int CORE_POOL_SIZE = 1;        // initial/minimum threads
    private static final int MAX_POOL_SIZE = 4;            // maximum threads
    private static final int KEEP_ALIVE_TIME = 10;        // time periods while keep the idle thread
    protected static final ThreadPoolExecutor EXECUTER
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    //server setting
    private static final String ip = "10.20.202.141";
    private static final int port = 1935;
    private static final String path = "/live/test.stream";
    private static final String username = "test";
    private static final String passwd = "1111";
    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    // need to delete the oncheckedChangeListener.
    public final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            if (isChecked && mUVCCamera == null) {
                //  CameraDialog.showDialog(MainActivity.this);
                final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(getApplicationContext(), R.xml.device_filter);
                mUSBMonitor.requestPermission((mUSBMonitor.getDeviceList(filter.get(0))).get(0));
                Log.d(TAG, "filter:" + filter.get(0));
                Log.d(TAG, "isChecked && mUVCCamera == null");
            } else if (mUVCCamera != null) {
                mUVCCamera.destroy();
                mUVCCamera = null;
                Log.d(TAG, "mUVCCamera != null");
            } else {
                Log.d(TAG, "ischecked is false");
            }
        }
    };
    //***************************
    private ToggleButton mCameraButton;
    //private Button mButtonSave;
    private ImageButton mButtonStart;
    private SurfaceView mSurfaceView;
    private TextView mTextBitrate;
    private Session mSession;
    private RtspClient mClient;
    private MSGHandler mMsgHandler;
    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            Toast.makeText(VGSActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            Log.d(TAG, "onConnect");

            if (mUVCCamera != null)
                mUVCCamera.destroy();
            mUVCCamera = new UVCCamera();
            EXECUTER.execute(new Runnable() {
                @Override
                public void run() {
                    mUVCCamera.open(ctrlBlock);
                    if (mUVCCamera != null) {
                        mUVCCamera.setPreviewDisplay(mSurfaceView);
                        mUVCCamera.startPreview();
                    } else {
                        Log.d(TAG, "mUVCCamera is null");
                    }
                }
            });
            //after connect
            mSession = SessionBuilder.getInstance()
                    .setContext(getApplicationContext())
                    .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                    .setVideoEncoder(SessionBuilder.VIDEO_H264)
                    .setSurfaceView(mSurfaceView)
                    .setCallback(VGSActivity.this)
                    .setCamera(mUVCCamera)
                    .build();

            if (mUVCCamera == null) Log.d(TAG, "mUVCCamera is null");
            // Configures the RTSP client
            mClient = new RtspClient();
            mClient.setSession(mSession);
            mClient.setCallback(VGSActivity.this);
            mSession.startPreview();
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            // XXX you should check whether the comming device equal to camera device that currently using
            if (mUVCCamera != null) {
                mUVCCamera.close();
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            mUVCCamera.destroy();
            mUVCCamera = null;
            Toast.makeText(VGSActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
        }
    };
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private FrameLayout mlayoutregister;
    private EditText mname;
    private EditText mpasswd;
    private EditText msip_address;
    private Button mregister;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
              //  mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Toast.makeText(getApplicationContext(), "Token retrieved and sent to server,you can now use this application!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "error occurred please try the application again!", Toast.LENGTH_LONG).show();
                }
            }
        };

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }


        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        mMsgHandler = new MSGHandler(this);
        mlayoutregister=(FrameLayout)findViewById(R.id.register_layout);
        mname=(EditText)findViewById(R.id.name);
         mpasswd=(EditText)findViewById(R.id.passwd);
         msip_address=(EditText)findViewById(R.id.sip_address);
        mregister=(Button)findViewById(R.id.register);
        if (sharedPreferences.getString("name",null)!=null) mlayoutregister.setVisibility(View.GONE);
        mname.setText(User.getName());
        mpasswd.setText(User.getPasswd());
        msip_address.setText(User.getSip_address());

        mCameraButton = (ToggleButton) findViewById(R.id.camera_button);

        Log.d(TAG, "onCreated");
        mButtonStart = (ImageButton) findViewById(R.id.start);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mTextBitrate = (TextView) findViewById(R.id.bitrate);

        mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mButtonStart.setOnClickListener(this);
        mregister.setOnClickListener(this);
       /*
        if (mUSBMonitor == null) {
            mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
           // final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(this, R.xml.device_filter);
            final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(getApplicationContext(), R.xml.device_filter);
            Log.d(TAG,"filter:"+filter.get(0));
            mUSBMonitor.requestPermission((mUSBMonitor.getDeviceList(filter.get(0))).get(0));
            Log.d(TAG,"mUSBMonitor == null");
        }
*//*
	Log.d(TAG, "aaaa");
	//libstreaming
	mSurfaceView.getHolder().addCallback(this);


}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 *//*
	private boolean checkPlayServices() {
		GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (apiAvailability.isUserResolvableError(resultCode)) {
				apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
						.show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.start:
				toggleStream();
				break;
			case R.id.register:
				register();


		}
	}
	public void register(){
		// We save the content user inputs in Shared Preferences
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(VGSActivity.this);
		SharedPreferences.Editor editor = mPrefs.edit();
		User.setName(mname.getText().toString());
		User.setPasswd(mpasswd.getText().toString());
		User.setSip_address(msip_address.getText().toString());
		editor.putString("name", mname.getText().toString());
		editor.putString("passwd", mpasswd.getText().toString());
		editor.putString("sip_address", msip_address.getText().toString());
		editor.commit();
		// we send it to the server
		//mMsgHandler.messageToSend(mname.getText().toString(),mpasswd.getText().toString(),msip_address.getText().toString());
		mMsgHandler.messageToSend(User.getName(),User.getPasswd(),User.getSip_address());
		Log.d(TAG, "send register message to server");
		Intent intent = new Intent(this, MainService.class);
		startService(intent);
		mlayoutregister.setVisibility(View.GONE);
	}
	@Override
	public void onDestroy() {
		if (mUVCCamera != null) {
			mUVCCamera.destroy();
			mUVCCamera = null;
		}
		if (mUSBMonitor != null) {
			mUSBMonitor.destroy();
			mUSBMonitor = null;
		}
		mCameraButton = null;
		super.onDestroy();
		mClient.release();
		mSession.release();
		mSurfaceView.getHolder().removeCallback(this);
	}


	private void enableUI() {
		mButtonStart.setEnabled(true);

	}

	// Connects/disconnects to the RTSP server and starts/stops the stream
	public void toggleStream() {
		Log.d(TAG, "toggleStream");
		if (!mClient.isStreaming() && mUVCCamera != null) {
			mClient.setServerAddress(ip, port);
			mClient.setCredentials(username, passwd);
			mClient.setStreamPath(path);
			mClient.startStream();

		} else {
			// Stops the stream and disconnects from the RTSP server
			mClient.stopStream();

		}
	}

	private void logError(final String msg) {
		final String error = (msg == null) ? "Error unknown" : msg;
		// Displays a popup to report the eror to the user
		AlertDialog.Builder builder = new AlertDialog.Builder(VGSActivity.this);
		builder.setMessage(msg).setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onBitrateUpdate(long bitrate) {
		mTextBitrate.setText("" + bitrate / 1000 + " kbps");
	}

	@Override
	public void onSessionConfigured() {

	}

	@Override
	public void onSessionStarted() {
		enableUI();
		mButtonStart.setImageResource(R.drawable.ic_switch_video_active);
	}

	@Override
	public void onSessionStopped() {
		enableUI();
		mButtonStart.setImageResource(R.drawable.ic_switch_video);
	}

	@Override
	public void onSessionError(int reason, int streamType, Exception e) {
		switch (reason) {
			case Session.ERROR_CAMERA_ALREADY_IN_USE:
				break;
			case Session.ERROR_INVALID_SURFACE:
				break;
			case Session.ERROR_STORAGE_NOT_READY:
				break;
			case Session.ERROR_CONFIGURATION_NOT_SUPPORTED:
				e.printStackTrace();
				return;
			case Session.ERROR_OTHER:
				break;
		}
		if (e != null) {
			logError(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void onPreviewStarted() {

	}

	@Override
	public void onRtspUpdate(int message, Exception e) {
		switch (message) {
			case RtspClient.ERROR_CONNECTION_FAILED:
			case RtspClient.ERROR_WRONG_CREDENTIALS:
				enableUI();
				logError(e.getMessage());
				e.printStackTrace();
				break;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		/** this is comment for test need to recovery **/
		// mSession.startPreview();
	/*
	}


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mClient.stopStream();
	}

	@Override
	public void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
				new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
		mUSBMonitor.register();
		if (mUVCCamera != null)
			mUVCCamera.startPreview();
	}

	@Override
	public void onPause() {

		super.onPause();
		mUSBMonitor.unregister();
		if (mUVCCamera != null) {
			mUVCCamera.stopPreview();
		}
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
	}

*/
}
