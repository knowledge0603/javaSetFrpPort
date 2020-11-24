import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;

public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    /*
     * channelAction
     *
     * Channel channel action active
     *
     * The channel is active when the client actively links to the server's links.That is, the client and the server establish communication channels and can transfer data
     *
     */
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().localAddress().toString() + " Channel activated！");
    }

    /*
     * channelInactive
     *
     * Channel channels are Inactive
     *
     * This channel is inactive when the client actively disconnects from the server.This means that the client and the server have closed the communication channel and cannot transfer data
     *
     */
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().localAddress().toString() + " Channel inactivity！");
    }

    private String getMessage(ByteBuf buf) {
        byte[] con = new byte[buf.readableBytes()];
        buf.readBytes(con);
        try {
            return new String(con, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Function: Read the information sent by the client
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        String rev = getMessage(buf);
        System.out.println("Client receives server data:" + rev);

        //0000 command to open FRP Channel
        if(rev.equals("0000")) {
            int port = 0;
            //Able to open 27,833 ports
            for (int i = 37701; i <= 65534; i++) {
                if (!NetUtil.isLoclePortUsing(i)) {
                    port = i;
                    if(Command.processBuilderLinux(port)){
                        ctx.channel().writeAndFlush(String.valueOf(port));
                    }
                    return;
                }
            }
        }
    }

    /**
     * Function: The operation after reading the data sent by the client
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("The server has finished receiving data..");
        // The first method: Write an empty BUF and refresh the write area.Close sock Channel connection after completion.
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Function: An exception occurs on the server
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        System.out.println("Exception information：\r\n" + cause.getMessage());
    }
}