package serial;

import api.LcdDataProviderException;

public interface LcdDataProvider {

    LcdData getData() throws LcdDataProviderException;
}
