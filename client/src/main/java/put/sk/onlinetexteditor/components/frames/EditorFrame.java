package put.sk.onlinetexteditor.components.frames;

import put.sk.onlinetexteditor.logic.CommunicationController;
import put.sk.onlinetexteditor.logic.ConnectionController;
import put.sk.onlinetexteditor.util.MessageCode;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class EditorFrame extends JFrame {
  private final ConnectionController connectionController;
  private final CommunicationController communicationController;
  private final Consumer<String> onNewFileListener;
  private final Runnable onOpenFileListener;
  private final Consumer<String> onSaveFileListener;
  private final Consumer<String> onChooseFileListener;
  private final Runnable onDisconnectListener;
  private JPanel mainPanel;
  private JTextArea textArea;
  private JComboBox<String> filesComboBox;
  private JButton newButton;
  private JButton openButton;
  private JButton saveButton;
  private JButton logOutButton;
  private JButton chooseButton;
  private String editedFilename;

  public EditorFrame(
          ConnectionController connectionController,
          CommunicationController communicationController,
          Consumer<String> onNewFileListener,
          Runnable onOpenFileListener,
          Consumer<String> onSaveFileListener,
          Consumer<String> onChooseFileListener,
          Runnable onDisconnectListener) {
    super("Online Text Editor");

    this.connectionController = connectionController;
    this.communicationController = communicationController;
    this.onNewFileListener = onNewFileListener;
    this.onOpenFileListener = onOpenFileListener;
    this.onSaveFileListener = onSaveFileListener;
    this.onChooseFileListener = onChooseFileListener;
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

    newButton.addActionListener(e -> onNewFile());
    openButton.addActionListener(e -> onOpenFile());
    saveButton.addActionListener(e -> onSaveFile());
    chooseButton.addActionListener(e -> onChooseFile());
    logOutButton.addActionListener(e -> onClose());
  }

  public void setTextArea(String buffer) {
    textArea.setText(buffer);
  }

  public void setEditedFilename(String filename) {
    editedFilename = filename;
  }

  public void setFilesComboBox(List<String> fileList) {
    filesComboBox.removeAllItems();
    for (String file : fileList) {
      filesComboBox.addItem(file);
    }
    filesComboBox.setSelectedItem(editedFilename);
  }

  public void runReadLoop() {
      while (connectionController.getClientStatus() != MessageCode.CLIENT_DISCONNECTED) {
        int messageCode = communicationController.receiveMessageCode(connectionController.getSocket());
        if (messageCode == MessageCode.SERVER_UPDATE_CLIENT_FILE) {
          String receivedBuffer = communicationController.receiveBuffer(connectionController.getSocket());
          if (receivedBuffer != null) {
            textArea.setText(receivedBuffer);
          }
        } else if (messageCode == MessageCode.SERVER_UPDATE_FILE_LIST) {
          List<String> fileList = communicationController.receiveFileList(connectionController.getSocket());
          if (fileList != null) {
            setFilesComboBox(fileList);
          }
        }
      }
  }

  private void onKeyReleased() {
    communicationController.sendBuffer(connectionController.getSocket(),
            MessageCode.CLIENT_UPDATE_FILE,
            new String[] { textArea.getText() } );
  }

  private void onNewFile() {
    String fileName = JOptionPane.showInputDialog(null, "Enter a file name");
    if ((fileName != null) && (fileName.length() > 0)) {
      editedFilename = fileName;
      SwingUtilities.invokeLater(() -> {
        onNewFileListener.accept(fileName);
        textArea.setEnabled(true);
        saveButton.setEnabled(true);
      });
    }
  }

  private void onOpenFile() {
    SwingUtilities.invokeLater(() -> {
      onOpenFileListener.run();
      textArea.setEnabled(true);
      saveButton.setEnabled(true);
    });
  }

  private void onSaveFile() {
    SwingUtilities.invokeLater(() -> {
      onSaveFileListener.accept(textArea.getText());
    });
  }

  private void onChooseFile() {
    if (filesComboBox.getItemCount() > 0) {
      SwingUtilities.invokeLater(() -> {
        onChooseFileListener.accept(Objects.requireNonNull(filesComboBox.getSelectedItem()).toString());
        textArea.setEnabled(true);
        saveButton.setEnabled(true);
      });
    }
  }

  private void onClose() {
    textArea.setEnabled(false);
    saveButton.setEnabled(false);
    onDisconnectListener.run();
  }
}
