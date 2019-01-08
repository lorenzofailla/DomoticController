package apps.android.loref;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.util.Base64;

import com.example.generalutilitieslibrary.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Created by 105053228 on 07-Mar-18.
 */

public class GeneralUtilitiesLibrary {

    private static final long MINUTE_MS = 60000L;
    private static final long HOUR_MS = 3600000L;
    private static final long DAY_MS = 86400000L;
    private static final long WEEK_MS = 604800000L;
    private static final long MONTH_MS = 2419200000L;
    private static final long YEAR_MS = 29030400000L;

    public static HashMap<String, Object> parseJSON(String code) {

        HashMap<String, Object> result = new HashMap<>();
        JSONObject input = null;

        try {

            input = new JSONObject(code);

            Iterator<String> keys = input.keys();
            while (keys.hasNext()) {

                result.put(keys.next(), input.getString(keys.next()));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return result;

    }

    public static String decode(String rawData) {

        try {

            return new String(Base64.decode(rawData, Base64.DEFAULT), "UTF-8");

        } catch (IOException e) {

            return e.getMessage();

        }

    }

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

    public static long getTimeMillis(String timeStamp, String format) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Calendar calendar = simpleDateFormat.getCalendar();

        try {
            calendar.setTime(simpleDateFormat.parse(timeStamp));
            return calendar.getTimeInMillis();
        } catch (ParseException e) {
            return -1;
        }

    }

    public static String getTimeElapsed(long when, Context context) {

        return getTimeElapsed(when, context, true);

    }

    public static String getTimeElapsed(long when, Context context, boolean longFormat) {

        long timeDiff = System.currentTimeMillis() - when;
        String unit;
        int numericQuantity;
        String quantity;
        String prefix;
        String suffix;

        if ((timeDiff) < MINUTE_MS) {
            // now
            if (longFormat) {
                unit = context.getString(R.string.minute);
                quantity = context.getString(R.string.now);
            } else {
                unit = context.getString(R.string.now_compact);
                quantity = "";
            }

            prefix = "";
            suffix = context.getString(R.string.ago);

        } else if ((timeDiff) < HOUR_MS) {
            // minutes

            numericQuantity = (int) (timeDiff / MINUTE_MS);
            if (longFormat) {
                if (numericQuantity > 1) {
                    unit = context.getString(R.string.minutes);
                } else {
                    unit = context.getString(R.string.minute);
                }

            } else {
                unit = context.getString(R.string.minute_compact);
            }
            quantity = "" + numericQuantity;
            prefix = "";
            suffix = context.getString(R.string.ago);

        } else if ((timeDiff) < DAY_MS) {
            //hours
            numericQuantity = (int) (timeDiff / HOUR_MS);
            if (longFormat) {
                if (numericQuantity > 1) {
                    unit = context.getString(R.string.hours);
                } else {
                    unit = context.getString(R.string.hour);
                }

            } else {
                unit = context.getString(R.string.hour_compact);
            }
            quantity = "" + numericQuantity;
            prefix = "";
            suffix = context.getString(R.string.ago);

        } else if ((timeDiff) < WEEK_MS) {
            //days
            numericQuantity = (int) (timeDiff / DAY_MS);
            if (longFormat) {
                if (numericQuantity > 1) {
                    unit = context.getString(R.string.days);
                } else {
                    unit = context.getString(R.string.day);
                }

            } else {
                unit = context.getString(R.string.day_compact);
            }
            quantity = "" + (int) (timeDiff / DAY_MS);
            prefix = "";
            suffix = context.getString(R.string.ago);

        } else if ((timeDiff) < MONTH_MS) {
            //weeks
            numericQuantity = (int) (timeDiff / WEEK_MS);
            if (longFormat) {
                if (numericQuantity > 1) {
                    unit = context.getString(R.string.weeks);
                } else {
                    unit = context.getString(R.string.week);
                }

            } else {
                unit = context.getString(R.string.week_compact);
            }
            quantity = "" + (int) (timeDiff / WEEK_MS);
            prefix = "";
            suffix = context.getString(R.string.ago);

        } else if ((timeDiff) < YEAR_MS) {
            // months
            numericQuantity = (int) (timeDiff / MONTH_MS);
            if (longFormat) {
                if (numericQuantity > 1) {
                    unit = context.getString(R.string.months);
                } else {
                    unit = context.getString(R.string.month);
                }

            } else {
                unit = context.getString(R.string.month_compact);
            }
            quantity = "" + (int) (timeDiff / MONTH_MS);
            prefix = "";
            suffix = context.getString(R.string.ago);

        } else {
            // years
            numericQuantity = (int) (timeDiff / YEAR_MS);
            if (longFormat) {
                if (numericQuantity > 1) {
                    unit = context.getString(R.string.years);
                } else {
                    unit = context.getString(R.string.year);
                }

            } else {
                unit = context.getString(R.string.year_compact);
            }
            quantity = "" + (int) (timeDiff / YEAR_MS);
            prefix = "";
            suffix = context.getString(R.string.ago);
        }


        if (longFormat) {

            return (prefix + " " + quantity + " " + unit + " " + suffix);

        } else {

            return (quantity + " " + unit);
        }

    }

}
