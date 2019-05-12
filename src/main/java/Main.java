import api.SpotifyApiClient;
import com.google.gson.*;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import configuration.Configuration;
import file.ConfigurationLoader;
import lombok.extern.java.Log;
import org.apache.commons.cli.*;
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
        final SerialClient serialClient = new SerialClient();

        final List<LcdDataProvider> providers = new ArrayList<>();
        providers.add(spotifyApiClient);

        while (true) {

            // TODO make this work with multiple providers

            final LcdDataProvider provider = providers.get(0);
            serialClient.send(toJsonObject(provider.getData()).toString());

            Thread.sleep(1500);
        }
    }

    public static JsonObject toJsonObject(final LcdData lcdData) {

        final JsonObject json = new JsonObject();

        json.add("Track", new JsonPrimitive(lcdData.getLine0()));//.substring(0, Math.min(16, lcdData.getLine0().length()))));
        json.add("Artist", new JsonPrimitive(lcdData.getLine1()));//.substring(0, Math.min(16, lcdData.getLine1().length()))));
//        json.add("Album", new JsonPrimitive(lcdData.getLine2()));//.substring(0, Math.min(16, lcdData.getLine2().length()))));
//        json.add("Line3", new JsonPrimitive(lcdData.getLine3().substring(0, Math.min(16, lcdData.getLine3().length()))));

        return json;
    }
}
