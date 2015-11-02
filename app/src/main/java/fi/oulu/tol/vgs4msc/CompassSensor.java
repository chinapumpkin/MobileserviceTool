package fi.oulu.tol.vgs4msc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

public class CompassSensor implements SensorEventListener {
	
	public static final int SCREEN_OFF_RECEIVER_DELAY = 5000;
	public static final String TAG = "vgs4msc.CompassSensor";
	
	private float mDegrees;
	private AreaObserver mObserver;
	
	private SensorManager mSensorManager;
	private Sensor mSensorAccelerometer;
	private Sensor mSensorMagneticField;
	
	private long lastTimeStamp = 0;
	private float lastDegrees = 0;
	private boolean firstTime = true;
	private int minDifferenceValue = 4;
	
	private float[] mValuesAccelerometer;
	private float[] mValuesMagneticField;
	
	private float[] matrixR;
	private float[] matrixI;
	private float[] matrixValues;	
	
	private Context mContext;
	
	BroadcastReceiver mReceiver;
	
	CompassSensor(Context context) {
		this.mContext = context;

		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
		mValuesAccelerometer = new float[3];
		mValuesMagneticField = new float[3];
		
		matrixR = new float[9];
		matrixI = new float[9];
		matrixValues = new float[3];
		
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
                filter.addAction(Intent.ACTION_SCREEN_OFF);
                mReceiver = new ScreenReceiver();
                mContext.registerReceiver(mReceiver, filter);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
	        
	        if((System.currentTimeMillis()-lastTimeStamp) > 50 || firstTime) {
	                switch(event.sensor.getType()){
                        case Sensor.TYPE_ACCELEROMETER:
                           for(int i =0; i < 3; i++){
                                   mValuesAccelerometer[i] = event.values[i];
                           }
                        break;
                        case Sensor.TYPE_MAGNETIC_FIELD:
                                for(int i =0; i < 3; i++){
                                        mValuesMagneticField[i] = event.values[i];
                                }
                        break;
                  }
                    
                  boolean success = SensorManager.getRotationMatrix(matrixR, matrixI, mValuesAccelerometer, mValuesMagneticField);
                    
                  if(success){
                          SensorManager.getOrientation(matrixR, matrixValues);
                          
                          
                          float azimuthInRadians = matrixValues[0];
                          float azimuthInDegress = (float)Math.toDegrees(azimuthInRadians);
                          if (azimuthInDegress < 0.0f) {
                                  azimuthInDegress += 360.0f;
                          }
                          
                          if(azimuthInDegress > (lastDegrees+minDifferenceValue) || azimuthInDegress < (lastDegrees-minDifferenceValue)) {
                                  lastDegrees = mDegrees;
                                  mDegrees = azimuthInDegress;
                                  lastTimeStamp = System.currentTimeMillis();
                                  if(mObserver != null){
                                          mObserver.newDegree(); 
                                  }
                          } else if (firstTime) {
                                  lastDegrees = azimuthInDegress;
                                  mDegrees = azimuthInDegress;
                                  firstTime = false;
                                  lastTimeStamp = System.currentTimeMillis();
                                  
                                  if(mObserver != null){
                                          mObserver.newDegree(); 
                                  }
                          }
                         
                  }
	        }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//NOT USED
	}
	
	public void setObserver(AreaObserver obs) {
		mObserver = obs;
	}
	
	public void stop() {
		unregisterListeners();
		mContext.unregisterReceiver(mReceiver);
	}
	
	public void start() {
		registerListeners();
	}
	
	public AreaObserver getObserver(AreaObserver obs) {
		return mObserver;
	}
	
	public float getDegrees() {
		return mDegrees;
	}
	
	public class ScreenReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				//Unregister and register listener after screen goes off.		
				Runnable runnable = new Runnable() {
					public void run() {
						Log.i(TAG, "Runnable executing.");
						unregisterListeners();
						registerListeners();
					}
				};
				
				new Handler().postDelayed(runnable, SCREEN_OFF_RECEIVER_DELAY);
			}             
		}
	};

   public void unregisterListeners(){
         mSensorManager.unregisterListener(this);
   }
   
   public void registerListeners() {
	   mSensorManager.registerListener(this, mSensorAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
	   mSensorManager.registerListener(this, mSensorMagneticField,SensorManager.SENSOR_DELAY_NORMAL);
   }

}
