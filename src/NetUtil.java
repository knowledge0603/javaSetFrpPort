
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetUtil {
    public static void main(String[] args) {
        System.out.println(isLoclePortUsing(8086));
    }
    /***
     *  true:already in using  false:not using
     * @param port
     */
    public static boolean isLoclePortUsing(int port){
        boolean flag = false;
        try {
            flag = isPortUsing("127.0.0.1", port);
        } catch (Exception e) {
        }
        return flag;
    }
    /***
     *  true:already in using  false:not using
     * @param host
     * @param port
     * @throws UnknownHostException
     */
    public static boolean isPortUsing(String host,int port) throws IOException {
        boolean flag = false;
        InetAddress theAddress = InetAddress.getByName(host);
        Socket socket =null;
        try {
            socket = new Socket(theAddress,port);
            flag = true;
            socket.close();
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("----------端口"+port+"没有被开启");
        }finally {
            socket.close();
        }
        return flag;
    }
}
