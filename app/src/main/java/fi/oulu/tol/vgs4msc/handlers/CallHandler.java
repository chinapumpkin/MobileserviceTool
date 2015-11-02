package fi.oulu.tol.vgs4msc.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCallStats;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneContent;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCore.EcCalibratorStatus;
import org.linphone.core.LinphoneCore.GlobalState;
import org.linphone.core.LinphoneCore.RegistrationState;
import org.linphone.core.LinphoneCore.RemoteProvisioningState;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneEvent;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneInfoMessage;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.PublishState;
import org.linphone.core.SubscriptionState;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration.AndroidCamera;

import fi.tol.oulu.vgs4msc.R;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class CallHandler implements LinphoneCoreListener, Runnable {
    //Object representing a call. created by method LinphoneCoreFactory#createLinphoneCore
    // (LinphoneCoreListener, String, String, Object).
    private LinphoneCore mLc;
    private final LinphoneCoreFactory lcFactory = LinphoneCoreFactory.instance();
    private Context mContext;
    private static final String TAG = "handlers.callhandles";
    private List<String> allowedContacts = new Vector<String>();
    private String sipAddress = "sip:xperia123@sip.linphone.org";
    private String sipPassword = "kakka333";

    private static CallHandler mInstance;
    private Timer mTimer;

    private boolean shouldRun = true;

    public CallHandler(Context context) {

        mContext = context;
        allowedContacts.add("sip:korvatap@sip.linphone.org");
        allowedContacts.add("sip:Emnu@sip.linphone.org");
        allowedContacts.add("sip:geldan2@sip.linphone.org");
        // Linphone Listener is a tag
        LinphoneCoreFactory.instance().setDebugMode(true, "Linphone Listener");

		/*LinphoneCoreFactory.instance().setDebugMode(true, "Linphone Listener");
        try {
		        String basePath = mContext.getFilesDir().getAbsolutePath();
		        copyAssetsFromPackage(basePath);
        		mLinphoneCore = LinphoneCoreFactory.instance().createLinphoneCore(this, basePath + "/.linphonerc", basePath + "/linphonerc", null, mContext);
        		initLinphoneCoreValues(basePath);
        		setUserAgent();
        		setPupilAsDefault();
        		startIterate();
        		mInstance = this;
        		mLinphoneCore.setNetworkReachable(true); // Let's assume it's true
		} catch (LinphoneCoreException e) {
		} catch (IOException e) {
		}*/
    }

    public static CallHandler getInstance() {
        return mInstance;
    }

    //Sets the user agent string used in SIP messages.In SIP, as in HTTP, the user agent may identify itself using
    //a message header field 'User-Agent', containing a text description of the software/hardware/product involved.
    //The User-Agent field is sent in request messages, which means that the receiving SIP server can see this information.
    //SIP network elements sometimes store this information,and it can be useful in diagnosing SIP compatibility problems.
    private void setUserAgent() {
        try {
            String versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
            if (versionName == null) {
                versionName = String.valueOf(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode);
            }
            mLc.setUserAgent("LinphoneAndroid", versionName);
        } catch (NameNotFoundException e) {
        }
    }

    //set pupilpro glasses camera as default
    private void setPupilAsDefault() {
        int camId = 0;
        AndroidCamera[] cameras = AndroidCameraConfiguration.retrieveCameras();
        for (AndroidCamera androidCamera : cameras) {
            Log.d(TAG, "CID: " + androidCamera.id);
            //if (androidCamera.frontFacing
            camId = androidCamera.id;
        }
        mLc.setVideoDevice(camId);
    }

    public void addAllowedContact(String c) {
        allowedContacts.add(c);
    }

    public String getSipAddress() {
        return sipAddress;
    }

    public static void copyIfNotExist(Context context, int ressourceId, String target) throws IOException {
        File lFileToCopy = new File(target);
        if (!lFileToCopy.exists()) {
            copyFromPackage(context, ressourceId, lFileToCopy.getName());
        }
    }

    public static void copyFromPackage(Context context, int ressourceId, String target) throws IOException {
        FileOutputStream lOutputStream = context.openFileOutput(target, 0);
        InputStream lInputStream = context.getResources().openRawResource(ressourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while ((readByte = lInputStream.read(buff)) != -1) {
            lOutputStream.write(buff, 0, readByte);
        }
        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }

    private void copyAssetsFromPackage(String basePath) throws IOException {
        copyIfNotExist(mContext, R.raw.oldphone_mono, basePath + "/oldphone_mono.wav");
        copyIfNotExist(mContext, R.raw.ringback, basePath + "/ringback.wav");
        copyIfNotExist(mContext, R.raw.toy_mono, basePath + "/toy_mono.wav");
        copyFromPackage(mContext, R.raw.linphonerc_default, new File(basePath + "/.linphonercc").getName());
        copyFromPackage(mContext, R.raw.linphonerc_factory, new File(basePath + "/linphonerc").getName());
        copyIfNotExist(mContext, R.raw.lpconfig, basePath + "/lpconfig.xsd");
        copyIfNotExist(mContext, R.raw.rootca, basePath + "/rootca.pem");
    }

    private void initLinphoneCoreValues(String basePath) {
        mLc.setContext(mContext);
        mLc.setRing(null);
        mLc.setRootCA(basePath + "/rootca.pem");
        mLc.setPlayFile(basePath + "/toy_mono.wav");
        mLc.setChatDatabasePath(basePath + "/linphone-history.db");
        int availableCores = Runtime.getRuntime().availableProcessors();
        mLc.setCpuCount(availableCores);
    }

    public List<String> getAllowedContacts() {
        return allowedContacts;
    }

    @Override
    public void run() {
        startLin();

    }

    public void startLin() {
        // AndroidCamera[] currentCameras = AndroidCameraConfiguration.retrieveCameras();
        try {
            try {

                String basePath = mContext.getFilesDir().getAbsolutePath();
                copyAssetsFromPackage(basePath);
                //mLc = LinphoneCoreFactory.instance().createLinphoneCore(this, mContext);
                mLc = LinphoneCoreFactory.instance().createLinphoneCore(this, basePath + "/.linphonercc", basePath + "/linphonerc", null, mContext);
                initLinphoneCoreValues(basePath);
                setUserAgent();
                //setPupilAsDefault();


                LinphoneAddress address = lcFactory.createLinphoneAddress(sipAddress);

                mLc.enableVideo(true, true);


                LinphoneAuthInfo info;
                //The LinphoneProxyConfig object represents a proxy configuration to be used by the LinphoneCore object.
                LinphoneProxyConfig proxyCfg;
                String username = address.getUserName();
                String domain = address.getDomain();

                if (sipPassword != null) {
                    // create authentication structure from identity and add to linphone.
                    info = lcFactory.createAuthInfo(username, sipPassword, null, domain);
                    Log.d(TAG, info.getPassword());
                    mLc.addAuthInfo(info);

                }

                proxyCfg = mLc.createProxyConfig(sipAddress, domain, null, true);
                proxyCfg.setExpires(5000);
                proxyCfg.enableRegister(true);
                mLc.addProxyConfig(proxyCfg);

                mLc.setDefaultProxyConfig(proxyCfg);


                mLc.enableSpeaker(true);

                startIterate();
                mInstance = this;
                mLc.setNetworkReachable(true);
            } catch (LinphoneCoreException e) {
                Log.e(TAG, "no config ready yet: " + e.getStackTrace().toString());
            }


        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

    }

    public void shutdown() {
        shouldRun = false;
    }


    private void startIterate() {
        if (shouldRun) {
            TimerTask lTask = new TimerTask() {
                @Override
                public void run() {
                    mLc.iterate();
                }
            };
                    /*use schedule instead of scheduleAtFixedRate to avoid iterate from being call in burst after cpu wake up*/
            mTimer = new Timer("Linphone scheduler");
            mTimer.schedule(lTask, 0, 20);
        }

    }

    public void destroy() {
        try {
            mTimer.cancel();
            mLc.destroy();
        } catch (RuntimeException e) {

        } finally {
            mLc = null;
            mInstance = null;
        }
    }

    @Override
    public void callState(LinphoneCore lc, LinphoneCall call, State cstate, String message) {

        boolean badCaller = true;
        LinphoneCallParams params = mLc.createDefaultCallParameters();

        Log.d(TAG, "Call state: " + cstate + "(" + message + ")");


        if (cstate == State.IncomingReceived) {
            for (String contact : allowedContacts) {
                Log.d(TAG, "contact: " + contact + ", remote: " + call.getRemoteAddress().asString());
                if (call.getRemoteAddress().asString().equals(contact)) {
                    Log.d(TAG, "GOOD CONTACT!");
                    badCaller = false;
                    break;
                }
            }
            if (!badCaller) {
                if (call.getRemoteParams().getVideoEnabled()) {
                    params.setVideoEnabled(true);
                } else {
                    params.setVideoEnabled(false);
                }


                Log.i(TAG, "new state: " + cstate.toString());
                try {
                    //lc.acceptCall(call);
                    lc.acceptCallWithParams(call, call.getCurrentParamsCopy());

                } catch (LinphoneCoreException e) {
                    Log.e(TAG, "Failed to accept call. " + e.getStackTrace().toString());
                }
            }

        } else if (cstate == State.CallEnd) {

            mLc.terminateCall(call);
            Log.e(TAG, "Call ended");
        } else if (cstate == State.CallUpdatedByRemote) {
            //The call's parameters are updated, used for example when video is asked by remote
            boolean remoteVideo = call.getRemoteParams().getVideoEnabled();

            if (remoteVideo) {
                Log.d(TAG, "REMOTE VIDEOSSS");
                call.enableCamera(true);
                mLc.getCurrentCall().enableCamera(true);

                try {
                    setPupilAsDefault();
                    mLc.acceptCallUpdate(call, call.getRemoteParams());
                } catch (LinphoneCoreException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // try {
                //    mLc.deferCallUpdate(call);
                // } catch (LinphoneCoreException e) {
                // TODO Auto-generated catch block
                //         e.printStackTrace();
                // }
            }


        } else if (cstate == State.StreamsRunning) {
            // LinphoneCallParams p = lc.getCurrentCall().getRemoteParams();
            // p.setVideoEnabled(true);
            //  lc.updateCall(call, p);
            mLc.enableSpeaker(mLc.isSpeakerEnabled());

        }
    }


    @Override
    public void authInfoRequested(LinphoneCore lc, String realm, String username, String Domain) {
        Log.d(TAG, "AUTHINFO");
    }

    @Override
    public void globalState(LinphoneCore lc, GlobalState state, String message) {
        Log.d(TAG, "Global state: " + state + "(" + message + ")");
    }

    @Override
    public void callStatsUpdated(LinphoneCore lc, LinphoneCall call, LinphoneCallStats stats) {
        Log.d(TAG, "callStatsUpdated");
    }

    @Override
    public void callEncryptionChanged(LinphoneCore lc, LinphoneCall call, boolean encrypted, String authenticationToken) {
        Log.d(TAG, "callEncryptionChanged");
    }

    @Override
    public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg, RegistrationState cstate, String smessage) {
        Log.d(TAG, "Registration state: " + cstate + "(" + smessage + ")");
    }

    @Override
    public void newSubscriptionRequest(LinphoneCore lc, LinphoneFriend lf, String url) {
        Log.d(TAG, "newSubscriptionRequest");
    }

    @Override
    public void notifyPresenceReceived(LinphoneCore lc, LinphoneFriend lf) {
        Log.d(TAG, "notifyPresenceReceived");
    }

    @Override
    public void textReceived(LinphoneCore lc, LinphoneChatRoom cr, LinphoneAddress from, String message) {
        Log.d(TAG, "textReceived");
    }

    @Override
    public void messageReceived(LinphoneCore lc, LinphoneChatRoom cr, LinphoneChatMessage message) {
        Log.d(TAG, "Message received from " + cr.getPeerAddress().asString() + " : " + message.getText() + "(" + message.getExternalBodyUrl() + ")");
    }

    @Override
    public void isComposingReceived(LinphoneCore lc, LinphoneChatRoom cr) {
        Log.d(TAG, "Composing received from " + cr.getPeerAddress().asString());
    }

    @Override
    public void dtmfReceived(LinphoneCore lc, LinphoneCall call, int dtmf) {
        Log.d(TAG, "dtmfReceived");
    }

    @Override
    public void ecCalibrationStatus(LinphoneCore lc, EcCalibratorStatus status, int delay_ms, Object data) {
        Log.d(TAG, "ecCalibrationStatus");
    }

    @Override
    public void notifyReceived(LinphoneCore lc, LinphoneCall call, LinphoneAddress from, byte[] event) {
        Log.d(TAG, "notifyReceived");
    }

    @Override
    public void transferState(LinphoneCore lc, LinphoneCall call, State new_call_state) {
        Log.d(TAG, "transferState");
    }

    @Override
    public void infoReceived(LinphoneCore lc, LinphoneCall call, LinphoneInfoMessage info) {
        Log.d(TAG, "infoReceived");
    }

    @Override
    public void subscriptionStateChanged(LinphoneCore lc, LinphoneEvent ev, SubscriptionState state) {
        Log.d(TAG, "subscriptionStateChanged");
    }

    @Override
    public void notifyReceived(LinphoneCore lc, LinphoneEvent ev, String eventName, LinphoneContent content) {
        Log.d(TAG, "notifyReceived");
    }

    @Override
    public void publishStateChanged(LinphoneCore lc, LinphoneEvent ev, PublishState state) {
        Log.d(TAG, "publishStateChanged");
    }

    @Override
    public void configuringStatus(LinphoneCore lc, RemoteProvisioningState state, String message) {
        Log.d(TAG, "Configuration state: " + state + "(" + message + ")");
    }

    @Override
    public void show(LinphoneCore lc) {
        Log.d(TAG, "show");
    }

    @Override
    public void displayStatus(LinphoneCore lc, String message) {
        Log.d(TAG, "displayStatus");
    }

    @Override
    public void displayMessage(LinphoneCore lc, String message) {
        Log.d(TAG, "displayMessage");
    }

    @Override
    public void displayWarning(LinphoneCore lc, String message) {
        Log.d(TAG, "displayWarning");
    }


}
