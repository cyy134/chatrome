package com.example.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/websocket/{userName}")
@Component
public class WebSocket {
    //用来存放每个客户端对应的webSocket对象。
    private String userName;
    //用userName为key，webSocket为对象保持每个客户端对应的WebSocket对象
    private static Map<String, WebSocket> webSocketMap = new ConcurrentHashMap<>();
    //    private static CopyOnWriteArraySet<WebSocket> webSockets = new CopyOnWriteArraySet<>();
    private Session session;

    @OnOpen
    public void onOpen(@PathParam("userName") String userName, Session session) {
        this.userName = userName;
        this.session = session;
        //messageType 1代表上线 2代表下线 3代表在线名单 4代表普通消息
        //先给所有人发送通知，说我上线了
        Map<String, Object> map1 = new HashMap();
        map1.put("messageType", 1);
        map1.put("username", userName);
        senMessageToAll(JSON.toJSONString(map1));
        webSocketMap.put(userName, this);
        //将在线名单发给自己
        Map<String, Object> map2 = new HashMap();
        map2.put("messageType", 3);
        Set<String> set = webSocketMap.keySet();
        map2.put("onlineUsers", set);
        sendMessageToOne(JSON.toJSONString(map2), userName);
    }

        @OnClose
        public void onClose () {
            webSocketMap.remove(userName);
            //messageType 1代表上线 2代表下线 3代表在线名单  4代表普通消息
            Map<String, Object> map1 = new HashMap();
            map1.put("messageType", 2);
            map1.put("onlineUsers", webSocketMap.keySet());
            map1.put("username", userName);
            senMessageToAll(JSON.toJSONString(map1));
            System.out.print(userName + "下线了!当前在线人数为 " + webSocketMap.size());
//        senMessageToAll(userName + "下线了!当前在线人数为 " + webSocketMap.size());
        }

        @OnMessage
        public void onMessage (String message, Session session){

            JSONObject jsonObject = JSON.parseObject(message);
            String text = jsonObject.getString("message");
            String userName = jsonObject.getString("userName");
            String toUser = jsonObject.getString("toUser");
            Map<String,Object> map1 = new HashMap();
            map1.put("messageType",4);
            map1.put("text",text);
            map1.put("username",userName);
            if (toUser.equals("All")) {
                map1.put("toUser","所有人");
                senMessageToAll(JSON.toJSONString(map1));
            } else {
                map1.put("toUser",toUser);
                sendMessageToOne(JSON.toJSONString(map1),toUser);
            }
        }

        public void sendMessageToOne (String message, String toUserName){
            WebSocket webSocket = webSocketMap.get(toUserName);
            webSocket.session.getAsyncRemote().sendText(message);
        }

        public void senMessageToAll (String message){
            for (WebSocket item : webSocketMap.values()) {
                item.session.getAsyncRemote().sendText(message);
            }
        }
//    @OnOpen
//    public void onOpen(Session session, @PathParam("userName") String userName){
//        this.session = session;
//        this.userName = userName;
//        webSockets.add(this);
//        System.out.print(userName+"上线了！在线人数： "+ webSockets.size());
//        //群发消息
//        sendMessage(userName+"上线了！当前在线人数为：" + webSockets.size());
//    }
//
//    @OnClose
//    public void onClose(){
//        webSockets.remove(this);
//        System.out.print(userName +"下线了!当前在线人数为 "+webSockets.size());
//        sendMessage(userName +"下线了!当前在线人数为 "+webSockets.size());
//    }
//
//    @OnMessage
//    public void onMessage(String message, Session session){
//        System.out.print("客户端消息： "+message);
//        senMessageToAll(userName+"："+message);
//    }

        @OnError
        public void onError (Session session, Throwable error){
            System.out.println("发生错误");
            error.printStackTrace();
        }
//
//
//    public void sendMessage(String message){
//        for (WebSocket item: webSockets){
//            item.session.getAsyncRemote().sendText(message);
//        }
//    }
}