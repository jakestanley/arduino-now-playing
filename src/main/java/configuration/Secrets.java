package configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Secrets {

    private String spotifyClientId;
    private String spotifyClientSecret;
    private String spotifyRefreshToken;
}
