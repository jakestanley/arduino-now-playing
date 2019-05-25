package serial;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Enumeration;

@Log
public class SerialClient {

    private SerialPort serialPort;
    private String lastSent;
    private Boolean chillMode;

    public SerialClient(final Boolean chillMode) {

        CommPortIdentifier port = null;
        this.lastSent = null;
        this.chillMode = chillMode;

        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        while(ports.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) ports.nextElement();
            if (currPortId.getName().equals("COM3")) {
                log.info(String.format("Will use port '%s'", currPortId.getName()));
                port = currPortId;
                break;
            }
        }

        try {
            serialPort = (SerialPort) port.open("ArduinoNowPlaying", 4000);
        } catch (PortInUseException e) {
            log.info(String
                    .format("Port '%s' currently in use", port.getName()));
        }

        try {
            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            // seems like i need to give arduino some time to sort the serial connection out
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        }
    }

    public void send(final String payload) {

        final String bakedPayload = chillMode ?
                StringUtils.stripAccents(payload).toLowerCase() : StringUtils.stripAccents(payload);

//        if (null != lastSent) {
//            if (lastSent.equals(bakedPayload)) {
//                return;
//            }
//        }

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
}
