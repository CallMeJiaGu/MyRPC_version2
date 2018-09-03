package Proxy;

import Message.ReqMessage;
import Server.NIOServer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by GEKL on 2018/9/3.
 */
public class NIOInvokeHandler implements InvocationHandler{

    private Class target;

    public NIOInvokeHandler(Class target){
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        SocketChannel sc = SocketChannel.open();
        Object result =null;
        sc.connect(new InetSocketAddress(8088));
        sc.configureBlocking(false);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        //创建消息对象
        ReqMessage reqMessage = new ReqMessage();
        reqMessage.setClassName(target.getName());
        reqMessage.setMethodName(method.getName());
        reqMessage.setTypeParameters(method.getParameterTypes());
        reqMessage.setParametersVal(args);
        //写操作
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ObjectOutputStream oOut = new ObjectOutputStream(bOut);
        oOut.writeObject(reqMessage);
        sc.write(ByteBuffer.wrap(bOut.toByteArray()));

        //读结果
        byteBuffer.clear();
        int count ;
        while( (count = sc.read(byteBuffer))!=-1) {//如果数据还没传过来，那么count值就为0，那么byteBuffer就会因为没有数据而在下面的，ObjectInputStream(bInput)的时候报错
            if(count>0) {
                byteBuffer.flip();
                ByteArrayInputStream bInput = new ByteArrayInputStream(byteBuffer.array());
                ObjectInputStream oInput = new ObjectInputStream(bInput);
                result = oInput.readObject();
            }
        }
        sc.close();
        return result;
    }
}
