package cn.beichenhpy.nio.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * nio socket server and client demo <br/>
 * ServerSocketChannel for tcp, first register only use {@link SelectionKey#OP_ACCEPT} event<br/>
 * <br/>
 * result: <br/>
 * <br/>
 * <prev>
 *      client connected successfully from ip: /127.0.0.1:59495 <br/>
 *      middle msg : hello_worl  <br/>
 *      middle msg : d123456789  <br/>
 *      middle msg : 0  <br/>
 *      client send message : hello_world1234567890  <br/>
 * </prev>
 *
 */
public class NioSocketDemo {

    static class Server {


        public static void main(String[] args) {
            try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
                //channel config
                //non blocking
                serverSocketChannel.configureBlocking(false);
                //bind ip and port
                serverSocketChannel.bind(new InetSocketAddress("127.0.0.1", 9999));
                //define socket buffer
                ByteBuffer byteBuffer = ByteBuffer.allocate(10);
                //define selector
                try (Selector selector = Selector.open()) {
                    //register channel on selector accept event
                    //OP_ACCEPT事件则仅能提供给ServerSocketChannel使用 其他的事件都会报错
                    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                    while (selector.select() > 0) {
                        //loop for channel
                        Set<SelectionKey> selectionKeys = selector.selectedKeys();
                        Iterator<SelectionKey> iterator = selectionKeys.iterator();
                        while (iterator.hasNext()) {
                            SelectionKey selectionKey = iterator.next();
                            //接收事件就绪
                            if (selectionKey.isAcceptable()) {
                                //accept client and register read and write event to selector
                                SocketChannel clientChannel = serverSocketChannel.accept();
                                System.out.println("client: " + clientChannel);
                                clientChannel.configureBlocking(false);
                                clientChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                                System.out.println("client connected successfully from ip: " + clientChannel.getRemoteAddress());
                            } else if (selectionKey.isReadable()) {
                                //读事件就绪
                                SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
                                StringBuilder message = new StringBuilder();
                                while (clientChannel.read(byteBuffer) != -1) {
                                    byteBuffer.flip();
                                    String msg = new String(byteBuffer.array(), 0, byteBuffer.remaining());
                                    System.out.println("middle msg : " + msg);
                                    message.append(msg);
                                    byteBuffer.clear();
                                }
                                System.out.println("client send message : " + message);
                                //close to see result 移除注释就可以看到结果，不然一直轮询打印看不到结果
                                //selector.close();
                            }
                            // 指针之前的移除
                            iterator.remove();
                        }

                    }

                } catch (IOException selectorIoException) {
                    throw new IOException(selectorIoException);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class Client {
        public static void main(String[] args) {
            //a channel for socket
            try (SocketChannel socketChannel = SocketChannel.open()){
                //connect server
                socketChannel.connect(new InetSocketAddress("127.0.0.1", 9999));
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                byteBuffer.put("hello_world1234567890".getBytes(StandardCharsets.UTF_8));
                //reset position for write to channel
                byteBuffer.flip();
                socketChannel.write(byteBuffer);
                byteBuffer.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
