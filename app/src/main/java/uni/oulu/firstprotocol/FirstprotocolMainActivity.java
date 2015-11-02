package uni.oulu.firstprotocol;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import android.content.Context;
import android.util.Log;

public class FirstprotocolMainActivity {
        
    public static final String TAG = "uni.oulu.firstprotocol";
    int blink_time=3;
    int [] values = new int[14];
    int brightness = 0;
    double frequency=1;
    String LogString="";
    boolean started = false;
    private Hashtable<String,DirectionPattern> directions = new Hashtable<String,DirectionPattern>();
    private HmdBtCommunicator mBtCommunicator;
    
    public FirstprotocolMainActivity(Context context) {
            mBtCommunicator = new HmdBtCommunicator(context, null);
            directions =  mBtCommunicator.getDirections();
    }

    public void start() {
    	if (mBtCommunicator != null)
    		mBtCommunicator.doStart();
    	        started = true; 
    }
    
    public void resume() {
    	if (mBtCommunicator != null)
    		mBtCommunicator.doResume();
    	        started = true;
    }
    
    public void stop() {
    	if (mBtCommunicator != null)
    		mBtCommunicator.doStop();
    	        started = false;
    }
    
    public boolean started() {
            return started;
    }

    // GREY = 0, RED = 1, GREEN = 2, YELLOW = 3
    // Brightness 1-15
    // blink_time 3 or 5
    // frequency 1, 1.5, 2
    public void sendDirections(int lValues[], int bright, int blkTime, double freq) {
        values = lValues;
    	blink_time = blkTime;
    	brightness = bright;
    	frequency = freq;

    	SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
        Date now = new Date();
        LogString=LogString+"_"+ compileLedValues(values, brightness)+","+frequency+","+blink_time+":"+String.valueOf(sdfDate.format(now));
                Log.i(TAG, LogString);
        
        DirectionPattern p = new DirectionPattern(new long [] {
                compileLedValues(values, brightness),0x0 }, (int)((0.5/frequency)*1000),
                (int)((0.5/frequency)*1000), (int)(frequency*blink_time));
        
        directions.put(HmdBtCommunicator.DIR_GOAL_KEY, p);
        mBtCommunicator.setDirections(directions);
        mBtCommunicator.sendData(HmdBtCommunicator.DIR_GOAL_KEY);
    }

        private long compileLedValues(int [] values, int bright) {
        	long ret = 0x0;
        	for (int i=0; i<values.length; i++) {
        		ret |= (long) ((values[i]&0x3) << (i*2));
        	}
        	
        	return ret|((bright&0xf) << 28);//0xf0000000;
        }
}
