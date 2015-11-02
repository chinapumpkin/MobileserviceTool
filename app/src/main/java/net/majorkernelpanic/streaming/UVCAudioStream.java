package net.majorkernelpanic.streaming;

import android.media.MediaRecorder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dengcanrong on 15/7/4.
 */
public class UVCAudioStream extends MediaStream implements IAudioStream {
    protected int mAudioSource;
    protected int mOutputFormat;
    protected int mAudioEncoder;
    protected AudioQuality mRequestedQuality = AudioQuality.DEFAULT_AUDIO_QUALITY.clone();
    protected AudioQuality mQuality = mRequestedQuality.clone();

    public UVCAudioStream() {
        setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
    }

    @Override
    public void setAudioSource(int audioSource) {
        mAudioSource = audioSource;
    }


    @Override
    public void setAudioEncoder(int audioEncoder) {
        mAudioEncoder = audioEncoder;

    }

    @Override
    public void setOutputFormat(int outputFormat) {
        mOutputFormat = outputFormat;

    }

    public void encodeWithMediaRecorder() throws IOException {
        // We need a local socket to forward data output by the camera to the packetizer
        createSockets();

        Log.v(TAG, "Requested audio with " + mQuality.bitRate / 1000 + "kbps" + " at " + mQuality.samplingRate / 1000 + "kHz");

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(mAudioSource);
        mMediaRecorder.setOutputFormat(mOutputFormat);
        mMediaRecorder.setAudioEncoder(mAudioEncoder);
        mMediaRecorder.setAudioChannels(1);
        mMediaRecorder.setAudioSamplingRate(mQuality.samplingRate);
        mMediaRecorder.setAudioEncodingBitRate(mQuality.bitRate);

        // We write the ouput of the camera in a local socket instead of a file !
        // This one little trick makes streaming feasible quiet simply: data from the camera
        // can then be manipulated at the other end of the socket
        FileDescriptor fd = null;
        if (sPipeApi == PIPE_API_PFD) {
            fd = mParcelWrite.getFileDescriptor();
        } else {
            fd = mSender.getFileDescriptor();
        }
        mMediaRecorder.setOutputFile(fd);
        mMediaRecorder.setOutputFile(fd);

        mMediaRecorder.prepare();
        mMediaRecorder.start();

        InputStream is = null;

        if (sPipeApi == PIPE_API_PFD) {
            is = new ParcelFileDescriptor.AutoCloseInputStream(mParcelRead);
        } else {
            try {
                // mReceiver.getInputStream contains the data from the camera
                is = mReceiver.getInputStream();
            } catch (IOException e) {
                stop();
                throw new IOException("Something happened with the local sockets :/ Start failed !");
            }
        }

        // the mPacketizer encapsulates this stream in an RTP stream and send it over the network
        mPacketizer.setInputStream(is);
        mPacketizer.start();
        mStreaming = true;

    }



}
