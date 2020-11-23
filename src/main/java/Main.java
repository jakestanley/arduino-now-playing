import api.SpotifyApiClient;
import com.google.gson.*;
import configuration.Configuration;
import file.ConfigurationLoader;
import lombok.extern.java.Log;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import serial.LcdData;
import serial.LcdDataProvider;
import serial.SerialClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RXTX binary builds provided as a courtesy of Fizzed, Inc. (http://fizzed.com/).
 * Please see http://fizzed.com/oss/rxtx-for-java for more information.
 */
@Log
public class Main {

    public static final int REFRESH_RATE = 1500;
    public static final String ARG_INTERFACE = "interface";
    public static final String ARG_SECRETS = "secrets";
    public static final int SECONDS = 1000;

    public static void main(String[] args) throws Exception {

        // command line parsing from https://stackoverflow.com/a/367714
        final Options options = new Options();
        final Option secrets = new Option("s", ARG_SECRETS, true, "path to a yaml file containing secrets");
        final Option iface = new Option("i", ARG_INTERFACE, true, "interface to use, i.e COM3, COM6");

        secrets.setRequired(true);
        options.addOption(secrets);
        options.addOption(iface);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            log.info(e.getMessage());
            formatter.printHelp("arduino-now-playing", options);
            System.exit(1);
        }


        final Configuration configuration = ConfigurationLoader.load(cmd.getOptionValue(ARG_SECRETS));

        log.info(configuration.toString());

        final SpotifyApiClient spotifyApiClient = new SpotifyApiClient(configuration);
        final SerialClient serialClient = SerialClient.builder()
                .chillMode(false)
                .iface(cmd.getOptionValue(ARG_INTERFACE)).build();

        final List<LcdDataProvider> providers = new ArrayList<>();
        providers.add(spotifyApiClient);

        while(true) {
//            serialClient.send(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            LocalDateTime now = LocalDateTime.now();
            final String dateStr = String.format("%d %s, %d", now.getDayOfMonth(), now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH), now.getYear());
            final String timeStr = String.format("%d:%d", now.getHour(), now.getMinute());

            serialClient.send(StringUtils.leftPad(StringUtils.rightPad(StringUtils.center(dateStr, 20) + StringUtils.center(timeStr, 20), 60), 20));
            Thread.sleep(2 * SECONDS);
        }

//        serialClient.send("this string is a string that is in fact eighty characters long. oh jeez rick....");

//        while (true) {
//
//            // TODO make this work with multiple providers
//
//            final LcdDataProvider provider = providers.get(0);
//            try {
//                final LcdData data = provider.getData();
//                serialClient.send(toJsonObject(data, false).toString());
//            } catch (final LcdDataProviderException e) {
//                log.info("LcdDataProviderException thrown");
//            } catch (final NullPointerException n) {
//                log.info(n.getMessage());
//            }
//
//            Thread.sleep(3000);
//        }
    }

    public static JsonObject toJsonObject(final LcdData lcdData, final Boolean scrolling) {

//        if (lcdData.getLines() > LcdData.MAX_LINES) {
//            throw new Exception(); // TODO
//        }

        final JsonObject json = new JsonObject();

        final String[] lines = lcdData.getLines();

        for (int i = 0; i < lines.length; i++) {
            final String line = scrolling ? lines[i] : lines[i].substring(0,Math.min(lines[i].length(),19));
            json.add(String.valueOf(i), new JsonPrimitive(line));
        }

        return json;
    }
}
