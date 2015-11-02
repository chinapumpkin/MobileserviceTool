package net.majorkernelpanic.streaming;

import java.io.IOException;

/**
 * Created by dengcanrong on 15/7/3.
 */
public interface IAudioStream extends IMediaStream {


    public void setAudioSource(int audioSource);

    /**
     * Returns the quality of the stream.
     */
    // public AudioQuality getAudioQuality();

    // public void setAudioQuality(AudioQuality quality);

    void setAudioEncoder(int audioEncoder);

    void setOutputFormat(int outputFormat);
    void encodeWithMediaRecorder() throws IOException;


}
