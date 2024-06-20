package work.mediway.ihe.client.ntp;



import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;
import work.mediway.ihe.client.dto.ServerInfoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author BoVane
 * @version 1.0
 * @description: TODO
 * @date 2023/6/5 10:48
 */
@Component
@Slf4j
public class NtpClient {
    @Value("${app.config.ntp-server-address}")
    private String ntpServerAddress;
    @Value("${app.config.ntp-server-port}")
    private int ntpServerPort;
    @Value("${app.config.ntp-version}")
    private int ntpVersion;
    @Value("${app.config.ntp-timeout}")
    private int ntpTimeout;

    /**
     * 获取 NTP 服务器的时间
     *
     * @author bovane
     * @return java.util.Date
     */
    public Date getServerTime(ServerInfoDTO serverInfoDTO) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");
        if (StrUtil.isNotEmpty(serverInfoDTO.getNtpServer())) {
            ntpServerAddress = serverInfoDTO.getNtpServer();
        }
        log.warn("当前ntpServer服务地址为: " + ntpServerAddress);
        // 设置连接信息
        NTPUDPClient ntpudpClient = new NTPUDPClient();
        ntpudpClient.setDEFAULT_PORT(ntpServerPort);
        ntpudpClient.setDefaultTimeout(ntpTimeout);
        ntpudpClient.setVersion(ntpVersion);
        Date date = null;
        try {
            // 连接NTP服务器并获取时间
            InetAddress address = InetAddress.getByName(ntpServerAddress);
            TimeInfo timeInfo = ntpudpClient.getTime(address);
            TimeStamp timeStamp = timeInfo.getMessage().getTransmitTimeStamp();
            date = timeStamp.getDate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.warn("当前时间服务器时间为: " + simpleDateFormat.format(date));
        return date;
    }
}
