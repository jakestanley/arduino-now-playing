package serial;

import exception.LcdDataProviderException;

public interface LcdDataProvider {

    LcdData getData() throws LcdDataProviderException;
}
