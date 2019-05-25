import api.LcdDataProviderException;
import api.SpotifyApiClient;
import com.google.gson.*;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import configuration.Configuration;
import file.ConfigurationLoader;
import lombok.extern.java.Log;
import org.apache.commons.cli.*;
import org.apache.commons.codec.binary.StringUtils;
import serial.LcdData;
import serial.LcdDataProvider;
import serial.SerialClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RXTX binary builds provided as a courtesy of Fizzed, Inc. (http://fizzed.com/).
 * Please see http://fizzed.com/oss/rxtx-for-java for more information.
 */
@Log
public class Main {

    public static final int REFRESH_RATE = 1500;

    public static void main(String[] args) throws InterruptedException {

        // command line parsing from https://stackoverflow.com/a/367714
        final Options options = new Options();
        final Option secrets = new Option("s", "secrets", true, "path to a yaml file containing secrets");
        secrets.setRequired(true);
        options.addOption(secrets);

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

        final Configuration configuration = ConfigurationLoader.load(cmd.getOptionValue("secrets"));

        log.info(configuration.toString());

        final SpotifyApiClient spotifyApiClient = new SpotifyApiClient(configuration);
        final SerialClient serialClient = new SerialClient(true);

        final List<LcdDataProvider> providers = new ArrayList<>();
        providers.add(spotifyApiClient);

        while (true) {

            // TODO make this work with multiple providers

            final LcdDataProvider provider = providers.get(0);
            try {
                final LcdData data = provider.getData();
                serialClient.send(toJsonObject(data, false).toString());
            } catch (final LcdDataProviderException e) {
                log.info("LcdDataProviderException thrown");
            } catch (final NullPointerException n) {
                log.info(n.getMessage());
            }

            Thread.sleep(3000);
        }
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
