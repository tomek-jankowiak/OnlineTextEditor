package put.sk.onlinetexteditor.logic;

import put.sk.onlinetexteditor.error.ConnectionException;
import put.sk.onlinetexteditor.error.InvalidAddressException;
import put.sk.onlinetexteditor.error.InvalidPortException;
import put.sk.onlinetexteditor.util.MessageCode;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ConnectionController {
  private Socket socket = null;
  private InetAddress address = null;
  private Integer port = null;
  private int clientStatus;

  public void connect(String address, String port) throws
          ConnectionException, InvalidAddressException, InvalidPortException {
    InetAddress inetAddress = parseInetAddress(address);
    int portNumber = parsePortNumber(port);
    try {
      socket = new Socket(inetAddress, portNumber);
      this.address = inetAddress;
      this.port = portNumber;
      clientStatus = MessageCode.CLIENT_CONNECTED;
    } catch (Exception ex) {
      throw new ConnectionException();
    }
  }

  public void disconnect() throws ConnectionException {
    if (socket != null) {
      try {
        clientStatus = MessageCode.CLIENT_DISCONNECTED;
        socket.close();
      } catch (IOException ex) {
        throw new ConnectionException();
      }
    }
  }

  public Socket getSocket() {
    return socket;
  }

  public int getClientStatus() {
    return clientStatus;
  }

  private InetAddress parseInetAddress(String address) throws InvalidAddressException {
    try {
      return InetAddress.getAllByName(address)[0];
    } catch (UnknownHostException ex) {
      throw new InvalidAddressException();
    }
  }

  private int parsePortNumber(String port) throws InvalidPortException {
    try {
      return Integer.parseInt(port);
    } catch (NumberFormatException ex) {
      throw new InvalidPortException();
    }
  }
}
