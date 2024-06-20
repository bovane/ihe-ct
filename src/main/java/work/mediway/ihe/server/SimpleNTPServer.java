
package work.mediway.ihe.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ntp.NtpUtils;
import org.apache.commons.net.ntp.NtpV3Impl;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

/**
 * The SimpleNTPServer class is a UDP implementation of a server for the Network
 * Time Protocol (NTP) version 3 as described in RFC 1305. It is a minimal NTP
 * server that doesn't actually adjust the time but only responds to NTP
 * datagram requests with response sent back to originating host with info
 * filled out using the current clock time. To be used for debugging or testing.
 *
 * To prevent this from interfering with the actual NTP service it can be run
 * from any local port.
 */
@Slf4j
public class SimpleNTPServer implements Runnable {
    private int port;
    
    private volatile boolean running;
    private boolean started;
    
    private DatagramSocket socket;

    /**
     * Create SimpleNTPServer listening on default NTP port.
     */
    public SimpleNTPServer() {
        this(NtpV3Packet.NTP_PORT);
    }

    /**
     * Create SimpleNTPServer.
     *
     * @param port the local port the server socket is bound to, or
     * <code>zero</code> for a system selected free port.
     * @throws IllegalArgumentException if port number less than 0
     */
    public SimpleNTPServer(final int port) {
        if (port < 0) {
            throw new IllegalArgumentException();
        }
        this.port = port;
    }
    
    public int getPort() {
        return port;
    }

    /**
     * Return state of whether time service is running.
     *
     * @return true if time service is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Return state of whether time service is running.
     *
     * @return true if time service is running
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Connect to server socket and listen for client connections.
     *
     * @throws IOException if an I/O error occurs when creating the socket.
     */
    public void connect() throws IOException {
        if (socket == null) {
            socket = new DatagramSocket(port);
            // port = 0 is bound to available free port
            if (port == 0) {
                port = socket.getLocalPort();
            }
            System.out.println("NTP-server v.1.0.1 running on port " + port + "/UDP");
        }
    }

    /**
     * Start time service and provide time to client connections.
     *
     * @throws IOException if an I/O error occurs when creating the
     * socket.
     */
    public void start() throws IOException {
        if (socket == null) {
            connect();
        }
        if (!started) {
            started = true;
            new Thread(this).start();
        }
    }

    /**
     * main thread to service client connections.
     */
    @Override
    public void run() {
        running = true;
        final byte buffer[] = new byte[48];
        final DatagramPacket request = new DatagramPacket(buffer, buffer.length);
        do {
            try {
                socket.receive(request);
                final long rcvTime = System.currentTimeMillis();
                handlePacket(request, rcvTime);
            } catch (final IOException e) {
                if (running) {
                    e.printStackTrace();
                }
                // otherwise socket thrown exception during shutdown
            }
        } while (running);
    }

    /**
     * Handle incoming packet. If NTP packet is client-mode then respond to that
     * host with a NTP response packet otherwise ignore.
     *
     * @param request incoming DatagramPacket
     * @param rcvTime time packet received
     *
     * @throws IOException if an I/O error occurs.
     */
    protected void handlePacket(final DatagramPacket request, final long rcvTime) throws IOException {
        final NtpV3Packet message = new NtpV3Impl();
        message.setDatagramPacket(request);
        log.info(String.format("NTP packet from %s mode=%s Server current time is %s  %n", request.getAddress().getHostAddress(),
                NtpUtils.getModeName(message.getMode()), formatDate(new Date(rcvTime))));
        if (message.getMode() == NtpV3Packet.MODE_CLIENT) {
            final NtpV3Packet response = new NtpV3Impl();
            
            response.setStratum(1);
            response.setMode(NtpV3Packet.MODE_SERVER);
            response.setVersion(NtpV3Packet.VERSION_3);
            response.setPrecision(-20);
            response.setPoll(0);
            response.setRootDelay(62);
            response.setRootDispersion((int) (16.51 * 65.536));

            // originate time as defined in RFC-1305 (t1)
            response.setOriginateTimeStamp(message.getTransmitTimeStamp());
            // Receive Time is time request received by server (t2)
            response.setReceiveTimeStamp(TimeStamp.getNtpTime(rcvTime));
            response.setReferenceTime(response.getReceiveTimeStamp());
            response.setReferenceId(0x4C434C00); // LCL (Undisciplined Local Clock)

            // Transmit time is time reply sent by server (t3)
            response.setTransmitTime(TimeStamp.getNtpTime(System.currentTimeMillis()));
            
            final DatagramPacket dp = response.getDatagramPacket();
            dp.setPort(request.getPort());
            dp.setAddress(request.getAddress());
            socket.send(dp);
        }
        // otherwise if received packet is other than CLIENT mode then ignore it
    }

    /**
     * Close server socket and stop listening.
     */
    public void stop() {
        running = false;
        if (socket != null) {
            socket.close();  // force closing of the socket
            socket = null;
        }
        started = false;
    }
    /**
     * 格式化日期
     *
     * @param date 日期
     * @return 格式化后的日期字符串，格式为：yyyy-MM-dd HH:mm:ss Z
     */
    public static String formatDate(Date date) {
        // 将获取默认时区的两个前置条件设置为 false, 令其获取系统时间而不是Java虚拟机的时间，原理见后面分析
        synchronized (TimeZone.class) {
            TimeZone.setDefault(null);
            System.setProperty("user.timezone", "");

            TimeZone.getDefault();
        }
        TimeZone timeZone = TimeZone.getDefault();
        return String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %2$s", date, timeZone.getID());
    }
    
    public static void main(final String[] args) {
        int port = NtpV3Packet.NTP_PORT;
        if (args.length != 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (final NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }
        final SimpleNTPServer timeServer = new SimpleNTPServer(port);
        try {
            timeServer.start();
            System.out.println("Enter 'stop' to close server");
            Scanner sc = new Scanner(System.in);
            new Thread(() -> {
                while (true) {
                    if (timeServer.isRunning() && sc.next().toLowerCase().trim().equals("stop")) {
                        timeServer.stop();
                        System.out.println("Server is closing");
                        System.exit(0);
                        break;
                    }
                }
            }).start();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }


}
