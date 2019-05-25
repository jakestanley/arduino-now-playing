package api;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import serial.LcdData;

@Builder
@EqualsAndHashCode
class NowPlayingModel implements LcdData {

    private String track;
    private String artist;
    private String album;
    private String state;

    @Override
    public String[] getLines() {
        final String[] lines =  { track, artist, album, state };
        return lines;
    }

    @NonNull
    public static NowPlayingModel unavailable() {
        return NowPlayingModel.builder().build();
    }
}
