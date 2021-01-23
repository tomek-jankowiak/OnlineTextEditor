package put.sk.onlinetexteditor.components.frames;

import put.sk.onlinetexteditor.logic.ConnectionController;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ConnectionFrame extends JFrame {
  private final ConnectionController connectionController;
  private final Runnable onConnectListener;
  private final Runnable onCloseListener;
  private JPanel mainPanel;
  private JTextField textFieldAddress;
  private JTextField textFieldPort;
  private JButton buttonConnect;

  public ConnectionFrame(
          ConnectionController connectionController, Runnable onConnectListener, Runnable onCloseListener) {
    super("Online Text Editor");

    this.connectionController = connectionController;
    this.onConnectListener = onConnectListener;
    this.onCloseListener = onCloseListener;

    this.setContentPane(mainPanel);
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.setResizable(false);
    this.pack();
    this.setLocationRelativeTo(null);

    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onClose();
      }
    });

    buttonConnect.addActionListener(e -> onConnect());

  }

  private void onConnect() {
    String address = textFieldAddress.getText();
    String port = textFieldPort.getText();
    SwingUtilities.invokeLater(() -> {
      connectionController.connect(address, port);
      onConnectListener.run();
    });
  }

  private void onClose() {
    onCloseListener.run();
  }
}
