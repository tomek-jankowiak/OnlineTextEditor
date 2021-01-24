package put.sk.onlinetexteditor.components.frames;

import put.sk.onlinetexteditor.data.UserFile;
import put.sk.onlinetexteditor.logic.CommunicationController;
import put.sk.onlinetexteditor.logic.ConnectionController;
import put.sk.onlinetexteditor.util.ClientStatus;

import javax.swing.*;
import java.awt.event.*;

public class EditorFrame extends JFrame {
  private final ConnectionController connectionController;
  private final CommunicationController communicationController;
  private final UserFile userFile;
  private final Runnable onDisconnectListener;
  private JPanel mainPanel;
  private JTextArea textArea;

  public EditorFrame(
          ConnectionController connectionController,
          CommunicationController communicationController,
          UserFile userFile,
          Runnable onDisconnectListener) {
    super("Online Text Editor");

    this.connectionController = connectionController;
    this.communicationController = communicationController;
    this.userFile = userFile;
    this.onDisconnectListener = onDisconnectListener;

    this.setContentPane(mainPanel);
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.pack();
    this.setLocationRelativeTo(null);

    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onClose();
      }
    });

    textArea.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        onKeyReleased();
      }
    });
  }

  public void setTextArea(String buffer) {
    textArea.setText(buffer);
  }

  public void runReadLoop() {
      while (connectionController.getClientStatus() != ClientStatus.CLIENT_CLOSE_CONNECTION) {
        String receivedBuffer = communicationController.receiveBuffer(connectionController.getSocket());
        if (receivedBuffer != null) {
          textArea.setText(receivedBuffer);
        }
      }
  }

  private void onKeyReleased() {
    communicationController.sendClientStatus(connectionController.getSocket(), ClientStatus.CLIENT_UPDATE_FILE);
    communicationController.sendBuffer(connectionController.getSocket(), textArea.getText());
  }

  private void onClose() {
    onDisconnectListener.run();
  }
}
