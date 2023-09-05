package cn.beichenhpy.nio.file;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


/**
 * Java nio FileChannel 无法支持非阻塞 所以无法绑定selector
 */
public class NioFileDemo {

    private static final String FILE_PATH = "./nio/note.txt";
    private static final String FILE_OUTPUT_PATH = "./nio/note.copy.txt";

    public static void main(String[] args) {
        try (FileInputStream fileInputStream = new FileInputStream(FILE_PATH)) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(FILE_OUTPUT_PATH)) {
                FileChannel inputChannel = fileInputStream.getChannel();
                FileChannel outputChannel = fileOutputStream.getChannel();
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                while (inputChannel.read(byteBuffer) > -1) {
                    byteBuffer.flip();
                    outputChannel.write(byteBuffer);
                    byteBuffer.clear();
                }
            } catch (IOException one) {
                one.printStackTrace();
            }
        } catch (IOException ine) {
            ine.printStackTrace();
        }
    }
}
