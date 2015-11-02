package fi.oulu.tol.vgs4msc.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import fi.tol.oulu.vgs4msc.R;

public class MessageServer {

        private static final int MAX_UDP_DATAGRAM_LEN = 1500;
        private static final String TAG = ".vgs4msc.messageserver";
        private boolean bKeepRunning = true;
        private List <String> mMsgList = new ArrayList<String>();
        private String serverPort = "8080";
        private MessageServerObserver proxyObserver= null;
        private Context mContext;
        private String userid = null;
        private String hsMessage;
        private InetAddress senderAddress = null;
        private static DatagramSocket dSocket;
        private Handler timerHandler = new Handler();
        private Timer pollingTimer = new Timer();
        
        private String ipAddress1 = "kotikolo.linkpc.net";
        private String ipAddress="10.20.204.144";
        public MessageServer(Context c, MessageServerObserver obs) {
                mContext = c;
                proxyObserver = obs;
                mMsgList = new Vector<String>();
                try {
                        dSocket = new DatagramSocket(Integer.parseInt(serverPort));
                        dSocket.setReuseAddress(true);
                } catch (NumberFormatException e) {
                        Log.d(TAG, e.toString());
                        e.printStackTrace();
                } catch (SocketException e) {
                        Log.d(TAG, e.toString());
                }
        }
        
        

        
        public void start() {
              /*this is supposed to download the message, however, it is been done by GCM now*/
        }


        /**need to delete because it is download message*/
        public String getLastMessage() {
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

        public void sendMessage(String message){
                new UploadMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);


        }
        
        private class UploadMessageTask extends AsyncTask<String, Void, Void> {
                @Override
                protected Void doInBackground(String ... text) {
                        
                        if( ipAddress != null) {
                                byte[] sendData = text[0].toString().getBytes();
                                try {
                                        senderAddress = InetAddress.getByName(ipAddress);
                                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, senderAddress, Integer.parseInt(serverPort));
                                        dSocket.send(sendPacket);
                                        Thread.sleep(100);
                                } catch (UnknownHostException e) {
                                        Log.d(TAG, e.toString());
                                } catch (InterruptedException e) {
                                        Log.d(TAG, e.toString());
                                } catch (IOException e) {
                                        Log.d(TAG, e.toString());
                                }        
                        }
                        return null;
                       
                }
        }


        
        public String getHandshakeMessage() {
                return hsMessage;
        }
        
        public String getSenderAddress() {
                return senderAddress.getHostAddress();
        }
        
        public boolean hasMessages() {
                if(!mMsgList.isEmpty()) {
                        return true;
                }
                return false;
        }
        

        public void handshakeReceived() {
                proxyObserver.handshakeReceived();
        }
        
        private boolean checkNetwork() {
        //to check is the network available. WIFI or 4G but it seems this method This method was deprecated in API level 23.
                ConnectivityManager connMgr = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    return true;
                }
                Log.d(TAG, "No network, cannot initiate retrieval!");
                return false;
        }
        
        public void kill() { 
                bKeepRunning = false;
        }
        
        public void setPort(String port) {
                serverPort = port;
        }
        
        public void setAddress(String ip) {
                ipAddress = ip;
        }
        
        public String getPort() {
                return serverPort;
        }
        
        public void setUserID(String uid) {
                userid = uid;
        }
        
        
        public String getUserID() {
                return userid;
        }


}
