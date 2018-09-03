package Server;

import MyServer.HelloService;
import MyServer.HelloServiceImpl;

/**
 * Created by GEKL on 2018/9/3.
 */
public class NIOServerStart {
    public static void main(String args[]){
        NIOServer nioServer = new NIOServer();
        nioServer.Regeister(HelloService.class.getName(), HelloServiceImpl.class);
        Thread thread = new Thread(nioServer);
        thread.start();
    }
}
