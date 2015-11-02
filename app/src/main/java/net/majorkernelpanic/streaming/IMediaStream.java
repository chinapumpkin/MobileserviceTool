package net.majorkernelpanic.streaming;

import net.majorkernelpanic.streaming.rtp.AbstractPacketizer;

import java.io.IOException;

/**
 * Created by dengcanrong on 15/7/3.
 */
public interface IMediaStream extends Stream {


    /**
     * Returns the streaming method in use, call this after
     * {@link #configure()} to get an accurate response.
     */
    public byte getStreamingMethod();

    /**
     * A MediaRecorder that streams what it records using a packetizer from the RTP package.
     * You can't use this class directly !
     */


    public void setStreamingMethod(byte mode);

    /**
     * Returns the packetizer associated with the {@link MediaStream}.
     *
     * @return The packetizer
     */
    public AbstractPacketizer getPacketizer();

    /**
     * Returns an approximation of the bit rate consumed by the stream in bit per seconde.
     */



    /**media recorder
    * **/
    void encodeWithMediaRecorder() throws IOException;

   /* MediaCodec , it requires android 4.3 and up.*/
    void encodeWithMediaCodec() throws IOException;

    /**
     * Returns a description of the stream using SDP.
     * This method can only be called after {@link Stream#configure()}.
     * @throws IllegalStateException Thrown when {@link Stream#configure()} was not called.
     */


    /**
     * Returns the SSRC of the underlying {@link net.majorkernelpanic.streaming.rtp.RtpSocket}.
     *
     * @return the SSRC of the stream
     */
    void createSockets() throws IOException;

    void closeSockets();
    String getSessionDescription();
}
