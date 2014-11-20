package dropbox.server.Util;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by micky on 2014. 11. 8..
 * 로그를 남기는 클래스
 */
public class Logger {
    public static void logging(String message) {
        logging(message, System.out, null);
    }

    public static void errorLogging(String message, Throwable t) {
        logging(message, System.err, t);
    }

    public static void errorLogging(Throwable t) {
        logging(null, System.err, t);
    }

    public static void logging(String message, PrintStream target) {
        logging(message, target, null);
    }

    public static void logging(String message, Throwable e) {
        logging(message, e);
    }

    public static void logging(String message, PrintStream target,  Throwable t) {
        DateFormat df = new SimpleDateFormat("MMM dd a hh:mm:ss");
        target.printf("[%s] %s",df.format(Calendar.getInstance().getTime()),  message == null?"":message);
        if(t != null) {
            target.print(t.getLocalizedMessage());
        }
        target.println();
    }

    public static void debuglogging(String message) {
        logging(message);
    }


}
