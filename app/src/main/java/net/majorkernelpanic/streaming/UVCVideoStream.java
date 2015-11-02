package net.majorkernelpanic.streaming;

import android.content.SharedPreferences;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.UVCCamera;

import net.majorkernelpanic.streaming.exceptions.CameraInUseException;
import net.majorkernelpanic.streaming.exceptions.ConfNotSupportedException;
import net.majorkernelpanic.streaming.exceptions.InvalidSurfaceException;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.hw.EncoderDebugger;
import net.majorkernelpanic.streaming.hw.NV21Convertor;
import net.majorkernelpanic.streaming.rtp.MediaCodecInputStream;
import net.majorkernelpanic.streaming.video.VideoQuality;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
// don't the to settle the quality because it use the default quality

/**
 * Created by dengcanrong on 15/7/4.
 */
public class UVCVideoStream extends MediaStream implements IVideoStream {
    protected final static String TAG = "UVCVideoStream";
    // for thread pool
    private static final int CORE_POOL_SIZE = 1;        // initial/minimum threads
    private static final int MAX_POOL_SIZE = 4;            // maximum threads
    private static final int KEEP_ALIVE_TIME = 10;        // time periods while keep the idle thread
    protected static final ThreadPoolExecutor EXECUTER
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    private static final String MIME_TYPE = "video/avc";
    protected static int[] recognizedFormats;

    static {
        recognizedFormats = new int[]{
//        	MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
                MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar,
//        	MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
        };
    }

    protected VideoQuality mRequestedQuality = VideoQuality.DEFAULT_VIDEO_QUALITY.clone();
    protected VideoQuality mQuality = mRequestedQuality.clone();
    protected SurfaceHolder.Callback mSurfaceHolderCallback = null;
    protected SurfaceView mSurfaceView = null;
    protected SharedPreferences mSettings = null;
    protected int mVideoEncoder, mCameraId = 0;
    //  protected int mRequestedOrientation = 0, mOrientation = 0;
    protected UVCCamera mCamera;
    protected Thread mCameraThread;
    protected Looper mCameraLooper;
    protected boolean mCameraOpenedManually = true;
    protected boolean mFlashEnabled = false;
    protected boolean mSurfaceReady = false;
    protected boolean mUnlocked = false;
    protected boolean mPreviewStarted = false;
    protected boolean mUpdated = false;
    protected String mMimeType;
    protected int mCameraImageFormat;
    protected boolean sendEOS = false;
    protected int mColorFormat;
    private MediaCodec.BufferInfo mBufferInfo;

    // don't use this directly
    public UVCVideoStream() {

    }

    public UVCVideoStream(UVCCamera camera) {
        mCamera = camera;

    }

    //
    protected static final int selectColorFormat(final MediaCodecInfo codecInfo, final String mimeType) {

        int result = 0;
        final MediaCodecInfo.CodecCapabilities caps;
        try {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            caps = codecInfo.getCapabilitiesForType(mimeType);
        } finally {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        }
        int colorFormat;
        for (int i = 0; i < caps.colorFormats.length; i++) {
            colorFormat = caps.colorFormats[i];
            if (isRecognizedViewoFormat(colorFormat)) {
                if (result == 0)
                    result = colorFormat;
                break;
            }
        }
        if (result == 0)
            Log.e(TAG, "couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return result;
    }

    private static final boolean isRecognizedViewoFormat(final int colorFormat) {
        final int n = recognizedFormats != null ? recognizedFormats.length : 0;
        for (int i = 0; i < n; i++) {
            if (recognizedFormats[i] == colorFormat) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setSurfaceView(SurfaceView view) {
        mSurfaceView = view;
        if (mSurfaceHolderCallback != null && mSurfaceView != null && mSurfaceView.getHolder() != null) {
            mSurfaceView.getHolder().removeCallback(mSurfaceHolderCallback);
        }
        if (mSurfaceView.getHolder() != null) {
            mSurfaceHolderCallback = new SurfaceHolder.Callback() {
                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mSurfaceReady = false;
                    stopPreview();
                    Log.d(TAG, "Surface destroyed !");
                }

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    mSurfaceReady = true;
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    Log.d(TAG, "Surface Changed !");
                }
            };
            mSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
            mSurfaceReady = true;
        }
    }

    /**
     * Configures the stream.
     * to apply your configuration of the stream.
     */
    public synchronized void configure() throws IllegalStateException, IOException {
        super.configure();
        //  mOrientation = mRequestedOrientation;
    }

    /**
     * Starts the stream.
     * This will also open the camera and display the preview
     * if {@link #startPreview()} has not already been called.
     */
    public synchronized void start() throws IllegalStateException, IOException {
        if (!mPreviewStarted) mCameraOpenedManually = false;
        super.start();
        Log.d(TAG, "Stream configuration: FPS: " + mQuality.framerate + " Width: " + mQuality.resX + " Height: " + mQuality.resY);
    }

    /**
     * Stops the stream.
     */
    public synchronized void stop() {
        if (mCamera != null) {
            if (mMode == MODE_MEDIACODEC_API) {//MediaCodec with buffer
                mCamera.setFrameCallback(null, 0);
                // mCamera.setPreviewCallbackWithBuffer(null);
            } else if (mMode == MODE_MEDIACODEC_API_2) {//Mediacodec with Surface
                ((SurfaceView) mSurfaceView).removeMediaCodecSurface();
            } else {
                Log.d(TAG, "mMode is Mediarecorder");

            }
            super.stop();
            // We need to restart the preview
            if (!mCameraOpenedManually) {
                destroyCamera();
            } else {
                try {
                    startPreview();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected synchronized void destroyCamera() {
        if (mCamera != null) {
            if (mStreaming) super.stop();
            //  lockCamera(); unlock the camera so the camera can be used by other acitivity
            mCamera.stopPreview();
            try {
                mCamera.close();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage() != null ? e.getMessage() : "unknown error");
            }
            mCamera = null;
            mCameraLooper.quit();
            mUnlocked = false;
            mPreviewStarted = false;
        }
    }

    protected synchronized void updateCamera() throws RuntimeException {

        // The camera is already correctly configured
        if (mUpdated) return;

        if (mPreviewStarted) {
            mPreviewStarted = false;
            mCamera.stopPreview();
        }
        try {
            mPreviewStarted = true;
            mUpdated = true;
        } catch (RuntimeException e) {
            destroyCamera();
            throw e;
        }
    }


    public VideoQuality getVideoQuality() {
        return mRequestedQuality;
    }

    public void setVideoQuality(VideoQuality videoQuality) {

        if (!mRequestedQuality.equals(videoQuality)) {
            mRequestedQuality = videoQuality.clone();
            mUpdated = false;

        }
    }

    @Override
    public void setPreferences(SharedPreferences prefs) {
        mSettings = prefs;
    }

    // call it after UVCVideoCamera(UVCCamera)
    @Override
    public void startPreview() throws CameraInUseException, InvalidSurfaceException, RuntimeException {
        mCameraOpenedManually = true;
        if (!mPreviewStarted) {
            //  createCamera();
            updateCamera();
        }
    }

    @Override
    public void stopPreview() {
        mCameraOpenedManually = false;
        stop();
    }

    public void encodeWithMediaCodec() throws RuntimeException, IOException {
        if (mMode == MODE_MEDIACODEC_API_2) {
            // Uses the method MediaCodec.createInputSurface to feed the encoder
            encodeWithMediaCodecMethod2();
        } else {
            // Uses dequeueInputBuffer to feed the encoder
            encodeWithMediaCodecMethod1();
        }
    }

    @Override
    public void encodeWithMediaCodecMethod1() throws RuntimeException, IOException {

        Log.d(TAG, "Video encoded using the MediaCodec API with a buffer");

        // Updates the parameters of the camera if needed
        // createCamera();
        updateCamera();

        // Estimates the frame rate of the camera
        measureFramerate();

        // Starts the preview if needed
        if (!mPreviewStarted) {
            try {
                mCamera.startPreview();
                mPreviewStarted = true;
            } catch (RuntimeException e) {
                destroyCamera();
                throw e;
            }
        }

        final MediaCodecInfo videoCodecInfo = selectVideoCodec(MIME_TYPE);
        if (videoCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }
        Log.i(TAG, "selected codec: " + videoCodecInfo.getName());
        EncoderDebugger debugger = EncoderDebugger.debug(mSettings, mQuality.resX, mQuality.resY);
        final NV21Convertor convertor = debugger.getNV21Convertor();

        mMediaCodec = MediaCodec.createByCodecName(debugger.getEncoderName());

        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, mQuality.resX, mQuality.resY);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mQuality.bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mQuality.framerate);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);


        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, debugger.getEncoderColorFormat());
        //mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat);
        Log.d(TAG, "color format" + mColorFormat);

        //I/ACodec﹕ setupVideoEncoder succeeded
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();


        /**this sentence is quite comment for test*/
        //  for (int i = 0; i < 10; i++) mCamera.addCallbackBuffer(new byte[convertor.getBufferSize()]);
        // mCamera.setPreviewCallbackWithBuffer(callback);
        IFrameCallback mIFrameCallback = new IFrameCallback() {

            @Override
            public void onFrame(final ByteBuffer frame) {

                //every frame is 460800
                long result = System.nanoTime() / 1000;
                ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
                if (inputBuffers == null) Log.d(TAG, "inputBuffer is null");

                int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);//10[msec]
                if (inputBufferIndex >= 0) {
                    final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                    inputBuffer.clear();
                    frame.position(0);
                    byte[] data = new byte[frame.remaining()];
                    frame.get(data);
                    Log.d(TAG, ":" + data.length);
                    convertor.convert(data, inputBuffer);
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, inputBuffer.position(),
                            result, 0);

                } else if (inputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    Log.d(TAG, "inputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER" + mBufferInfo.size);
                    // wait for MediaCodec encoder is ready to encode
                    // nothing to do here because MediaCodec#dequeueInputBuffer(TIMEOUT_USEC)
                    // will wait for maximum TIMEOUT_USEC(10msec) on each call
                } else if (inputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat format = mMediaCodec.getOutputFormat();
                    Log.d(TAG, "outIndex:" + format);

                }
                //}
            }
        };

        mCamera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_NV21);

        // The packetizer encapsulates the bit stream in an RTP stream and send it over the network

        mPacketizer.setInputStream(new MediaCodecInputStream(mMediaCodec));
        mPacketizer.start();

        mStreaming = true;
        // Log.d(TAG, "sendEOS is true");

    }

    ///
    protected final MediaCodecInfo selectVideoCodec(final String mimeType) {

        // get the list of available codecs
        final int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {    // skipp decoder
                continue;
            }
            // select first codec that match a specific MIME type and color format
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    final int format = selectColorFormat(codecInfo, mimeType);
                    if (format > 0) {
                        mColorFormat = format;
                        return codecInfo;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void encodeWithMediaCodecMethod2() throws RuntimeException, IOException {
        Log.d(TAG, "Video encoded using the MediaCodec API with a surface");

        // Updates the parameters of the camera if needed
        // createCamera();
        updateCamera();

        // Estimates the frame rate of the camera
        // consider using the default quality , the framerate is fixed
        measureFramerate();

        EncoderDebugger debugger = EncoderDebugger.debug(mSettings, mQuality.resX, mQuality.resY);

        mMediaCodec = MediaCodec.createByCodecName(debugger.getEncoderName());
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", mQuality.resX, mQuality.resY);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mQuality.bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mQuality.framerate);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        Surface surface = mMediaCodec.createInputSurface();
        ((SurfaceView) mSurfaceView).addMediaCodecSurface(surface);
        //mSurfaceView=surface;
        mMediaCodec.start();
        Log.d(TAG, "mediacodec.start" + mQuality.bitrate + "," + mQuality.framerate);
        // The packetizer encapsulates the bit stream in an RTP stream and send it over the network
        if (mMediaCodec == null) {
            Log.d(TAG, "mMediacodec");
        }


        mPacketizer.setInputStream(new MediaCodecInputStream(mMediaCodec));
        mPacketizer.start();

        mStreaming = true;

    }

    /**
     * Video encoding is done by a MediaRecorder.
     */

    public void encodeWithMediaRecorder() throws IOException, ConfNotSupportedException {
        //after search the Internet, I found if you need to encode with mediaRecorder, you need to write basic library like c
        Log.d(TAG, "Video encoded using the MediaRecorder API");

        // We need a local socket to forward data output by the camera to the packetizer
        createSockets();

        // Reopens the camera if needed
        destroyCamera();

        // The camera must be unlocked before the MediaRecorder can use it

        try {
            //  mCamera = getCameraInstance();
            mMediaRecorder = new MediaRecorder();
            // mMediaRecorder.setCamera(mCamera);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setVideoEncoder(mVideoEncoder);
            mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
            mMediaRecorder.setVideoSize(mRequestedQuality.resX, mRequestedQuality.resY);
            mMediaRecorder.setVideoFrameRate(mRequestedQuality.framerate);

            // The bandwidth actually consumed is often above what was requested
            mMediaRecorder.setVideoEncodingBitRate((int) (mRequestedQuality.bitrate * 0.8));

            // We write the output of the camera in a local socket instead of a file !
            // This one little trick makes streaming feasible quiet simply: data from the camera
            // can then be manipulated at the other end of the socket
            FileDescriptor fd = null;
            if (sPipeApi == PIPE_API_PFD) {
                fd = mParcelWrite.getFileDescriptor();
            } else {
                fd = mSender.getFileDescriptor();
            }
            mMediaRecorder.setOutputFile(fd);

            mMediaRecorder.prepare();
            mMediaRecorder.start();

        } catch (Exception e) {
            throw new ConfNotSupportedException(e.getMessage());
        }

        InputStream is = null;

        if (sPipeApi == PIPE_API_PFD) {
            is = new ParcelFileDescriptor.AutoCloseInputStream(mParcelRead);
        } else {
            is = mReceiver.getInputStream();
        }

        // This will skip the MPEG4 header if this step fails we can't stream anything :(
        try {
            byte buffer[] = new byte[4];
            // Skip all atoms preceding mdat atom
            while (!Thread.interrupted()) {
                while (is.read() != 'm') ;
                is.read(buffer, 0, 3);
                if (buffer[0] == 'd' && buffer[1] == 'a' && buffer[2] == 't') break;
            }
        } catch (IOException e) {
            Log.e(TAG, "Couldn't skip mp4 header :/");
            stop();
            throw e;
        }

        // The packetizer encapsulates the bit stream in an RTP stream and send it over the network
        mPacketizer.setInputStream(is);
        mPacketizer.start();

        mStreaming = true;

    }

    /**
     * Computes the average frame rate at which the preview callback is called.
     * We will then use this average frame rate with the MediaCodec.
     * Blocks the thread in which this function is called.
     */
    @Override
    public void measureFramerate() {
        final Semaphore lock = new Semaphore(0);


        IFrameCallback callback = new IFrameCallback() {
            int i = 0, t = 0;
            long now, oldnow, count = 0;

            @Override
            public void onFrame(ByteBuffer frame) {
                i++;
                now = System.nanoTime() / 1000;
                if (i > 3) {
                    t += now - oldnow;
                    count++;
                }
                if (i > 20) {
                    mQuality.framerate = (int) (1000000 / (t / count) + 1);
                    lock.release();
                }
                oldnow = now;
            }
        };
        // mCamera.setPreviewCallback(callback);
        mCamera.setFrameCallback(callback, UVCCamera.PIXEL_FORMAT_NV21);
        try {
            lock.tryAcquire(2, TimeUnit.SECONDS);
            Log.d(TAG, "Actual framerate: " + mQuality.framerate);
            if (mSettings != null) {
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putInt(PREF_PREFIX + "fps" + mRequestedQuality.framerate + "," + mCameraImageFormat + "," + mRequestedQuality.resX + mRequestedQuality.resY, mQuality.framerate);
                editor.commit();
            }
        } catch (InterruptedException e) {
        }

        mCamera.setFrameCallback(null, 0);

    }


}
