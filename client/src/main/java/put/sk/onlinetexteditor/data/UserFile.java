package put.sk.onlinetexteditor.data;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;

public class UserFile {
  private final String fileName;
  private String fileBuffer;

  public UserFile(String fileName, String fileBuffer) {
    this.fileName = fileName;
    this.fileBuffer = fileBuffer;
  }

  public String getFileName() {
    return fileName;
  }

  public String getFileBuffer() {
    return fileBuffer;
  }

  public static UserFile openFile() {
    JFileChooser fileChooser = new JFileChooser("f:");
    FileFilter filter = new FileNameExtensionFilter("TXT File", "txt");
    fileChooser.setFileFilter(filter);
    int returnValue = fileChooser.showOpenDialog(null);

    if (returnValue == JFileChooser.APPROVE_OPTION) {
      File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
      try {
        if (!file.exists()) {
          return null;
        }
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          stringBuilder.append(line);
          stringBuilder.append("\n");
        }
        bufferedReader.close();

        String buffer = stringBuilder.toString();
        return new UserFile(file.getName(), buffer);
      } catch (IOException ex) {
        ex.printStackTrace();
        return null;
      }
    } else {
      return null;
    }
  }

  public void saveFile(String buffer) {
    fileBuffer = buffer;
    String[] lines = buffer.split("\n");

    JFileChooser fileChooser = new JFileChooser("f:");
    FileFilter filter = new FileNameExtensionFilter("text file (.txt)", "txt");
    fileChooser.setFileFilter(filter);
    int returnValue = fileChooser.showOpenDialog(null);

    if (returnValue == JFileChooser.APPROVE_OPTION) {
      File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
      System.out.println(file.getAbsolutePath());
      try {
        if (!file.exists()) {
          if (!file.createNewFile()) {
            return;
          }
        }
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        for (String line : lines) {
          bufferedWriter.write(line);
          bufferedWriter.newLine();
        }

        bufferedWriter.flush();
        bufferedWriter.close();
        JOptionPane.showMessageDialog(null, "File saved.");

      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

}
