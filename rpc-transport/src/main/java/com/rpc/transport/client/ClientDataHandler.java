package com.rpc.transport.client;

import com.google.common.base.Splitter;
import com.rpc.common.configuration.SeparatorEnum;
import com.rpc.common.util.LogUtil;
import com.rpc.common.rpc.RPCRequest;
import com.rpc.common.rpc.RPCResponse;
import com.rpc.transport.codec.MessageDecoder;
import com.rpc.transport.codec.MessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: Bojun Ji
 * @Description: client side data handler, send data to server, handle returned data
 * @Date: 2018/7/6_1:25 AM
 */
public class ClientDataHandler extends SimpleChannelInboundHandler<RPCResponse> {
    private String host;
    private int port;
    private RPCResponse response;
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    ;

    public ClientDataHandler(String serverAddress) {
        //get host and port from address
        Iterator<String> result = Splitter.on(SeparatorEnum.ADDRESS_SEPARATOR.getValue()).omitEmptyStrings().trimResults().split(serverAddress).iterator();
        int count = 0;
        while (result.hasNext() && count <= 1) {
            if (count == 0) {
                this.host = result.next();
                count++;
            } else if (count == 1) {
                this.port = Integer.parseInt(result.next());
            }
        }
    }

    public RPCResponse sendRequest(RPCRequest request) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel)
                                throws Exception {
                            channel.pipeline()
                                    .addLast(new MessageEncoder(RPCRequest.class))
                                    .addLast(new MessageDecoder(RPCResponse.class))
                                    .addLast(ClientDataHandler.this);
                        }
                    }).option(ChannelOption.TCP_NODELAY, true);
            // connect and send request to server
            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().writeAndFlush(request).sync();

            //sync, wait until channel read get new event of returned message
            LogUtil.logInfo(this.getClass(), "client start to wait response");
            countDownLatch.await();

            if (this.response != null) {
                //close connection when received response
                future.channel().closeFuture().sync();
                LogUtil.logInfo(this.getClass(), "client connection closed");
            }
            LogUtil.logInfo(this.getClass(), String.format("return response: %s", response));
            return this.response;
        } finally {
            group.shutdownGracefully();
        }
    }

    //listen to event and get response
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCResponse msg) throws Exception {
        LogUtil.logInfo(this.getClass(), String.format("got server response: %s", msg));
        this.response = msg;
        countDownLatch.countDown();
        LogUtil.logInfo(this.getClass(), "got response, wake up client and close connection");
        //short connection, so client close context to close connection
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        LogUtil.logError(ClientDataHandler.class, cause.getMessage());
        ctx.close();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public RPCResponse getResponse() {
        return response;
    }

    public void setResponse(RPCResponse response) {
        this.response = response;
    }
}
