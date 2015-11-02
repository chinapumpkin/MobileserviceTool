package net.majorkernelpanic.streaming;

import android.graphics.ImageFormat;
import android.media.MediaRecorder;

import com.serenegiant.usb.UVCCamera;

import net.majorkernelpanic.streaming.rtp.H263Packetizer;

import java.io.IOException;

/**
 * Created by dengcanrong on 15/7/4.
 */
public class UVCH263Stream extends UVCVideoStream {

    public UVCH263Stream(UVCCamera camera) {
        super(camera);
        mCameraImageFormat = ImageFormat.NV21;
        mVideoEncoder = MediaRecorder.VideoEncoder.H263;
        mPacketizer = new H263Packetizer();
    }
    /**
     * Starts the stream.
     */
    public synchronized void start() throws IllegalStateException, IOException {
        if (!mStreaming) {
            configure();
            super.start();
        }
    }

    public synchronized void configure() throws IllegalStateException, IOException {
        super.configure();
        mMode = MODE_MEDIARECORDER_API;
        mQuality = mRequestedQuality.clone();
    }

    /**
     * Returns a description of the stream using SDP. It can then be included in an SDP file.
     */
    public String getSessionDescription() {
        return "m=video "+String.valueOf(getDestinationPorts()[0])+" RTP/AVP 96\r\n" +
                "a=rtpmap:96 H263-1998/90000\r\n";
    }

}
