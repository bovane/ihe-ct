package work.mediway.ihe.client.timezone;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import work.mediway.ihe.client.dto.ServerInfoDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author BoVane
 * @version 1.0
 * @description: TODO
 * @date 2023/7/25 19:39
 */
@Component
@Data
@Slf4j
public class TimeZoneClient {

    @Value("${app.config.ntp-server-address}")
    private String timeZoneAddress;
    @Value("${app.config.timeZonePort}")
    private int timeZonePort;
    public String getByBio(ServerInfoDTO serverInfoDTO)  {
        if (StrUtil.isNotEmpty(serverInfoDTO.getNtpServer())) {
            timeZoneAddress = serverInfoDTO.getNtpServer();
        }
        String zone = null;
        // 通过 web socket 获取 时区信息
        try (Socket socket = new Socket(timeZoneAddress, timeZonePort)) {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            output.println("request");

            // 获取到返回信息
            String response = input.readLine();
            log.warn("当前时间服务器所在时区为 " + response);
            zone = response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.error(zone);
        return zone;
    }
    public static void main(String[] args) {
        String timeZoneAddress = "127.0.0.1";
//        String timeZoneAddress = "192.168.0.107";
        int timeZonePort = 1234;
        TimeZoneClient timeZoneClient = new TimeZoneClient();
        String zone = null;
        try (Socket socket = new Socket(timeZoneAddress, timeZonePort)) {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            output.println("request");

            String response = input.readLine();
            System.out.println("Server response: " + response);
            zone = response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.error(zone);
    }
}
