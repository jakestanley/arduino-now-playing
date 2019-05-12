package configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Secrets {

    private String spotifyClientId;
    private String spotifyClientSecret;
    private String spotifyRefreshToken;
}
