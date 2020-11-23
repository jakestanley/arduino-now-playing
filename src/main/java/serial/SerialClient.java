package serial;

import exception.NoInterfaceFoundException;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import lombok.Builder;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Log
public class SerialClient {

    private SerialPort serialPort;
    private String lastSent;
    private Boolean chillMode;

    @Builder
    public SerialClient(String iface, Boolean chillMode) throws Exception {

        final CommPortIdentifier port;
        this.lastSent = null;
        this.chillMode = chillMode;

        if (Objects.nonNull(iface)) {
            port = CommPortIdentifier.getPortIdentifier(iface);
        } else {
            port = getFreeCommPortIdentifier();
        }

        try {
            serialPort = (SerialPort) port.open("ArduinoNowPlaying", 4000);
        } catch (PortInUseException e) {
            log.info(String
                    .format("Port '%s' currently in use", port.getName()));
        }

        serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        // seems like i need to give arduino some time to sort the serial connection out
        Thread.sleep(2000);
    }

    // TODO lol
//    public void send(final LcdData data) {
//
//        data.getLines()
//    }

    public void send(final String payload) {

        final String bakedPayload = chillMode ?
                StringUtils.stripAccents(payload).toLowerCase() : StringUtils.stripAccents(payload);

        if (null != lastSent) {
            if (lastSent.equals(bakedPayload)) {
                return;
            }
        }

        try {
            final byte[] bytes = bakedPayload.getBytes();

            serialPort.getOutputStream().write(bytes);

//              TODO serial buffer is capped at 64 bytes. need to be more clever.
//            int response = serialPort.getInputStream().read();
            log.info(String.format("wrote: '%s'", bakedPayload));
            log.info(String.format("sent %d bytes", bytes.length));
            lastSent = bakedPayload;

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(3);
        }
    }

    public void close() {
        serialPort.close();
    }

    /**
     * @implNote Largely assumes there's only one port you want to use
     * @return
     */
    private static CommPortIdentifier getFreeCommPortIdentifier() throws Exception {

        // making a stream from the ports
        final Set<CommPortIdentifier> identifiers = new HashSet<>();
        CommPortIdentifier
                .getPortIdentifiers()
                .asIterator()
                .forEachRemaining(e -> identifiers.add((CommPortIdentifier) e));

        // now stream the set
        return identifiers.stream()
                .filter(identifier -> !identifier.isCurrentlyOwned())
                .findFirst()
                .orElseThrow(NoInterfaceFoundException::new);
    }
}
