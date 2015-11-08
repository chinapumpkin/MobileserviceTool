package oulu.university.smartglasses;

// you can delete this class, but there is something to do before

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import org.linphone.core.*;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import fi.tol.oulu.vgs4msc.R;

/**
 * Created by afirouzi on 16.10.2015.
 */
public class LinphoneMiniManager implements LinphoneCoreListener {
    private static LinphoneMiniManager mInstance;
    private Context mContext;
    private LinphoneCore mLinphoneCore;
    private Timer mTimer;
    ICallback ic;


    public LinphoneMiniManager(Context c,String someParameter, ICallback _ic) {
        this.ic=_ic;
        mContext = c;
        LinphoneCoreFactory.instance().setDebugMode(true, "Linphone Mini");

        try {
            String basePath = mContext.getFilesDir().getAbsolutePath();
            copyAssetsFromPackage(basePath);
            mLinphoneCore = LinphoneCoreFactory.instance().createLinphoneCore(this, basePath + "/.linphonerc", basePath + "/linphonerc", null, mContext);

//            mLinphoneCore = LinphoneCoreFactory.instance().createLinphoneCore(this, mContext);
//            LinphoneProxyConfig proxy_cfg;
//            proxy_cfg = mLinphoneCore.createProxyConfig();
//            LinphoneAuthInfo info = LinphoneCoreFactory.instance().createAuthInfo("indicator_glasses", "Abas161", "sip.linphone.org", "sip.linphone.org");
//            info.setDomain("sip.linphone.org");
//            info.setUserId("indicator_glasses");
//            info.setUsername("indicator_glasses");
//            info.setRealm("sip.linphone.org");
//            info.setPassword("Abas161");
//            mLinphoneCore.addAuthInfo(info);
//            proxy_cfg.setIdentity("sip:indicator_glasses@sip.linphone.org"); /*set identity with user name and domain*/
//            proxy_cfg.setProxy("sip.linphone.org"); /* we assume domain = proxy server address*/
//            proxy_cfg.enableRegister(true); /*activate registration for this proxy config*/
//            mLinphoneCore.addProxyConfig(proxy_cfg); /*add proxy config to linphone core*/
//            mLinphoneCore.setDefaultProxyConfig(proxy_cfg); /*set to default proxy*/
//            proxy_cfg.done();
//            mLinphoneCore.iterate();
//            LinphoneChatRoom chat_room = mLinphoneCore.getOrCreateChatRoom("aryan_firouzian"); //lc is object of LinphoneCore
//            chat_room.sendMessage("Hello world");


            initLinphoneCoreValues(basePath);
            setUserAgent();
            setFrontCamAsDefault();
            mLinphoneCore.setNetworkReachable(true); // Let's assume it's true
            startIterate();
            mInstance = this;


        } catch (LinphoneCoreException e) {
        } catch (IOException e) {
        }
    }

    public static LinphoneMiniManager getInstance() {
        return mInstance;
    }

    public void destroy() {
        try {
            mTimer.cancel();
            mLinphoneCore.destroy();
        } catch (RuntimeException e) {
        } finally {
            mLinphoneCore = null;
            mInstance = null;
        }
    }

    private void startIterate() {
        TimerTask lTask = new TimerTask() {
            @Override
            public void run() {
                mLinphoneCore.iterate();
            }
        };

		/*use schedule instead of scheduleAtFixedRate to avoid iterate from being call in burst after cpu wake up*/
        mTimer = new Timer("LinphoneMini scheduler");
        mTimer.schedule(lTask, 0, 20);
    }

    private void setUserAgent() {
        try {
            String versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
            if (versionName == null) {
                versionName = String.valueOf(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode);
            }
            mLinphoneCore.setUserAgent("LinphoneMiniAndroid", versionName);
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    private void setFrontCamAsDefault() {
        int camId = 0;
        AndroidCameraConfiguration.AndroidCamera[] cameras = AndroidCameraConfiguration.retrieveCameras();
        for (AndroidCameraConfiguration.AndroidCamera androidCamera : cameras) {
            if (androidCamera.frontFacing)
                camId = androidCamera.id;
        }
        mLinphoneCore.setVideoDevice(camId);
    }

    private void copyAssetsFromPackage(String basePath) throws IOException {
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.oldphone_mono, basePath + "/oldphone_mono.wav");
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.ringback, basePath + "/ringback.wav");
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.toy_mono, basePath + "/toy_mono.wav");
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.linphonerc_default, basePath + "/.linphonerc");
        LinphoneMiniUtils.copyFromPackage(mContext, R.raw.linphonerc_factory, new File(basePath + "/linphonerc").getName());
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.lpconfig, basePath + "/lpconfig.xsd");
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.rootca, basePath + "/rootca.pem");
    }

    private void initLinphoneCoreValues(String basePath) {
        mLinphoneCore.setContext(mContext);
        mLinphoneCore.setRing(null);
        mLinphoneCore.setRootCA(basePath + "/rootca.pem");
        mLinphoneCore.setPlayFile(basePath + "/toy_mono.wav");
        mLinphoneCore.setChatDatabasePath(basePath + "/linphone-history.db");

        int availableCores = Runtime.getRuntime().availableProcessors();
        mLinphoneCore.setCpuCount(availableCores);
    }


    //When mesage is received call this method
    @Override
    public void messageReceived(LinphoneCore lc, LinphoneChatRoom cr, final LinphoneChatMessage message) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ic.callBackLinphoneMessageReceived(message.getText());
            }
        });
    }

    @Override
    public void authInfoRequested(LinphoneCore lc, String realm, String username, String Unknown) {

    }

    @Override
    public void globalState(LinphoneCore lc, LinphoneCore.GlobalState state, String message) {
    }

    @Override
    public void callState(LinphoneCore lc, LinphoneCall call, LinphoneCall.State cstate,
                          String message) {
    }

    @Override
    public void callStatsUpdated(LinphoneCore lc, LinphoneCall call,
                                 LinphoneCallStats stats) {

    }

    @Override
    public void callEncryptionChanged(LinphoneCore lc, LinphoneCall call,
                                      boolean encrypted, String authenticationToken) {

    }

    @Override
    public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg,
                                  LinphoneCore.RegistrationState cstate, final String smessage) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ic.callBackLinphoneStatusChanged(smessage.toString());
            }
        });
        LinphoneChatRoom chat_room = mLinphoneCore.getOrCreateChatRoom("sip:aryan_firouzian@sip.linphone.org"); //lc is object of LinphoneCore
        chat_room.sendMessage("Connected");
    }

    @Override
    public void newSubscriptionRequest(LinphoneCore lc, LinphoneFriend lf,
                                       String url) {

    }

    @Override
    public void notifyPresenceReceived(LinphoneCore lc, LinphoneFriend lf) {
        //MainActivity.LinphoneBtn.setText("presence");
    }

    @Override
    public void textReceived(LinphoneCore linphoneCore, LinphoneChatRoom linphoneChatRoom, LinphoneAddress linphoneAddress, String s) {

    }

    @Override
    public void isComposingReceived(LinphoneCore lc, LinphoneChatRoom cr) {
        //MainActivity.LinphoneBtn.setText("chatroomCompose");
    }

    @Override
    public void dtmfReceived(LinphoneCore lc, LinphoneCall call, int dtmf) {

    }

    @Override
    public void ecCalibrationStatus(LinphoneCore lc, LinphoneCore.EcCalibratorStatus status,
                                    int delay_ms, Object data) {

    }

    @Override
    public void notifyReceived(LinphoneCore lc, LinphoneCall call,
                               LinphoneAddress from, byte[] event) {
        //MainActivity.LinphoneBtn.setText("notify");
    }

    @Override
    public void transferState(LinphoneCore lc, LinphoneCall call,
                              LinphoneCall.State new_call_state) {

    }

    @Override
    public void infoReceived(LinphoneCore lc, LinphoneCall call,
                             LinphoneInfoMessage info) {
        //MainActivity.LinphoneBtn.setText("inforeceived");
    }

    @Override
    public void subscriptionStateChanged(LinphoneCore lc, LinphoneEvent ev,
                                         SubscriptionState state) {

    }

    @Override
    public void notifyReceived(LinphoneCore lc, LinphoneEvent ev,
                               String eventName, LinphoneContent content) {
        //MainActivity.LinphoneBtn.setText("notifyReceived");
    }

    @Override
    public void publishStateChanged(LinphoneCore lc, LinphoneEvent ev,
                                    PublishState state) {

    }

    @Override
    public void configuringStatus(LinphoneCore lc, LinphoneCore.RemoteProvisioningState state, String message) {

    }

    @Override
    public void show(LinphoneCore lc) {

    }

    @Override
    public void displayStatus(LinphoneCore lc, String message) {

    }

    @Override
    public void displayMessage(LinphoneCore lc, String message) {
        //MainActivity.LinphoneBtn.setText("displaymessage");
    }

    @Override
    public void displayWarning(LinphoneCore lc, String message) {

    }





}
