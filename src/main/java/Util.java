import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Static methods providing basic functionality to be used by multiple classes.
 */
public class Util {

    /**
     * Helper method. Formats the time delta between {@code before} and {@code after} to a string with human readable
     * time difference in days, hours, minutes, and seconds.
     * @param before Start time instance.
     * @param after End time instance.
     * @return Human-readable string.
     */
    public static String getDeltaTimeString(Instant before, Instant after){

        // unfortunately Java 1.9 which is currently incompatible with coveralls maven plugin...
        //long days = Duration.between(before, after).toDaysPart();
        //long hours = Duration.between(before, after).toHoursPart();
        //long minutes = Duration.between(before, after).toMinutesPart();
        //long seconds = Duration.between(before, after).toSecondsPart();

        Duration delta = Duration.between(before, after);

        long days = delta.toDays();
        long hours = days > 0 ? delta.toHours() % (days * 24) : delta.toHours();


        long minutesModuloPart = days * 24 * 60 + hours * 60;
        long minutes = minutesModuloPart > 0 ? delta.toMinutes() % (minutesModuloPart) : delta.toMinutes();

        long secondsModuloPart = days * 24 * 60 * 60 + hours * 60 * 60 + minutes * 60;
        long seconds = secondsModuloPart > 0 ? TimeUnit.MILLISECONDS.toSeconds(delta.toMillis()) % (secondsModuloPart) : TimeUnit.MILLISECONDS.toSeconds(delta.toMillis());

        String result = "Days: " + days + "\n";
        result += "Hours: " + hours + "\n";
        result += "Minutes: " + minutes + "\n";
        result += "Seconds: " + seconds + "\n";
        return result;
    }

}
