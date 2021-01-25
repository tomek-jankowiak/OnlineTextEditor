package put.sk.onlinetexteditor.logic;

import put.sk.onlinetexteditor.util.MessageCode;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CommunicationController {

  public void sendBuffer(Socket socket, byte messageCode, String[] buffer) {
    byte[] encodedBuffer = encodeBuffer(messageCode, buffer);
    System.out.println(messageCode);
    if (encodedBuffer == null) {
      return;
    }

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

  public List<String> receiveFileList(Socket socket) {
    List<String> fileList = new ArrayList<>();
    try {
      InputStream in = socket.getInputStream();
      int filesLength = readLength(in);
      if (filesLength > 0) {
        for (int i = 0; i < filesLength; i++) {
          String file = receiveBuffer(socket);
          if (file != null) {
            fileList.add(file);
          }
        }
        return fileList;
      }
      return null;
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public void sendMessageCode(Socket socket, byte status) {
    try {
      OutputStream out = socket.getOutputStream();
      out.write(status);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public int receiveMessageCode(Socket socket) {
    try {
      InputStream inputStream = socket.getInputStream();
      return inputStream.read();
    } catch (IOException ex) {
      ex.printStackTrace();
      return -1;
    }
  }

  private byte[] encodeBuffer(byte messageCode, String[] buffer) {
    int bufferSize = 0;
    for (String s : buffer) {
      bufferSize += s.length();
    }
    ByteBuffer byteBuffer;
    switch (messageCode) {
      case MessageCode.CLIENT_CREATE_NEW_FILE:
      case MessageCode.CLIENT_OPEN_FILE:
      case MessageCode.CLIENT_UPDATE_FILE:
        byteBuffer = ByteBuffer.allocate(1 + 4 + bufferSize);
        break;
      case MessageCode.CLIENT_UPLOAD_NEW_FILE:
        byteBuffer = ByteBuffer.allocate(1 + 8 + bufferSize);
        break;
      default:
        return null;
    }
    byteBuffer.put(messageCode);
    for (String s : buffer) {
      byteBuffer.putInt(s.length());
      byteBuffer.put(s.getBytes());
    }
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
