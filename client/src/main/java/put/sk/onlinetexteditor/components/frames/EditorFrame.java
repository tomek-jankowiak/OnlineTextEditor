package put.sk.onlinetexteditor.components.frames;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import put.sk.onlinetexteditor.error.ConnectionException;
import put.sk.onlinetexteditor.error.InvalidFileException;
import put.sk.onlinetexteditor.logic.CommunicationController;
import put.sk.onlinetexteditor.logic.ConnectionController;
import put.sk.onlinetexteditor.util.MessageCode;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
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
  private final Runnable onLostConnectionListener;
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
          Runnable onLostConnectionListener,
          Runnable onDisconnectListener) {
    super("Online Text Editor");

    this.connectionController = connectionController;
    this.communicationController = communicationController;
    this.onNewFileListener = onNewFileListener;
    this.onOpenFileListener = onOpenFileListener;
    this.onSaveFileListener = onSaveFileListener;
    this.onChooseFileListener = onChooseFileListener;
    this.onLostConnectionListener = onLostConnectionListener;
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
      try {
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
      } catch (ConnectionException ex) {
        return;
      }
    }
  }

  private void onKeyReleased() {
    communicationController.sendBuffer(connectionController.getSocket(),
            MessageCode.CLIENT_UPDATE_FILE,
            new String[]{textArea.getText()});
  }

  private void onNewFile() {
    String fileName = JOptionPane.showInputDialog(null, "Enter a file name");
    if ((fileName != null) && (fileName.length() > 0)) {
      editedFilename = fileName;
      SwingUtilities.invokeLater(() -> {
        try {
          onNewFileListener.accept(fileName);
          textArea.setEnabled(true);
          saveButton.setEnabled(true);
        } catch (ConnectionException ex) {
          showError("Connection lost.");
          onLostConnectionListener.run();
        }
      });
    }
  }

  private void onOpenFile() {
    SwingUtilities.invokeLater(() -> {
      try {
        onOpenFileListener.run();
        textArea.setEnabled(true);
        saveButton.setEnabled(true);
      } catch (InvalidFileException ex) {
        showError("Invalid file.");
      } catch (ConnectionException ex) {
        showError("Connection lost.");
        onLostConnectionListener.run();
      }
    });
  }

  private void onSaveFile() {
    SwingUtilities.invokeLater(() -> {
      try {
        onSaveFileListener.accept(textArea.getText());
      } catch (InvalidFileException ex) {
        showError("Couldn't save file.");
      }
    });
  }

  private void onChooseFile() {
    if (filesComboBox.getItemCount() > 0) {
      SwingUtilities.invokeLater(() -> {
        try {
          onChooseFileListener.accept(Objects.requireNonNull(filesComboBox.getSelectedItem()).toString());
          textArea.setEnabled(true);
          saveButton.setEnabled(true);
        } catch (ConnectionException ex) {
          showError("Connection lost.");
          onLostConnectionListener.run();
        }
      });
    }
  }

  private void onClose() {
    textArea.setEnabled(false);
    saveButton.setEnabled(false);
    onDisconnectListener.run();
  }

  private void showError(String message) {
    JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
    $$$setupUI$$$();
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer
   * >>> IMPORTANT!! <<<
   * DO NOT edit this method OR call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$() {
    mainPanel = new JPanel();
    mainPanel.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
    mainPanel.setPreferredSize(new Dimension(500, 350));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(1, 2, new Insets(10, 10, 10, 10), -1, -1));
    mainPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Currently edited files", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, null, null));
    filesComboBox = new JComboBox();
    filesComboBox.setEnabled(true);
    panel1.add(filesComboBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(291, 30), null, 0, false));
    chooseButton = new JButton();
    chooseButton.setText("Choose");
    panel1.add(chooseButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
    mainPanel.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    final JScrollPane scrollPane1 = new JScrollPane();
    panel2.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    textArea = new JTextArea();
    textArea.setEnabled(false);
    scrollPane1.setViewportView(textArea);
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new GridLayoutManager(3, 1, new Insets(0, 5, 0, 5), -1, -1));
    panel2.add(panel3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "File", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, null, null));
    newButton = new JButton();
    newButton.setText("New");
    panel3.add(newButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    openButton = new JButton();
    openButton.setText("Open");
    panel3.add(openButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    saveButton = new JButton();
    saveButton.setEnabled(false);
    saveButton.setText("Save");
    panel3.add(saveButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel4 = new JPanel();
    panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
    mainPanel.add(panel4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    logOutButton = new JButton();
    logOutButton.setText("Log out");
    panel4.add(logOutButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer1 = new Spacer();
    panel4.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
  }

  /**
   * @noinspection ALL
   */
  public JComponent $$$getRootComponent$$$() {
    return mainPanel;
  }

}
