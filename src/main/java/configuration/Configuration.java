package configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public final class Configuration {

    private SpotifyConfiguration spotify;
    private Secrets secrets;
}
