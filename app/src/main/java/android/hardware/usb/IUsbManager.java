package android.hardware.usb;

/**
 * Created by dengcanrong on 15/8/4.
 */
//4.2.2
public interface IUsbManager extends android.os.IInterface
{
    /** Local-side IPC implementation stub class. */
    public static abstract class Stub extends android.os.Binder implements android.hardware.usb.IUsbManager {
        private static final java.lang.String DESCRIPTOR = "android.hardware.usb.IUsbManager";

        /** Construct the stub at attach it to the interface. */
        public Stub()         {
            throw new RuntimeException( "Stub!" );
        }

        /**
         * Cast an IBinder object into an android.hardware.usb.IUsbManager
         * interface, generating a proxy if needed.
         */
        public static android.hardware.usb.IUsbManager asInterface( android.os.IBinder obj) {
            throw new RuntimeException( "Stub!" );
        }

        @Override
        public android.os.IBinder asBinder() {
            throw new RuntimeException( "Stub!" );
        }

        @Override
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
            throw new RuntimeException( "Stub!" );
        }

        static final int TRANSACTION_getDeviceList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_openDevice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_getCurrentAccessory = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
        static final int TRANSACTION_openAccessory = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
        static final int TRANSACTION_setDevicePackage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
        static final int TRANSACTION_setAccessoryPackage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
        static final int TRANSACTION_hasDevicePermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
        static final int TRANSACTION_hasAccessoryPermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
        static final int TRANSACTION_requestDevicePermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
        static final int TRANSACTION_requestAccessoryPermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
        static final int TRANSACTION_grantDevicePermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
        static final int TRANSACTION_grantAccessoryPermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
        static final int TRANSACTION_hasDefaults = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
        static final int TRANSACTION_clearDefaults = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
        static final int TRANSACTION_setCurrentFunction = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
        static final int TRANSACTION_setMassStorageBackingFile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
        static final int TRANSACTION_allowUsbDebugging = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
        static final int TRANSACTION_denyUsbDebugging = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
    }

    /* Returns a list of all currently attached USB devices */
    public void getDeviceList(android.os.Bundle devices)
            throws android.os.RemoteException;

    /*
     * Returns a file descriptor for communicating with the USB device. The
     * native fd can be passed to usb_device_new() in libusbhost.
     */
    public android.os.ParcelFileDescriptor openDevice(
            java.lang.String deviceName) throws android.os.RemoteException;

    /* Returns the currently attached USB accessory */
    public android.hardware.usb.UsbAccessory getCurrentAccessory()
            throws android.os.RemoteException;

    /*
     * Returns a file descriptor for communicating with the USB accessory. This
     * file descriptor can be used with standard Java file operations.
     */
    public android.os.ParcelFileDescriptor openAccessory(
            android.hardware.usb.UsbAccessory accessory)
            throws android.os.RemoteException;

    /*
     * Sets the default package for a USB device (or clears it if the package
     * name is null)
     */
    public void setDevicePackage(android.hardware.usb.UsbDevice device,
                                 java.lang.String packageName, int userId)
            throws android.os.RemoteException;

    /*
     * Sets the default package for a USB accessory (or clears it if the package
     * name is null)
     */
    public void setAccessoryPackage(
            android.hardware.usb.UsbAccessory accessory,
            java.lang.String packageName, int userId)
            throws android.os.RemoteException;

    /* Returns true if the caller has permission to access the device. */
    public boolean hasDevicePermission(android.hardware.usb.UsbDevice device)
            throws android.os.RemoteException;

    /* Returns true if the caller has permission to access the accessory. */
    public boolean hasAccessoryPermission(
            android.hardware.usb.UsbAccessory accessory)
            throws android.os.RemoteException;

    /*
     * Requests permission for the given package to access the device. Will
     * display a system dialog to query the user if permission had not already
     * been given.
     */
    public void requestDevicePermission(android.hardware.usb.UsbDevice device,
                                        java.lang.String packageName, android.app.PendingIntent pi)
            throws android.os.RemoteException;

    /*
     * Requests permission for the given package to access the accessory. Will
     * display a system dialog to query the user if permission had not already
     * been given. Result is returned via pi.
     */
    public void requestAccessoryPermission(
            android.hardware.usb.UsbAccessory accessory,
            java.lang.String packageName, android.app.PendingIntent pi)
            throws android.os.RemoteException;

    /* Grants permission for the given UID to access the device */
    public void grantDevicePermission(android.hardware.usb.UsbDevice device,
                                      int uid) throws android.os.RemoteException;

    /* Grants permission for the given UID to access the accessory */
    public void grantAccessoryPermission(
            android.hardware.usb.UsbAccessory accessory, int uid)
            throws android.os.RemoteException;

    /*
     * Returns true if the USB manager has default preferences or permissions
     * for the package
     */
    public boolean hasDefaults(java.lang.String packageName, int userId)
            throws android.os.RemoteException;

    /* Clears default preferences and permissions for the package */
    public void clearDefaults(java.lang.String packageName, int userId)
            throws android.os.RemoteException;

    /* Sets the current USB function. */
    public void setCurrentFunction(java.lang.String function,
                                   boolean makeDefault) throws android.os.RemoteException;

    /* Sets the file path for USB mass storage backing file. */
    public void setMassStorageBackingFile(java.lang.String path)
            throws android.os.RemoteException;

    /*
     * Allow USB debugging from the attached host. If alwaysAllow is true, add
     * the the public key to list of host keys that the user has approved.
     */
    public void allowUsbDebugging(boolean alwaysAllow,
                                  java.lang.String publicKey) throws android.os.RemoteException;

    /* Deny USB debugging from the attached host */
    public void denyUsbDebugging() throws android.os.RemoteException;
}