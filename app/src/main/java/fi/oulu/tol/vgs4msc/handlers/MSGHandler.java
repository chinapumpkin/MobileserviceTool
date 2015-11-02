package fi.oulu.tol.vgs4msc.handlers;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

import fi.oulu.tol.vgs4msc.ConnectionObserver;
import fi.oulu.tol.vgs4msc.User;
import fi.oulu.tol.vgs4msc.server.MessageServer;
import fi.oulu.tol.vgs4msc.server.MessageServerObserver;

/**
 * if you need to realize the smart glasses function , You can just delete the comment
 * after /*Indicator-based smart glasses
 */


public class MSGHandler implements MessageServerObserver {
    public static final String TAG = "vgs4msc.MSGHandler";
    private MessageServer mMessageServer;
    private Vector<String> mMsgList = new Vector<String>();
    private Context mContext;
    private String mToken;
    /*Indicator-based smart glasses*/
    //private FirstprotocolMainActivity mLedService;
    private String sipAddress = "";
    private String name="";
    private String passwd="";
    private User user;
    public MSGHandler(Context context) {
        mContext = context;
        mMessageServer = new MessageServer(mContext, this);
        handShake();
        mMessageServer.start();

        /*Indicator-based smart glasses*/
        //mLedService = new FirstprotocolMainActivity(mContext);
        //mLedService.start();
    }

    public void startServer() {
        mMessageServer.start();
    }

    public void closeServer() {
        mMessageServer.kill();
    }

    public void closeLedService() {
        /*Indicator-based smart glasses*/
        // mLedService.stop();
    }




    public void sendMessage(String longitude, String latitude, String degrees) {

        try {

            JSONObject message = new JSONObject();
            message.put("type", "location");
            message.put("name",User.getName());
            message.put("passwd",User.getPasswd());
            message.put("latitude", latitude);
            message.put("longitude", longitude);
            message.put("heading", degrees);

//in this part, I delete the userid. So the get userid function can be delete
            /***/
            // Log.d(TAG,"sendmessage"+message.toString());
            mMessageServer.sendMessage(message.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, e.toString());
        }
    }




    public void processMessages(String message) {// this is analysis the download  message
        int[] values = new int[14];
        Log.d(TAG, "Processing messages");
        if (message != null) {
                         /*Indicator-based smart glasses*/
                                            /*if(!mLedService.started()) {
                                                mLedService.resume();
                                                }*/
            // PARSE VALUE FROM MESSAGE
            switch (message) {
                case "stop":
                    Log.d(TAG, "Setting values for led machine");
                    for (int i = 0; i < values.length; i++) {
                        values[i] = 1;
                    }
                    break;

                case "right":
                    values[13] = 2;
                    break;

                case "left":
                    values[6] = 2;
                    break;

                case "up":
                    values[3] = 2;
                    values[7] = 2;
                    break;

                case "down":
                    values[1] = 2;
                    values[12] = 2;
                    break;

                case "right_down":
                    values[13] = 2;
                    values[11] = 2;
                    values[10] = 2;
                    break;

                case "left_down":
                    values[6] = 2;
                    values[0] = 2;
                    values[2] = 2;
                    break;

                case "right_up":
                    values[8] = 2;
                    values[9] = 2;
                    values[13] = 2;
                    break;

                case "left_up":
                    values[4] = 2;
                    values[5] = 2;
                    values[6] = 2;
                    break;

                default:
                    break;
            }

        }

        Log.d(TAG, "sending values for led machine");
                                            /*Indicator-based smart glasses*///
        // mLedService.sendDirections(values, 15, 5, 1);


    }


    public String getLastMessage() {//it is used in download message function to get the finnally message, it is tapio's code
        String tmp = null;
        if (!mMsgList.isEmpty()) {
            tmp = mMsgList.get(0);
        }
        if (null != tmp) {
            mMsgList.remove(0);
            return tmp;
        } else {
            return null;
        }
    }

    public void messageToSend(String name, String passwd,String sip_address) { //used to send the token
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", "register");
            msg.put("name", User.getName());
            msg.put("passwd",User.getPasswd());
            msg.put("sip_address",User.getSip_address());
            msg.put("token",User.getToken());


            /***/
             Log.d(TAG,"sendmessage"+msg.toString());
            mMessageServer.sendMessage(msg.toString());
        } catch (JSONException i) {
            Log.d(TAG, i.toString());
        }
    }
    public void setToken(String token){
        mToken=token;
    }
    public void tcpMessageSend() {


    }

    @Override
    public void messageSend() {
        // TODO Auto-generated method stub

    }

    @Override
    public void errorNotification(String error) {
        // TODO Auto-generated method stub

    }


    @Override
    public void handshakeReceived() {
        String msg = mMessageServer.getHandshakeMessage();
        String tokens[] = msg.split(",");

        mMessageServer.setUserID(tokens[0]);

    }

    public boolean hasMessages() {
        if (!mMsgList.isEmpty()) {
            return true;
        }
        return false;
    }

    public void setNetwork(String mIpAddress, String mPort) {//反正我是发现这个有什么用
        Log.d("TESTING", mIpAddress + mPort);

        if (mContext != null) {
            mMessageServer = new MessageServer(mContext, this);
            mMessageServer.setAddress(mIpAddress);
            mMessageServer.setPort(mPort);
            startServer();


            mMessageServer.setUserID("1");

            JSONObject message = new JSONObject();
            try {
                message.put("userid", mMessageServer.getUserID());
                message.put("SIP: ", sipAddress);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, "JSON ERROR" + e.toString());
            }

            mMessageServer.sendMessage(message.toString());

        }
    }

    public void handShake() {

        if (mContext != null) {
            mMessageServer.setUserID("1");

            JSONObject message = new JSONObject();
            try {
                message.put("userid", mMessageServer.getUserID());
                message.put("SIP: ", sipAddress);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, "JSON ERROR" + e.toString());
            }

            mMessageServer.sendMessage(message.toString());

        }
    }

    public void setLinphonAddress(String sipAddress) {
        this.sipAddress = sipAddress;

    }
}
