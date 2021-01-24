package put.sk.onlinetexteditor.application;

import put.sk.onlinetexteditor.components.frames.ConnectionFrame;
import put.sk.onlinetexteditor.components.frames.EditorFrame;
import put.sk.onlinetexteditor.data.UserFile;
import put.sk.onlinetexteditor.logic.CommunicationController;
import put.sk.onlinetexteditor.logic.ConnectionController;
import put.sk.onlinetexteditor.util.ClientStatus;

import javax.swing.*;

public class Application {
  private final ConnectionController connectionController;
  private final CommunicationController communicationController;
  private final UserFile userFile;
  private final ConnectionFrame connectionFrame;
  private final EditorFrame editorFrame;

  public Application() {
    this.connectionController = new ConnectionController();
    this.communicationController = new CommunicationController();
    this.userFile = new UserFile();

    this.connectionFrame = new ConnectionFrame(
            connectionController,this::connectedCallback, this::closeApplication);
    this.editorFrame = new EditorFrame(
            connectionController, communicationController, userFile, this::disconnectedCallback);
  }

  public void run() {
    this.connectionFrame.setVisible(true);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      Application application = new Application();
      application.run();
    });
  }

  private void closeApplication() {
    connectionFrame.dispose();
    editorFrame.dispose();
  }

  private void connectedCallback() {
    connectionFrame.setVisible(false);
    editorFrame.setVisible(true);
    SwingUtilities.invokeLater(() -> {
      int status = communicationController.receiveClientStatus(connectionController.getSocket());
      System.out.println(status);
      if (status == ClientStatus.CLIENT_OPEN_FILE) {
        System.out.println("OPEN FILE");
        String input = communicationController.receiveBuffer(connectionController.getSocket());
        editorFrame.setTextArea(input);
      }
      //editorFrame.runReadLoop();
    });
  }

  private void disconnectedCallback() {
    communicationController.sendClientStatus(connectionController.getSocket(), ClientStatus.CLIENT_CLOSE_CONNECTION);
    connectionController.disconnect();
    editorFrame.setVisible(false);
    connectionFrame.setVisible(true);
  }
}
