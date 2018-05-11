package apps.android.loref;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Created by 105053228 on 07-Mar-18.
 */

public class GeneralUtilitiesLibrary {

    private static final long MINUTE_MS=60000L;
    private static final  long HOUR_MS=3600000L;
    private static final long DAY_MS=86400000L;
    private static final long WEEK_MS=604800000L;
    private static final long MONTH_MS=2419200000L;
    private static final long YEAR_MS=29030400000L;

    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();

        return output;

    }

    public static long getTimeMillis(String timeStamp, String format){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Calendar calendar = simpleDateFormat.getCalendar();

        try {
            calendar.setTime(simpleDateFormat.parse(timeStamp));
            return calendar.getTimeInMillis();
        } catch (ParseException e) {
            return -1;
        }

    }

    public static String getTimeElapsed(long when){

        long timeDiff = System.currentTimeMillis()-when;
        String unit;
        String quantity;
        String prefix;
        String suffix;

        if((timeDiff)<MINUTE_MS){
            // now
            unit="now";
            quantity="";
            prefix="";
            suffix="";

        } else if ((timeDiff)<HOUR_MS) {
            // muinutes
            unit="minutes";
            quantity="" + (int) (timeDiff/MINUTE_MS);
            prefix="";
            suffix="ago";

        } else if ((timeDiff)<DAY_MS) {
            //hours
            unit="hours";
            quantity="" + (int) (timeDiff/HOUR_MS);
            prefix="";
            suffix="ago";

        } else if ((timeDiff)<WEEK_MS) {
            //days
            unit="days";
            quantity=""+ (int) (timeDiff/DAY_MS);
            prefix="";
            suffix="ago";

        } else if ((timeDiff)<MONTH_MS) {
            //weeks
            unit="weeks";
            quantity="" + (int) (timeDiff/WEEK_MS);
            prefix="";
            suffix="ago";

        } else if ((timeDiff)<YEAR_MS) {
            // months
            unit="months";
            quantity=""+ (int) (timeDiff/MONTH_MS);
            prefix="";
            suffix="ago";

        } else {
            // years
            unit="years";
            quantity=""+ (int) (timeDiff/YEAR_MS);
            prefix="";
            suffix="ago";

        }

        return (String.format("%s %s %s %s", prefix, quantity, unit, suffix));

    }

}
