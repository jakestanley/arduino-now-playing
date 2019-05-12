package api;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import serial.LcdData;

@Builder
@EqualsAndHashCode
class NowPlayingModel implements LcdData {

    private String track;
    private String artist;
    private String album;

    @Override
    public String getLine0() {
        return track;
    }

    @Override
    public String getLine1() {
        return artist;
    }

    @Override
    public String getLine2() {
        return album;
    }

    @Override
    public String getLine3() {
        return "Spotify";
    }
}
