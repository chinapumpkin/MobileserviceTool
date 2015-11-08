package oulu.university.smartglasses;

// some change

/**
 * Created by afirouzi on 20.10.2015.
 */
public interface ICallback {
        //change this interface method to your own application
        void callBackLinphoneMessageReceived(String linphoneMessageValue);
        //remove this method
        void callBackLinphoneStatusChanged(String linphoneOnlineStatueValue);
}
