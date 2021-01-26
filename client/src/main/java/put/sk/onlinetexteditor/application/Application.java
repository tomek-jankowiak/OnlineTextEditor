package put.sk.onlinetexteditor.application;

import put.sk.onlinetexteditor.components.frames.ConnectionFrame;
import put.sk.onlinetexteditor.components.frames.EditorFrame;
import put.sk.onlinetexteditor.data.UserFile;
import put.sk.onlinetexteditor.logic.CommunicationController;
import put.sk.onlinetexteditor.logic.ConnectionController;
import put.sk.onlinetexteditor.util.MessageCode;

import javax.swing.*;
import java.util.List;

public class Application {
  private final ConnectionController connectionController;
  private final CommunicationController communicationController;
  private final ConnectionFrame connectionFrame;
  private final EditorFrame editorFrame;
  private UserFile userFile;

  public Application() {
    this.connectionController = new ConnectionController();
    this.communicationController = new CommunicationController();

    this.connectionFrame = new ConnectionFrame(
            connectionController,
            this::connectedCallback,
            this::closeApplication);

    this.editorFrame = new EditorFrame(connectionController,
            communicationController,
            this::newFileCallback,
            this::openFileCallback,
            this::saveFileCallback,
            this::chooseFileCallback,
            this::disconnectedCallback);
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
    new Thread(editorFrame::runReadLoop).start();
  }

  private void disconnectedCallback() {
    communicationController.sendMessageCode(connectionController.getSocket(),
            MessageCode.CLIENT_DISCONNECTED);
    connectionController.disconnect();
    editorFrame.setVisible(false);
    editorFrame.setTextArea("");
    connectionFrame.setVisible(true);
  }

  private void newFileCallback(String fileName) {
    editorFrame.setTextArea("");
    communicationController.sendBuffer(connectionController.getSocket(),
            MessageCode.CLIENT_CREATE_NEW_FILE,
            new String[] { fileName });
    userFile = new UserFile(fileName, "");
  }

  private void openFileCallback() {
    userFile = UserFile.openFile();
    if (userFile != null) {
      editorFrame.setEditedFilename(userFile.getFileName());
      editorFrame.setTextArea(userFile.getFileBuffer());
      communicationController.sendBuffer(connectionController.getSocket(),
              MessageCode.CLIENT_UPLOAD_NEW_FILE,
              new String[]{userFile.getFileName(), userFile.getFileBuffer()});
    }
  }

  private void chooseFileCallback(String fileName) {
    editorFrame.setEditedFilename(fileName);
    communicationController.sendBuffer(connectionController.getSocket(),
            MessageCode.CLIENT_OPEN_FILE,
            new String[] { fileName });
  }

  private void saveFileCallback(String buffer) {
    userFile.saveFile(buffer);
  }
}
