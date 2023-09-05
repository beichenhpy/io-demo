package cn.beichenhpy.bio.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class BioSocketDemo {

    static class Server {
        public static void main(String[] args) {
            System.out.println("----------------bio-server-startup------------------");
            while (true) {
                try (ServerSocket serverSocket = new ServerSocket()) {
                    serverSocket.bind(new InetSocketAddress("127.0.0.1", 8088));
                    //监听
                    Socket socket = serverSocket.accept();
                    //获取输入流
                    try (InputStream inputStream = socket.getInputStream()) {
                        //读取
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = inputStream.read(buffer)) > -1) {
                            System.out.println("收到客户端的消息: " + new String(buffer, 0, len));
                        }
                        try (OutputStream outputStream = socket.getOutputStream()) {
                            PrintWriter printWriter = new PrintWriter(outputStream);
                            printWriter.print("你好客户端，这里是服务端");
                            printWriter.flush();
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Client {

        public static void main(String[] args) {
            System.out.println("----------------bio-client-startup------------------");
            try (Socket client = new Socket()) {
                client.connect(new InetSocketAddress("127.0.0.1", 8088));
                //获取输出流
                try (OutputStream outputStream = client.getOutputStream()) {
                    PrintWriter printWriter = new PrintWriter(outputStream);
                    printWriter.print("服务端你好，我是客户端");
                    printWriter.flush();
                    client.shutdownOutput();
                    try (InputStream inputStream = client.getInputStream()) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = inputStream.read(buffer)) > -1) {
                            System.out.println("客户端接收服务端消息：" + new String(buffer, 0, len));
                        }
                        client.shutdownInput();
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
