package work.mediway.ihe.client.exec;

import lombok.Getter;
import lombok.Setter;
import work.mediway.ihe.client.entity.ExecResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * @author BoVane
 * @version 1.0
 * @description: TODO
 * @date 2023/6/5 15:24
 */
@Component
@Getter
@Setter
public class SystemCmd {
    @Value("${app.config.charset-linux}")
    private String charsetLinux;
    @Value("${app.config.charset-windows}")
    private String charsetWindows;
    @Value("${app.config.cmd-timeout}")
    private long timeout;

    private String charset;
    private String OS;

    @PostConstruct
    private void init() {
        this.getOS();
        if ("windows".equals(OS)) {
            this.charset = this.charsetWindows;
        } else {
            this.charset = this.charsetLinux;
        }
    }

    public TimeZone getTimeZone() {
        String zone;
        if ("windows".equals(OS)) {
            String[] zoneCmd = new String[]{"tzutil", "/g"};
            ExecResult execResult = execCmd(zoneCmd);
            zone = execResult.isSuccess() ? execResult.getResult() : "China Standard Time";
            zone = com.ibm.icu.util.TimeZone.getIDForWindowsID(zone, null);
        } else if ("linux".equals(OS)) {
            String[] zoneCmd = new String[]{"timedatectl | grep Time"};
            ExecResult execResult = execCmd(zoneCmd);
            if (execResult.isSuccess()) {
                zone = execResult.getResult();
                zone = zone.substring("Time zone: ".length(), zone.indexOf("("));
            } else {
                zone = "Asia/Shanghai";
            }
        } else {
            String[] zoneCmd = new String[]{"sudo", "systemsetup", "-gettimezone"};

            ExecResult execResult = execCmd(zoneCmd);
            if (execResult.isSuccess()) {
                zone = execResult.getResult();
                zone = zone.substring("Time zone: ".length());
            } else {
                zone = "Asia/Shanghai";
            }
        }
        return TimeZone.getTimeZone(zone);
    }

    public ExecResult setWindowsTimeZone(String zone) {
        if (!"windows".equals(this.OS)) {
            ExecResult execResult = new ExecResult();
            execResult.setSuccess(false);
            execResult.setResult("OS is not windows");
            return execResult;
        }
        String[] cmd;
        cmd = new String[]{"cmd", "/c", "tzutil", "/s", zone};
        return this.execCmd(cmd);
    }

    public ExecResult changeTime(Date time) {
        String[] cmd;
        TimeZone timeZone = getTimeZone();
        if ("windows".equals(this.OS)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(timeZone);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            timeFormat.setTimeZone(timeZone);
            SimpleDateFormat millsFormat = new SimpleDateFormat("SSS");
            int mills = Integer.parseInt(millsFormat.format(time)) / 10;
            cmd = new String[]{"cmd", "/c", "time", timeFormat.format(time) + "." + mills, "&&", "cmd.exe", "/c", "date", dateFormat.format(time)};
            return this.execCmd(cmd);

        } else if ("linux".equals(this.OS)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            simpleDateFormat.setTimeZone(timeZone);
            cmd = new String[]{"date", "-s", simpleDateFormat.format(time)};
            return this.execCmd(cmd);

        } else {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMddHHmmyyyy");
            simpleDateFormat.setTimeZone(timeZone);
            String format = simpleDateFormat.format(time);
            cmd = new String[]{"sudo", "date", format};
            return this.execCmd(cmd);

        }
    }

    private void getOS() {
        String os = System.getProperty("os.name");
        if (os.matches("^(?i)Windows.*$")) {
            this.OS = "windows";
            return;
        } else if (os.matches("^(?i)Linux.*$")) {
            this.OS = "linux";
            return;
        } else if (os.matches("^(?i)Mac.*$")) {
            this.OS = "Mac";
            return;
        }
        this.OS = os;
    }


    public ExecResult execCmd(String[] cmd) {
        ExecResult execResult = new ExecResult();
        StringBuilder result = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor(timeout, TimeUnit.MILLISECONDS);
            BufferedReader correct = new BufferedReader(new InputStreamReader(process.getInputStream(), charset));
            BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream(), charset));
            String line;
            execResult.setSuccess(true);
            while ((line = correct.readLine()) != null) {
                if (result.length() != 0) {
                    result.append('\n');
                }
                result.append(line);
            }
            while ((line = error.readLine()) != null) {
                if (result.length() != 0) {
                    result.append('\n');
                }
                result.append(line);
                execResult.setSuccess(false);
            }
            execResult.setResult(result.toString());
        } catch (Exception e) {
            execResult.setSuccess(false);
            e.printStackTrace();
        }
        if ("".equals(execResult.getResult())) {
            execResult.setResult("No handing result");
        }
        return execResult;
    }
}
