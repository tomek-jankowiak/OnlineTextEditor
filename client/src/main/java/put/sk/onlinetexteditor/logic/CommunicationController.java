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
      if (bufferLength == 0) {
        return null;
      }
      byte[] byteBuffer = new byte[bufferLength];
      int writeCount = 0;
      int count;
      while (writeCount != bufferLength) {
        count = in.read(byteBuffer, writeCount, bufferLength - writeCount);
        if (count == -1) {
          return null;
        } else {
          writeCount += count;
        }
      }
      return new String(byteBuffer);
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public void sendClientStatus(Socket socket, byte status) {
    System.out.println(status);
    try {
      OutputStream out = socket.getOutputStream();
      out.write(status);
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

  private byte[] encodeBuffer(String buffer) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(4 + buffer.length());
    byteBuffer.putInt(buffer.length());
    byteBuffer.put(buffer.getBytes());
    return byteBuffer.array();
  }

  private int readLength(InputStream in) throws IOException {
    byte[] length = new byte[4];
    int readCount = 0;
    int count;
    while (readCount != 4) {
      count = in.read(length,readCount, 4 - readCount);
      if (count == -1) {
        return 0;
      }
      readCount += count;
    }
    ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    byteBuffer.put(length);
    return byteBuffer.getInt(0);
  }

}
