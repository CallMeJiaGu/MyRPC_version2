package Client;

import MyServer.HelloService;
import Proxy.NIOInvokeHandler;
import java.lang.reflect.Proxy;


/**
 * Created by GEKL on 2018/8/28.
 */
public class RPCClient {

    public static void main(String args[]) throws Exception{
        NIOInvokeHandler invokeHandler = new NIOInvokeHandler(HelloService.class);
        HelloService helloService = (HelloService) Proxy.newProxyInstance(HelloService.class.getClassLoader(),new Class<?>[]{HelloService.class},invokeHandler);
        String result = helloService.sayHi("123") ;
        System.out.println(result);

    }

}
