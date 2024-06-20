package work.mediway.ihe.client.timezone;

/**
 * 测试获取时区 TimeZone
 * @author bovane bovane.ch@gmail.com
 * @date 2024/1/4
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ZoneClientTest {
    public static void main(String[] args) {
        String serverName = "43.138.135.196";
//        String serverName = "127.0.0.1";
//        String serverName = "192.168.0.107";
        int port = 1234;

        for (int i=0; i<10; i++) {
            try (Socket socket = new Socket(serverName, port)) {
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                output.println("request");

                String response = input.readLine();
                System.out.println("Server response: " + response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

