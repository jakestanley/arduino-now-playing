package api;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import configuration.Configuration;
import lombok.extern.java.Log;
import serial.LcdData;
import serial.LcdDataProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Log
public class SpotifyApiClient implements LcdDataProvider {

    private final SpotifyApi api;

    private String clientId;
    private String clientSecret;
    private String refreshToken;

    private AuthorizationCodeRefreshRequest request;
    private AuthorizationCodeCredentials credentials;

    private NowPlayingModel nowPlaying;

    public SpotifyApiClient(final Configuration config) {

        clientId = config.getSecrets().getSpotifyClientId();
        clientSecret = config.getSecrets().getSpotifyClientSecret();
        refreshToken = config.getSecrets().getSpotifyRefreshToken();

        this.api = SpotifyApi.builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        this.request = api.authorizationCodeRefresh().build();
        this.credentials = getCredentials();

        this.nowPlaying = null;
    }

    private AuthorizationCodeCredentials getCredentials() {

        // TODO get a new access token if this has expired

        try {
            this.credentials = Optional
                    .ofNullable(credentials)
                    .orElse(request.execute());
            return this.credentials;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SpotifyWebApiException e) {
            e.printStackTrace();
        }

        return null;
    }

    boolean updateNowPlaying() throws LcdDataProviderException {

        log.info(String
                .format("Credentials expire in %d", credentials
                .getExpiresIn()));

        if (credentials.getExpiresIn() < 10) {
            log.info("refreshing token...");
            credentials = getCredentials();
        }

        final String token = credentials.getAccessToken();

        api.setAccessToken(token);
        GetUsersCurrentlyPlayingTrackRequest currentlyPlayingTrackRequest = api
                .getUsersCurrentlyPlayingTrack().build();

        try {
            final CurrentlyPlaying currentlyPlaying = currentlyPlayingTrackRequest.execute();
            final NowPlayingModel nowPlayingRefresh = toNowPlayingModel(currentlyPlaying);

            if (null == nowPlaying || !nowPlaying.equals(nowPlayingRefresh)) {
                this.nowPlaying = nowPlayingRefresh;
                return true;
            } else {
                return false;
            }
        } catch (SpotifyWebApiException | IOException s) {
            s.printStackTrace();
            throw new LcdDataProviderException();
        }
    }

    private NowPlayingModel toNowPlayingModel(CurrentlyPlaying currentlyPlaying) {
        return NowPlayingModel.builder()
                .artist(Arrays.stream(currentlyPlaying.getItem().getArtists()).findFirst().get().getName())
                .track(currentlyPlaying.getItem().getName())
                .album(""/*currentlyPlaying.getItem().getAlbum().getName()*/)
                .state(currentlyPlaying.getIs_playing() ? "" : "||")
                .build();
    }

    @Override
    public LcdData getData() throws LcdDataProviderException {

        updateNowPlaying();

        if (Objects.nonNull(this.nowPlaying)) {
            return this.nowPlaying;
        } else {
            return NowPlayingModel.unavailable();
        }
    }
}
