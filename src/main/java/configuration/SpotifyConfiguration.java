package configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public final class SpotifyConfiguration {

    private String user;
    private String scope;
}
