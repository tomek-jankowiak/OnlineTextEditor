package put.sk.onlinetexteditor.logic;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

public class CommunicationController {
  public void sendBuffer(Socket socket, String buffer) {
    byte[] encodedBuffer = encodeBuffer(buffer);
    try {
      OutputStream out = socket.getOutputStream();
      out.write(encodedBuffer);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public String receiveBuffer(Socket socket) {
    try {
      InputStream in = socket.getInputStream();

      int bufferLength = readLength(in);
      byte[] byteBuffer = new byte[bufferLength];
      int count = 0;
      while (count != bufferLength) {
        count += in.read(byteBuffer, count, bufferLength - count);
      }
      return new String(byteBuffer);
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public void sendClientStatus(Socket socket, int status) {
    byte[] clientStatus = encodeStatus(status);
    try {
      OutputStream out = socket.getOutputStream();
      out.write(clientStatus);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public int receiveClientStatus(Socket socket) {
    try {
      InputStream inputStream = socket.getInputStream();
      return inputStream.read();
    } catch (IOException ex) {
      ex.printStackTrace();
      return -1;
    }
  }

  private byte[] encodeStatus(int status) {
    ByteBuffer buffer = ByteBuffer.allocate(1);
    buffer.putInt(status);
    return buffer.array();
  }

  private byte[] encodeBuffer(String buffer) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(4 + buffer.length());
    byteBuffer.putInt(buffer.length());
    byteBuffer.put(buffer.getBytes());
    return byteBuffer.array();
  }

  private int readLength(InputStream in) throws IOException {
    byte[] length = new byte[4];
    int readCount = 0;
    while (readCount != 4) {
      readCount += in.read(length,readCount, 4 - readCount);
    }
    ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    byteBuffer.put(length);
    return byteBuffer.getInt(0);
  }

}
