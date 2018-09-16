package loref.android.apps.androidtcpcomm;

/**
 * Created by lore_f on 29/07/2018.
 */

public interface TCPCommListener {

    void onConnected(int port);
    void onInterfaceReady();
    void onConnectionError(Exception e);
    void onDataWriteError (Exception e);
    void onDataReadError (Exception e);
    void onDataLineReceived (byte[] data);
    void onClose(boolean byLocal);

}
