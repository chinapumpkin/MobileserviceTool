/*
 * Author: Jari Tervonen <jjtervonen@gmail.com> 2013-
 * 
 * Mostly adapted from Android API sample's BluetoothChat:
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uni.oulu.firstprotocol;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;

public class HmdBtCommunicator implements HmdCommunicator {

	public static final long[] DIR_GOAL_VAL = {0x80208220,0x0};
	public static final long[] DIR_START_VAL = {0x80ffffff,0x0};
	public static final long[] DIR_RIGHT_VAL = {0x800a0000,0x0};
	public static final long[] DIR_LEFT_VAL = {0x80000802,0x0};
	public static final long[] DIR_UP_VAL = {0x80008200,0x0};
	public static final long[] DIR_DOWN_VAL = {0x80200020,0x0};
	public static final long[] DIR_LEFT_DOWN_VAL = {0x80000022,0x0};
	public static final long[] DIR_RIGHT_DOWN_VAL = {0x80280000,0x0};
	public static final long[] DIR_LEFT_UP_VAL = {0x80000a00,0x0};
	public static final long[] DIR_RIGHT_UP_VAL = {0x80028000,0x0};
	public static final long[] DIR_NONE_VAL = {0x0};
	
	public static final String DIR_GOAL_KEY = "GOAL";
	public static final String DIR_START_KEY = "START";
	public static final String DIR_RIGHT_KEY = "RIGHT";
	public static final String DIR_LEFT_KEY = "LEFT";
	public static final String DIR_UP_KEY = "UP";
	public static final String DIR_DOWN_KEY = "DOWN";
	public static final String DIR_LEFT_DOWN_KEY = "LEFT_DOWN";
	public static final String DIR_RIGHT_DOWN_KEY = "RIGHT_DOWN";
	public static final String DIR_LEFT_UP_KEY = "LEFT_UP";
	public static final String DIR_RIGHT_UP_KEY = "RIGHT_UP";
	public static final String DIR_NONE_KEY = "NONE";
	
    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    public static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    public static final int REQUEST_ENABLE_BT = 3;
    
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    public static final int SEND_NEXT_DATA = 6;
    public static final int DELAYED_SEND = 7;
    
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TAG = "uni.oulu.firstprotocol";

    private String lgAddress = "00:18:B2:02:51:78";
    private boolean paired = false;
    
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothChatService mChatService = null;
	
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
	
	private boolean bConnected = false;
	private long last_data = 0x0;
	private String last_string_data = null;
	
	private int mRepeatCount=0;
	private Context mContext = null;
	
	private Hashtable<String,DirectionPattern> directions = new Hashtable<String,DirectionPattern>();
	
	public HmdBtCommunicator(Context c, Hashtable<String, DirectionPattern> dirs) {
	        mContext = c;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if (dirs != null) {
			directions = dirs;
		}
		else {
			directions.put( DIR_GOAL_KEY, new DirectionPattern(DIR_GOAL_VAL, 0,0,0));
			directions.put( DIR_START_KEY, new DirectionPattern(DIR_START_VAL, 0,0,0));
			directions.put( DIR_RIGHT_KEY, new DirectionPattern(DIR_RIGHT_VAL, 0,0,0));
			directions.put( DIR_LEFT_KEY, new DirectionPattern(DIR_LEFT_VAL, 0,0,0));
			directions.put( DIR_UP_KEY, new DirectionPattern(DIR_UP_VAL, 0,0,0));
			directions.put( DIR_DOWN_KEY, new DirectionPattern(DIR_DOWN_VAL,0,0,0));
			directions.put( DIR_LEFT_DOWN_KEY, new DirectionPattern(DIR_LEFT_DOWN_VAL, 0,0,0));
			directions.put( DIR_RIGHT_DOWN_KEY, new DirectionPattern(DIR_RIGHT_DOWN_VAL, 0,0,0));
			directions.put( DIR_LEFT_UP_KEY, new DirectionPattern(DIR_LEFT_UP_VAL, 0,0,0));
			directions.put( DIR_RIGHT_UP_KEY, new DirectionPattern(DIR_RIGHT_UP_VAL, 0,0,0));
			directions.put( DIR_NONE_KEY, new DirectionPattern(DIR_NONE_VAL, 0,0,0));
		}
	        // Register for broadcasts when a device is discovered
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                mContext.registerReceiver(mReceiver, filter);

                // Register for broadcasts when discovery has finished
                filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                mContext.registerReceiver(mReceiver, filter);
                
		mBluetoothAdapter.startDiscovery();
	}
	
	public Hashtable<String,DirectionPattern> getDirections() {
		return directions;
	}
	
	public void setDirections(Hashtable<String,DirectionPattern> d) {
		directions=d;
	}
	
	@Override
	public void doStart() {

		// If BT is not on, request that it be enabled.
	    // setupChat() will then be called during onActivityResult
	    if (!mBluetoothAdapter.isEnabled()) {
	        Log.i(TAG, "Bluetooth enabled");
	        mBluetoothAdapter.enable();
	    // Otherwise, setup the chat session
	    } else {
	        if (mChatService == null) setupChat();
	        
	    }
	    
	}
	
	@Override
	public void doResume() {
		
		if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
	}
	
	@Override
	public void doStop() {
		if (mChatService != null) mChatService.stop();
	}

	@Override
	public boolean isConnected() {
		return bConnected;
	}
	
	private void setupChat() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(mContext, mHandler);

        // Initialize the buffer for outgoing messages
        //mOutStringBuffer = new StringBuffer("");
    }
	
	public void connectDevice() {
	        BluetoothDevice mmDevice = null;
	        
	        Set<BluetoothDevice> pairedDevices =  BluetoothAdapter.getDefaultAdapter().getBondedDevices();
                for (BluetoothDevice device : pairedDevices) {
                        String name = device.getName();
                        Log.d(TAG,device.getName());
                        if (name.contains("HMD")) {
                            mmDevice = mBluetoothAdapter.getRemoteDevice(device.getAddress());
                            break;
                        }
                }
                
                // Attempt to connect to the device
                
                if (mmDevice != null) {
                        Log.d("BTBLOP", "DEVICE:"+mmDevice.toString());
                	mChatService.connect(mmDevice, false);
                }
	}
	
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // If it's already paired, skip it, because it's been listed already
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                        if(device.getName().contains("HMD")) {
                                try {
                                        if(createBond(device)) {
                                                paired = true;
                                                mBluetoothAdapter.cancelDiscovery();
                                        }
                                } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        Log.d(TAG, e.toString());
                                }
                        }
                    } else {
                            paired = true;
                    }
                // When discovery is finished, change the Activity title
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    if(paired) {
                            connectDevice();  
                    }
                            
                }
            }
        };
	
        
        public boolean createBond(BluetoothDevice btDevice)  
        throws Exception  
        { 
            Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
            Method createBondMethod = class1.getMethod("createBond");  
            Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);  
            return returnValue.booleanValue();  
        }  
	
	@Override
	public boolean sendData(long data) {
		// Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
           return false;
        }

        // Get the message bytes and tell the BluetoothChatService to write
        //byte[] send = toBytes(data);
        //mChatService.write(send);
        if (last_data != data) {
        	new SendDataTask().execute(data);
        	last_data = data;
        }
        return true;
	}
	
	
	// The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            	case MESSAGE_STATE_CHANGE:
            		switch (msg.arg1) {
            			case BluetoothChatService.STATE_CONNECTED:
            				bConnected = true;
            				break;
            			case BluetoothChatService.STATE_CONNECTING:                   
            				break;
            			case BluetoothChatService.STATE_LISTEN:
            			case BluetoothChatService.STATE_NONE:
            				break;
            		}
            		break;
            	case MESSAGE_WRITE:
            		//byte[] writeBuf = (byte[]) msg.obj;
            		// construct a string from the buffer
            		//String writeMessage = new String(writeBuf);
            		//mConversationArrayAdapter.add("Me:  " + writeMessage);
            		break;
            	case MESSAGE_READ:
            		//byte[] readBuf = (byte[]) msg.obj;
            		// construct a string from the valid bytes in the buffer
            		//String readMessage = new String(readBuf, 0, msg.arg1);
            		//mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
            		break;
            	case MESSAGE_DEVICE_NAME:
            		// save the connected device's name
            		//mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
            		//Toast.makeText(getApplicationContext(), "Connected to "
            		//               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
            		break;
            	case SEND_NEXT_DATA:
            		Long d = (Long)msg.obj;
            		new SendDataTask().execute(d);
            		break;
            } 
        }
    };
    
    private class SendDataTask extends AsyncTask <Long, Void, Void> {

		@Override
		protected Void doInBackground(Long... data) {
			byte[] send = toBytes(data[0]);
	        mChatService.write(send);
			return null;
		}
	}

	@Override
	public boolean sendData(int data) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean sendData(byte data) {
		// TODO Auto-generated method stub
		return false;
	}
	
	protected byte[] toBytes(long d) {

		byte [] ret = new byte[4];
			
		ret[0] = (byte) (d  & 0xff);
		ret[1] = (byte) ((d >>> 8) & 0xff);
		ret[2] = (byte) ((d >>> 16) & 0xff);
		ret[3] = (byte) ((d >>> 24) & 0xff);
		return ret;
	 }

	@Override
	public boolean sendData(String data) {
		if (mChatService == null)
			return false;
		
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
	           return false;
	    }
		
		/*if (data == last_string_data)
			return true;*/
		
		mHandler.removeMessages(SEND_NEXT_DATA);
		
		DirectionPattern d = directions.get(data);
		long time=0;
		if (d.getRepeats() != 0	) {
			for (int i=0; i<d.getRepeats();i++){
				for (int j=0; j<d.getDataCount();j++){
					Message m = mHandler.obtainMessage(SEND_NEXT_DATA, d.getData(j));
					//m.arg1=j;
					//m.arg2=mRepeatCount;
					
					if (j==0 && i==0)
						mHandler.sendMessage(m);
					else if ( (j & 0x1)==1) {
						time += (d.getOnDelay());
						mHandler.sendMessageDelayed(m, time);	
					}			
					else {
						time += (d.getOffDelay());
						mHandler.sendMessageDelayed(m, time);
					}
				}
			}
			
		}
		else {
			Message m = mHandler.obtainMessage(SEND_NEXT_DATA, d.getData(0));
			mHandler.sendMessage(m);
		}
		last_string_data = data;
		
	    /*if (last_data != d) {
	        new SendDataTask().execute(d);
	        last_data = d;
	    }*/
	    return true;
	}
	
}
