package com.geekbrains.chat.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

public class MainHandler extends SimpleChannelInboundHandler<String> {
    private static final List<Channel> channels=new ArrayList<>();
    private String clientName;
    private static int newClientIndex=1;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Клиент подключился: "+ctx);
        channels.add(ctx.channel());
        clientName="Клиент #"+ newClientIndex;
        newClientIndex++;
        broadCastMessage("SERVER","Подключился новый клиент " + clientName);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        System.out.println("Получено сообщение "+s);
        if (s.startsWith("/")){
            if (s.startsWith("/changename")){
               String newClientName=s.split("\\s",2)[1];
                broadCastMessage("SERVER","Клиент " + clientName + " смненил имя на " + newClientName);
                clientName=s.split("\\s",2)[1];
            }
            return;
        }
        broadCastMessage(clientName,s);
    }

    public void broadCastMessage (String clientName,String message){
        String out=String.format("[%s]: %s\n",clientName,message);
        for (Channel i:channels){
            i.writeAndFlush(out);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
         System.out.println("Клиент "+clientName+" отвалился");
         channels.remove(ctx.channel());
         broadCastMessage("SERVER","Клиент " + clientName + " вышел из сети");
         ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Клиент "+clientName+" отвалился");
        channels.remove(ctx.channel());
        broadCastMessage("SERVER","Клиент " + clientName + " вышел из сети");
        ctx.close();
    }
}
