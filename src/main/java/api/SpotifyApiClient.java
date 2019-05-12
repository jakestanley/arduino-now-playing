package api;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import configuration.Configuration;

import java.io.IOException;
import java.util.Optional;

public class SpotifyApiClient {

    private final SpotifyApi api;

    private String clientId;
    private String clientSecret;
    private String refreshToken;

    private AuthorizationCodeRefreshRequest request;
    private AuthorizationCodeCredentials credentials;

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

    public CurrentlyPlaying getNowPlaying() {

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

        return currentlyPlaying;
    }
}
