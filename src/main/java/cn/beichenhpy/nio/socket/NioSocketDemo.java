package cn.beichenhpy.nio.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * nio socket server and client demo <br/>
 * ServerSocketChannel for tcp, first register only use {@link SelectionKey#OP_ACCEPT} event<br/>
 * <br/>
 * result: <br/>
 * <br/>
 * <pre>
 * middle msg: hello_world123456789
 * middle msg: 0早上好中国1，
 * middle msg: 现在我有1冰淇
 * middle msg: 淋1，我最爱吃
 * middle msg: 🍦
 * client send message : hello_world1234567890早上好中国1，现在我有1冰淇淋1，我最爱吃🍦
 * </pre>
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
                int bufferSize = 20;
                ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
                //使用charBuffer 解决中文半包读取问题
                CharBuffer charBuffer = CharBuffer.allocate(bufferSize);
                CharsetDecoder utf8CharsetDecoder = StandardCharsets.UTF_8.newDecoder();
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
                                while (true) {
                                    int read = clientChannel.read(byteBuffer);
                                    if (read == -1) {
                                        break;
                                    }
                                    //byteBuffer切换至读模式 limit = position, position = 0
                                    byteBuffer.flip();
                                    //这里将byteBuffer写入到charBuffer中，可能会出现剩余的byte不够一个char，则会剩余byte为写光，即position != limit
                                    //所以后面需要使用byteBuffer#compact 将剩余的byte放到最前面，position = remaining  limit = capacity
                                    //重新写入到bytebuffer后，会继续从position = remaining开始写入 直到limit
                                    utf8CharsetDecoder.decode(byteBuffer, charBuffer, byteBuffer.limit() < bufferSize);
                                    //将charBuffer切换至读模式 limit = position, position = 0
                                    charBuffer.flip();
                                    message.append(charBuffer);
                                    System.out.println("middle msg: " +  charBuffer);
                                    //保存剩余的byte，并将数据放置头部 position = remaining  limit = capacity
                                    byteBuffer.compact();
                                    //清空charBuffer position = 0 limit = capacity
                                    charBuffer.clear();
                                }
                                System.out.println("client send message : " + message);
                                //close to see result 移除注释就可以看到结果，不然一直轮询打印看不到结果
                                selector.close();
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
            try (SocketChannel socketChannel = SocketChannel.open()) {
                //connect server
                socketChannel.connect(new InetSocketAddress("127.0.0.1", 9999));
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                byteBuffer.put("hello_world1234567890".getBytes(StandardCharsets.UTF_8));
                //reset position for write to channel
                byteBuffer.flip();
                socketChannel.write(byteBuffer);
                byteBuffer.clear();
                //会出现半包乱码问题
                String msg = "早上好中国1，现在我有1冰淇淋1，我最爱吃🍦";
                byteBuffer.put(msg.getBytes(StandardCharsets.UTF_8));
                byteBuffer.flip();
                socketChannel.write(byteBuffer);
                byteBuffer.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
