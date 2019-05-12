import api.SpotifyApiClient;
import com.google.gson.*;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import configuration.Configuration;
import file.ConfigurationLoader;
import lombok.extern.java.Log;
import org.apache.commons.cli.*;
import serial.SerialClient;

import java.util.Arrays;
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



        while (true) {

            final CurrentlyPlaying nowPlaying = spotifyApiClient.getNowPlaying();

            final String currentTrack = nowPlaying.getItem().getName();
            final String currentArtist = Arrays.stream(nowPlaying.getItem().getArtists()).map(ArtistSimplified::getName).findFirst().orElse("");
//            final String yearOfRelease = nowPlaying.getProgress_ms()

            JsonObject a = new JsonObject();
            a.add("Track", new JsonPrimitive(currentTrack));
            a.add("Artist", new JsonPrimitive(currentArtist));

            serialClient.send(a.toString());
            Thread.sleep(1500);
        }
    }
}
