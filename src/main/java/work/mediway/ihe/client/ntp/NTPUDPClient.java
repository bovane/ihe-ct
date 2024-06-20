package work.mediway.ihe.client.ntp;

import org.apache.commons.net.DatagramSocketClient;
import org.apache.commons.net.ntp.NtpV3Impl;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * @author BoVane
 * @version 1.0
 * @description: TODO
 * @date 2023/6/5 14:27
 */

public final class NTPUDPClient extends DatagramSocketClient {
    private int DEFAULT_PORT = 123;
    private int version = 3;


    public NTPUDPClient() {
    }

    public void setDEFAULT_PORT(int DEFAULT_PORT) {
        this.DEFAULT_PORT = DEFAULT_PORT;
    }

    /**
     * 获取NTP服务器时间 TimeInfo 对象
     *
     * @author bovane
     * [host, port]
     * @return org.apache.commons.net.ntp.TimeInfo
     */
    public TimeInfo getTime(InetAddress host, int port) throws IOException {
        if (!this.isOpen()) {
            this.open();
        }

        NtpV3Packet message = new NtpV3Impl();
        message.setMode(3);
        message.setVersion(this.version);
        DatagramPacket sendPacket = message.getDatagramPacket();
        sendPacket.setAddress(host);
        sendPacket.setPort(port);
        NtpV3Packet recMessage = new NtpV3Impl();
        DatagramPacket receivePacket = recMessage.getDatagramPacket();
        TimeStamp now = TimeStamp.getCurrentTime();
        message.setTransmitTime(now);
        this._socket_.send(sendPacket);
        this._socket_.receive(receivePacket);
        long returnTimeMillis = System.currentTimeMillis();
        return new TimeInfo(recMessage, returnTimeMillis, false);
    }

    public TimeInfo getTime(InetAddress host) throws IOException {
        return this.getTime(host, DEFAULT_PORT);
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}