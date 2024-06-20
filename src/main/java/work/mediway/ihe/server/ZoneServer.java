package work.mediway.ihe.server;

/**
 * 时区服务器,返回TimeZone
 * @author bovane bovane.ch@gmail.com
 * @date 2024/1/4
 */

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

@Slf4j
public class ZoneServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(1234)) {
            System.out.println("Server is running and listening on port 1234");
            System.out.println("enter stop to exit ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                final long rcvTime = System.currentTimeMillis();
                handlePacket(socket,rcvTime);
                socket.close();
                // 读取控制台输入并检查是否为 "stop"，如果是则退出循环
                userInput = br.readLine();
                if (userInput.trim().equalsIgnoreCase("stop")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    protected static void handlePacket(final Socket clientSocket, final long rcvTime) {
        // 通过客户端套接字的输入流来接收信息
        InputStream input = clientSocket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String message = reader.readLine();
        // 处理接收到的信息
        log.warn("接收到的信息：" + message);
        log.info(String.format("NTP packet from %s mode=%s Server current time is %s  %n", clientSocket.getInetAddress().getHostAddress(),
                "Client", formatDate(new Date(rcvTime))));
        String zoneId = getServerZone();
        log.error("当前时区ID为【{}】",zoneId);
        // 接收到客户端请求后，向客户端发送时区ID信息
        OutputStream output = clientSocket.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output),true);
        writer.println(zoneId);
        // 关闭套接字和服务器套接字
    }
    public static String getServerZone() {
        // 保证获取的是系统时区,而不是 Java 虚拟机的时区
        synchronized (TimeZone.class) {
            TimeZone.setDefault(null);
            System.setProperty("user.timezone", "");
            TimeZone.getDefault();
        }
        TimeZone timeZone = TimeZone.getDefault();
        return timeZone.getID();
    }

    public static String formatDate(Date date) {
        // 创建一个 SimpleDateFormat 对象来定义日期格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

        // 使用 String.format 方法将 Date 类型的日期格式化为字符串
        String formattedDate = String.format("%s", dateFormat.format(date));
        return formattedDate;
    }
}

