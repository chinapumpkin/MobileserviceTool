package fi.oulu.tol.vgs4msc;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

final class GPSTracker implements LocationListener {
	
    private final Context mContext;
    public static final String TAG = "fi.oulu.tol.vgs4msc.GPSTracker";
         
    Location mLocation; // location
    double mLatitude; // latitude
    double mLongitude; // longitude
    private long mLocationTime = 0;
    private String mCurrentProvider;
    
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meter
 
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1; // 1 second
 
    // Declaring a Location Manager
    private LocationManager mLocationManager;
    private boolean mIsGpsEnabled;
    private boolean mIsWifiEnabled;
    private boolean mIsRunning;
    private ConnectivityManager mConMan;
    
    //Observer
    private AreaObserver mObserver;
    
    public GPSTracker(Context context) {
        this.mContext = context;
        
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mConMan = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
    
    public void start() {
        registerForLocationUpdates();
        mIsRunning = true;
    }
    
    public void stop() {
    	unregisterForLocationUpdates();
    	mIsRunning = false;
    }
    
	public void setObserver(AreaObserver obs) {
		mObserver = obs;
	}
	
	public AreaObserver getObserver(AreaObserver obs) {
		return mObserver;
	}
    
    public double getLatitude(){
        return mLatitude;
    }
     

    public double getLongitude(){
        return mLongitude;
    }
    
    public long getLocationTime() {
    	return mLocationTime;
    }
     
    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
    	if(!mIsWifiEnabled && !mIsGpsEnabled) {
    		return false;
    	} 
        return true;
    }

	@Override
	public void onLocationChanged(Location location) {
		Log.d("GPS Tracker", "onLocationChanged");
		mLocation = location;
		
		mLocationTime = mLocation.getTime();
		mLatitude = mLocation.getLatitude();
		mLongitude = mLocation.getLongitude();
		
		if(mIsRunning && mObserver != null) {
			mObserver.newLocation();
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if(mCurrentProvider.equals(provider) && status != LocationProvider.AVAILABLE) {
			unregisterForLocationUpdates();
			registerForLocationUpdates();
		} else if(provider.equals(LocationManager.NETWORK_PROVIDER)) {
			if(mCurrentProvider.equals(LocationManager.PASSIVE_PROVIDER)) {
				unregisterForLocationUpdates();
				registerForLocationUpdates();
			}
		} else if(provider.equals(LocationManager.GPS_PROVIDER)) {
			if(!mCurrentProvider.equals(LocationManager.GPS_PROVIDER)) {
				unregisterForLocationUpdates();
				registerForLocationUpdates();
			}	
		}
	}

	@Override
	public void onProviderEnabled(String provider) {
		// Better provider might have been enabled
		// Lets re-register location listeners
		
		if(provider.equals(LocationManager.GPS_PROVIDER)) {
			if(!mCurrentProvider.equals(LocationManager.GPS_PROVIDER)) {
				unregisterForLocationUpdates();
				registerForLocationUpdates();
			}
		} else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
			if(mCurrentProvider.equals(LocationManager.PASSIVE_PROVIDER)) {
				unregisterForLocationUpdates();
				registerForLocationUpdates();
			}
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// if current provider have been disabled
		// Lets re-register location listeners
		
		if(mCurrentProvider.equals(provider)) {
			unregisterForLocationUpdates();
			registerForLocationUpdates();
		}
	}
	
	private void registerForLocationUpdates() {

        if(!networkConnected()) {
        	Log.v("GPS Tracker", "GPS NOR NETWORK IS NOT AVAILABLE");
        } else {
        	if(gpsEnabled()) {
        		try {
        		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        		} catch (SecurityException e) {
        			Log.e("GPS Tracker", "Security exception for location updates!!!");
        		}
        		Log.d("GPS Tracker", "GPS Enabled");
        		if (mLocationManager != null) {
        			mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        			if(mLocation != null) {
        				mLatitude = mLocation.getLatitude();
        				mLongitude = mLocation.getLongitude();
        				mCurrentProvider = LocationManager.GPS_PROVIDER;
        			}
        		}
        	} else if (wifiEnabled()) {
        		try {
        			mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        		} catch (SecurityException e) {
        			Log.e("GPS Tracker", "Security exception for location updates!!!");
        		}
        		Log.d("GPS Tracker", "Wifi Enabled");
        		if (mLocationManager != null) {
        			mLocation = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        			if(mLocation != null) {
        				mLatitude = mLocation.getLatitude();
        				mLongitude = mLocation.getLongitude();
        				mCurrentProvider = LocationManager.PASSIVE_PROVIDER;
        			}
        		}
        	} else if (mobileEnabled()) {
        		try {
        			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        		} catch (SecurityException e) {
        			Log.e("GPS Tracker", "Security exception for location updates!!!");
        		}
        		Log.d("GPS Tracker", "Mobile Enabled");
        		if (mLocationManager != null) {
        			mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        			if(mLocation != null) {
        				mLatitude = mLocation.getLatitude();
        				mLongitude = mLocation.getLongitude();
        				mCurrentProvider = LocationManager.NETWORK_PROVIDER;
        			}
        		}
        	}
        	
        	
        }
	}
	
	private void unregisterForLocationUpdates() {
		mLocationManager.removeUpdates(this);
	}
	
	private boolean mobileEnabled() {
		NetworkInfo mobileInfo = mConMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if(mobileInfo.isConnected()) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean wifiEnabled() {
		NetworkInfo wifiInfo = mConMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		if(wifiInfo.isConnected()) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean gpsEnabled() {
		return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	private boolean networkConnected() {
		return (wifiEnabled() || mobileEnabled() || gpsEnabled());
	}

}
