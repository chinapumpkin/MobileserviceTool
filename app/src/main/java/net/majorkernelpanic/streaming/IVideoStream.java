package net.majorkernelpanic.streaming;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.hardware.Camera;

import net.majorkernelpanic.streaming.exceptions.CameraInUseException;
import net.majorkernelpanic.streaming.exceptions.InvalidSurfaceException;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.video.VideoQuality;

import java.io.IOException;

/**
 * Created by dengcanrong on 15/7/3.
 */
public interface IVideoStream extends IMediaStream {
    /**
     * Returns the id of the camera currently selected.
     * Can be either {@link Camera.CameraInfo#CAMERA_FACING_BACK} or
     * {@link Camera.CameraInfo#CAMERA_FACING_FRONT}.
     */
    //  public int getCamera();

    /**
     * Sets the camera that will be used to capture video.
     * You can call this method at any time and changes will take effect next time you start the stream.
     *
     * @param camera Can be either CameraInfo.CAMERA_FACING_BACK or CameraInfo.CAMERA_FACING_FRONT
     */
    // public void setCamera(int camera);

    /**
     * Sets a Surface to show a preview of recorded media (video).
     * You can call this method at any time and changes will take effect next time you call {@link #start()}.
     */
    public void setSurfaceView(SurfaceView view);

    /**
     * Toggles the LED of the phone if it has one.
     * You can get the current state of the flash with //{@link //VideoStream#getFlashState()}.
     */
   // public void toggleFlash();

    /**
     * Indicates whether or not the flash of the phone is on.
     */
  //  public boolean getFlashState();

    /**
     * Turns the LED on or off if phone has one.
     */
   // public void setFlashState(boolean state);

    /**
     * Sets the orientation of the preview.
     *
     * @param orientation The orientation of the preview
     */
//    public void setPreviewOrientation(int orientation);

    /**
     * Returns the quality of the stream.
     */
     public VideoQuality getVideoQuality();

    /**
     * Sets the configuration of the stream. You can call this method at any time
     * and changes will take effect next time you call {@link #configure()}.
     *
     * @param videoQuality Quality of the stream
     */
     public void setVideoQuality(VideoQuality videoQuality);

    /**
     * // * Some data (SPS and PPS params) needs to be stored when {@link # getSessionDescription()} is called
     *
     * @param prefs The SharedPreferences that will be used to save SPS and PPS parameters
     */
    public void setPreferences(SharedPreferences prefs);


    public void startPreview()
            throws CameraInUseException,
            InvalidSurfaceException,
            RuntimeException;

    /**
     * Stops the preview.
     */
    void stopPreview();


    /**
     * Video encoding is done by a MediaCodec.
     */
    @SuppressLint("NewApi")
    void encodeWithMediaCodecMethod1() throws RuntimeException, IOException;

    /**
     * Video encoding is done by a MediaCodec.
     * But here we will use the buffer-to-surface method
     */
    @SuppressLint({"InlinedApi", "NewApi"})
    void encodeWithMediaCodecMethod2() throws RuntimeException, IOException;

    /**
     * Returns a description of the stream using SDP.
     * This method can only be called after {@link Stream#configure()}.
     *
     * @throws IllegalStateException Thrown when {@link Stream#configure()} wa not called.
     */

    /**
     * Opens the camera in a new Looper thread so that the preview callback is not called from the main thread
     * If an exception is thrown in this Looper thread, we bring it back into the main thread.
     *
     * @throws RuntimeException Might happen if another app is already using the camera.
     */
    // void openCamera() throws RuntimeException;

    //  void createCamera() throws RuntimeException;

    // void destroyCamera();

    // void updateCamera() throws RuntimeException;

    //  void lockCamera();

    //  void unlockCamera();

    /**
     * Computes the average frame rate at which the preview callback is called.
     * We will then use this average frame rate with the MediaCodec.
     * Blocks the thread in which this function is called.
     */
    void measureFramerate();
}
