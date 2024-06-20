package work.mediway.ihe.client.timer;

import lombok.extern.slf4j.Slf4j;
import work.mediway.ihe.client.exec.SystemCmd;
import work.mediway.ihe.client.ntp.NtpClient;
import work.mediway.ihe.client.timezone.TimeZoneClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author BoVane
 * @version 1.0
 * @description: TODO
 * @date 2023/7/6 13:28
 */
@Component
@Slf4j
public class SyncTask {

    @Resource
    private NtpClient ntpClient;
    @Resource
    private SystemCmd systemCmd;
    @Resource
    private TimeZoneClient timeZoneClient;

//    @Scheduled(cron = "0/2 * * * * ?")
//    public void doTask() {
//        systemCmd.setWindowsTimeZone(timeZoneClient.getByBio());
//        Date serverTime = ntpClient.getServerTime();
//        ExecResult execResult = systemCmd.changeTime(serverTime);
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");
//        TimeZone timeZone = systemCmd.getTimeZone();
//        simpleDateFormat.setTimeZone(timeZone);
//        log.info("时区:" + timeZone.getID() +" 时间:" +simpleDateFormat.format(ntpClient.getServerTime()));
//        log.info("处理结果:" + execResult.toString());
//    }
}
