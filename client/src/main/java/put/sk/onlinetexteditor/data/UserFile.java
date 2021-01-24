package put.sk.onlinetexteditor.data;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class UserFile {
  private String fileName;
  private String buffer;
  private File file;

  public String getFileName() {
    return fileName;
  }

  public String getBuffer() {
    return buffer;
  }

  public void newFile() {
    JFileChooser fileChooser = new JFileChooser("f:");
    int r = fileChooser.showOpenDialog(null);

    if (r == JFileChooser.APPROVE_OPTION) {
      file = new File(fileChooser.getSelectedFile().getAbsolutePath());
      fileName = file.getName();
      try {
        if (!file.exists()) {
          file.createNewFile();
        }
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          stringBuilder.append(line);
          stringBuilder.append("\n");
        }
        buffer = stringBuilder.toString();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  public void setFile(String fileName, String buffer) {
      this.fileName = fileName;
      this.buffer = buffer;
  }
}
