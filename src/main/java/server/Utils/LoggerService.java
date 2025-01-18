package server.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerService {

    public static void log (String message) {
        System.out.printf("%s\t%s\n",getDate(), message);
    }

    public static void logerror (String message) {
        System.err.printf("%s\t%s\n",getDate(), message);
    }

    private static String getDate() {
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return fechaHoraActual.format(formatter);
    }
}
