package put.sk.onlinetexteditor.logic;

import put.sk.onlinetexteditor.util.ClientStatus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ConnectionController {
  private Socket socket = null;
  private InetAddress address = null;
  private Integer port = null;
  private int clientStatus;

  public void connect(String address, String port) {
    InetAddress inetAddress = parseInetAddress(address);
    int portNumber = parsePortNumber(port);
    try {
      socket = new Socket(inetAddress, portNumber);
      this.address = inetAddress;
      this.port = portNumber;
      clientStatus = ClientStatus.CLIENT_NEW_CONNECTION;
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public void disconnect() {
    if (socket != null) {
      try {
        clientStatus = ClientStatus.CLIENT_CLOSE_CONNECTION;
        socket.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  public Socket getSocket() {
    return socket;
  }

  public int getClientStatus() {
    return clientStatus;
  }

  private InetAddress parseInetAddress(String address) {
    try {
      return InetAddress.getAllByName(address)[0];
    } catch (UnknownHostException ex) {
      return null;
    }
  }

  private int parsePortNumber(String port) {
    try {
      return Integer.parseInt(port);
    } catch (NumberFormatException ex) {
      return -1;
    }
  }
}
