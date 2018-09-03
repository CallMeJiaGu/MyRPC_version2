package Server;

import Message.ReqMessage;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by GEKL on 2018/9/3.
 */
public class NIOServer implements Runnable{

    private HashMap<String,Class> mapClass = new HashMap<>();

    public void Regeister(String name,Class c){
        if(!mapClass.containsKey(name)) {
            mapClass.put(name,c);
        }
    }

    @Override
    public void run() {
        try {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            Selector selector = Selector.open();
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress(8088));
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server: 启动成功...");
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            SocketChannel sc = null;
            Object result = null;
            while(true){
                System.out.println("Server: 等待触发事件...");
                selector.select();//没有事情产生会被阻塞在这
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while(keyIterator.hasNext()){
                    SelectionKey skey = keyIterator.next();

                    if(skey.isAcceptable()){
                        System.out.println("Server: 有链接进来");
                        sc =  ((ServerSocketChannel) skey.channel()).accept();
                        sc.configureBlocking(false);
                        sc.register(selector,SelectionKey.OP_READ);
                        System.out.println("Server: 与Client建立链接");
                    }
                    if(skey.isReadable()){
                        System.out.println("Server: 有可读操作进来");
                        sc = (SocketChannel) skey.channel();
                        byteBuffer.clear();
                        while(sc.read(byteBuffer)>0) {//因为有可能字节数很多，byteBuffer一次读不了
                            //读取channel中的数据
                            //byteBuffer.flip();
                            ByteArrayInputStream bInput = new ByteArrayInputStream(byteBuffer.array());
                            ObjectInputStream oInput = new ObjectInputStream(bInput);
                            ReqMessage reqMessage = (ReqMessage) oInput.readObject();
                            result = getResultByParams(reqMessage);
                            sc.register(selector, SelectionKey.OP_WRITE);
                        }
                    }
                    if(skey.isWritable()){
                        System.out.println("Server: 有可写操作进来");
                        sc = (SocketChannel) skey.channel();
                        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                        ObjectOutputStream oOut = new ObjectOutputStream(bOut);
                        oOut.writeObject(result);
                        sc.write(ByteBuffer.wrap(bOut.toByteArray()));
                        sc.close();
                    }
                    //移除当前监听的事件
                    keyIterator.remove();
                }
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public Object getResultByParams(ReqMessage reqMessage) throws Exception{
        Class c = mapClass.get(reqMessage.getClassName());
        Method method = c.getMethod(reqMessage.getMethodName(),reqMessage.getTypeParameters());
        Object result = method.invoke(c.newInstance(),reqMessage.getParametersVal());
        return result;
    }
}
