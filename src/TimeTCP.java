import java.util.Calendar;
import java.util.Date;

public class TimeTCP {

    static double setTime(){

        Date date = new Date();

        Calendar cal = Calendar.getInstance();

        cal.setTime(date);

        double minutes  = cal.get(Calendar.MINUTE);
        double seconds  = cal.get(Calendar.SECOND);

        seconds = seconds / 100;

        return minutes + seconds;
    }


}
