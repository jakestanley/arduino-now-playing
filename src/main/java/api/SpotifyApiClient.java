package api;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import configuration.Configuration;
import serial.LcdData;
import serial.LcdDataProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

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

    public boolean updateNowPlaying() {

        final String token = credentials.getAccessToken();

        api.setAccessToken(token);
        GetUsersCurrentlyPlayingTrackRequest currentlyPlayingTrackRequest = api.getUsersCurrentlyPlayingTrack().build();
        CurrentlyPlaying currentlyPlaying = null;

        try {
            currentlyPlaying = currentlyPlayingTrackRequest.execute();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SpotifyWebApiException e) {
            e.printStackTrace();
        }

        final NowPlayingModel nowPlayingRefresh = toNowPlayingModel(currentlyPlaying);

        if (null == nowPlaying || !nowPlaying.equals(nowPlayingRefresh)) {
            this.nowPlaying = nowPlayingRefresh;
            return true;
        } else {
            return false;
        }
    }

    private NowPlayingModel toNowPlayingModel(CurrentlyPlaying currentlyPlaying) {
        return NowPlayingModel.builder()
                .artist(Arrays.stream(currentlyPlaying.getItem().getArtists()).findFirst().get().getName())
                .track(currentlyPlaying.getItem().getName())
                .album(currentlyPlaying.getItem().getAlbum().getName())
                .build();
    }

    @Override
    public LcdData getData() {

        updateNowPlaying();
        return this.nowPlaying;
    }

}
