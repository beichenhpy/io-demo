package cn.beichenhpy.bio.file;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BioFileDemo {

    private static final String FILE_PATH = "./bio/note.txt";
    private static final String FILE_OUTPUT_PATH = "./bio/note.copy.txt";

    public static void main(String[] args) {
        try (FileInputStream fileInputStream = new FileInputStream(FILE_PATH)) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(FILE_OUTPUT_PATH)) {
                byte[] bytes = new byte[1024];
                int len = -1;
                while ((len = fileInputStream.read(bytes)) > -1) {
                    fileOutputStream.write(bytes, 0, len);
                }
            } catch (IOException ee) {
                throw new IOException(ee);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
