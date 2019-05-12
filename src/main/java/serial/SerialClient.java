package serial;

import com.google.gson.JsonObject;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import lombok.extern.java.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

@Log
public class SerialClient {

    private SerialPort serialPort;
    private String lastSent;

    public SerialClient() {

        CommPortIdentifier port = null;
        lastSent = null;

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

        if (null != lastSent) {
            if (lastSent.equals(payload)) {
                return;
            }
        }

        try {
            OutputStream outputStream = serialPort.getOutputStream();

            outputStream.write(payload.getBytes());

//            int response = serialPort.getInputStream().read();
            log.info(String.format("wrote: '%s'", payload));
            log.info(String.format("sent %d bytes", payload.getBytes().length));
            lastSent = payload;

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(3);
        }
    }

    public void close() {
        serialPort.close();
    }
}
