package net.majorkernelpanic.streaming;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.serenegiant.usb.UVCCamera;

import net.majorkernelpanic.streaming.exceptions.StorageUnavailableException;
import net.majorkernelpanic.streaming.hw.EncoderDebugger;
import net.majorkernelpanic.streaming.mp4.MP4Config;
import net.majorkernelpanic.streaming.rtp.H264Packetizer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 * Created by dengcanrong on 15/7/4.
 */
public class UVCH264Stream extends UVCVideoStream {
    public final static String TAG = "H264Stream";

    private Semaphore mLock = new Semaphore(0);
    private MP4Config mConfig;


    /**
     * Constructs the H.264 stream.
     *
     * @param // cameraId Can be either CameraInfo.CAMERA_FACING_BACK or CameraInfo.CAMERA_FACING_FRONT
     * @throws IOException
     */
    public UVCH264Stream(UVCCamera camera) {
        super(camera);
        mMimeType = "video/avc";
        mCameraImageFormat = ImageFormat.NV21;
        mVideoEncoder = MediaRecorder.VideoEncoder.H264;
        mPacketizer = new H264Packetizer();
    }

    /**
     * Returns a description of the stream using SDP. It can then be included in an SDP file.
     */
    public synchronized String getSessionDescription() throws IllegalStateException {
        if (mConfig == null)
            throw new IllegalStateException("You need to call configure() first !");
        return "m=video " + String.valueOf(getDestinationPorts()[0]) + " RTP/AVP 96\r\n" +
                "a=rtpmap:96 H264/90000\r\n" +
                "a=fmtp:96 packetization-mode=1;profile-level-id=" + mConfig.getProfileLevel() + ";sprop-parameter-sets=" + mConfig.getB64SPS() + "," + mConfig.getB64PPS() + ";\r\n";
    }

    /**
     * Starts the stream.
     * This will also open the camera and display the preview if {@link #startPreview()} has not already been called.
     */
    public synchronized void start() throws IllegalStateException, IOException {
        if (!mStreaming) {
            configure();
            byte[] pps = Base64.decode(mConfig.getB64PPS(), Base64.NO_WRAP);
            byte[] sps = Base64.decode(mConfig.getB64SPS(), Base64.NO_WRAP);
            ((H264Packetizer) mPacketizer).setStreamParameters(pps, sps);
            super.start();
        }
    }

    /**
     * Configures the stream. You need to call this before calling {@link #getSessionDescription()} to apply
     * your configuration of the stream.
     */
    public synchronized void configure() throws IllegalStateException, IOException {
        super.configure();
        mMode = mRequestedMode;
        mQuality = mRequestedQuality.clone();
        mConfig = testH264();
    }

    /**
     * Tests if streaming with the given configuration (bit rate, frame rate, resolution) is possible
     * and determines the pps and sps. Should not be called by the UI thread.
     */
    private MP4Config testH264() throws IllegalStateException, IOException {
        if (mMode != MODE_MEDIARECORDER_API) return testMediaCodecAPI();
        else {
            Log.d(TAG, "the application didn't satisfy the API requirement");
            return null;
    }
    }

    @SuppressLint("NewApi")
    private MP4Config testMediaCodecAPI() throws RuntimeException, IOException {
        updateCamera();
        try {
            if (mQuality.resX >= 640) {
                // Using the MediaCodec API with the buffer method for high resolutions is too slow
                mMode = MODE_MEDIARECORDER_API;
            }
            EncoderDebugger debugger = EncoderDebugger.debug(mSettings, mQuality.resX, mQuality.resY);
            return new MP4Config(debugger.getB64SPS(), debugger.getB64PPS());
        } catch (Exception e) {
            // Fallback on the old streaming method using the MediaRecorder API
            Log.e(TAG, "Resolution not supported with the MediaCodec API, we fallback on the old streamign method.");
            mMode = MODE_MEDIARECORDER_API;
            return testH264();
        }
    }




}
