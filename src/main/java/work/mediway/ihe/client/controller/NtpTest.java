package work.mediway.ihe.client.controller;


import work.mediway.ihe.client.dto.ServerInfoDTO;
import work.mediway.ihe.client.entity.ExecResult;
import work.mediway.ihe.client.exec.SystemCmd;
import work.mediway.ihe.client.ntp.NtpClient;
import work.mediway.ihe.client.timezone.TimeZoneClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @author BoVane
 * @version 1.0
 * @description: TODO
 * @date 2023/6/5 10:54
 */
@RestController
@RequestMapping("/ntpAnysc")
public class NtpTest {
    @Resource
    private NtpClient ntpClient;
    @Resource
    private SystemCmd systemCmd;
    @Resource
    private TimeZoneClient timeZoneClient;

    @GetMapping("/getTime")
    @ResponseBody
    public String getTime(ServerInfoDTO serverInfoDTO) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");
        // 获取本机时区
        TimeZone timeZone = systemCmd.getTimeZone();
        simpleDateFormat.setTimeZone(timeZone);
        return "时区:" + timeZone.getID() +" 时间:" +simpleDateFormat.format(ntpClient.getServerTime(serverInfoDTO));
    }

    @GetMapping("/syncTime")
    @ResponseBody
    public ExecResult syncTime(ServerInfoDTO serverInfoDTO) {
        // 设置时区
        this.syncWindowsTimezone(serverInfoDTO);
        // 设置时间
        return systemCmd.changeTime(ntpClient.getServerTime(serverInfoDTO));
    }

    public void syncWindowsTimezone(ServerInfoDTO serverInfoDTO){
        systemCmd.setWindowsTimeZone(timeZoneClient.getByBio(serverInfoDTO));
    }
    @GetMapping("/getZone")
    public String getTimeZone(ServerInfoDTO serverInfoDTO){
        return timeZoneClient.getByBio(serverInfoDTO);
    }
}
