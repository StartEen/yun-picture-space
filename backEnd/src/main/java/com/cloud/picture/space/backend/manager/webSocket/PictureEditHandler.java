package com.cloud.picture.space.backend.manager.webSocket;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.cloud.picture.space.backend.manager.webSocket.model.PictureEditActionEnum;
import com.cloud.picture.space.backend.manager.webSocket.model.PictureEditMessageTypeEnum;
import com.cloud.picture.space.backend.manager.webSocket.model.PictureEditRequestMessage;
import com.cloud.picture.space.backend.manager.webSocket.model.PictureEditResponseMessage;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-02-10  14:34
 * @Description: TODO webSocket处理器,在链接成功、链接关闭、接受到客户端消息时进行相应的处理
 */
@Slf4j
@Component
public class PictureEditHandler extends TextWebSocketHandler {


    @Resource
    private UserService userService;


    /**
     * 存储正在编辑的图片的id和用户id
     * key:pictureId【图片id】
     * value:id【用户id】
     */
    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();

    /**
     * 保存所有链接的会话
     * key:pictureId【会话id】
     * value:session【会话】
     */
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();


    /**
     * 编写广播会话【支持排除某个会话发送广播】
     */
    private void broadcastToPicture(Long pictureId,
                                    PictureEditResponseMessage pictureEditResponseMessage,
                                    WebSocketSession excludeSession) throws Exception {
        Set<WebSocketSession> sessions = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(sessions)) {
            // 创建ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            // 配置序列化：将Long类型转为String类型，解决精度丢失的问题
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            objectMapper.registerModule(module);

            // 序列化为JSON字符串
            String message = objectMapper.writeValueAsString(pictureEditResponseMessage);
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : sessions) {
                // 排除掉的session不发送
                if (ObjUtil.isNotEmpty(excludeSession) && excludeSession.equals(session)) {
                    continue;
                }
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }

    /**
     * 向所有广播发送
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws Exception {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }

    /**
     * 链接建立成功后，将会话保存到集合中
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 保存会话到集合中
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);

        // 构造响应
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("用户【%s】已进入编辑状态", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVo(user));

        // 广播给同一张图片的用户
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }

    /**
     * 接受客户端信息，根据消息类别执行处理
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        // 将消息解析为PictureEditMessage
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        String type = pictureEditRequestMessage.getType();
        PictureEditMessageTypeEnum pictureEditMessageTypeEnum = PictureEditMessageTypeEnum.valueOf(type);

        // 从Session属性中获取公共参数
        Map<String, Object> attributes = session.getAttributes();
        User user = (User) attributes.get("user");
        Long pictureId = (Long) attributes.get("pictureId");

        // 调用对应的消息处理方法
        switch (pictureEditMessageTypeEnum) {
            case ENTER_EDIT:
                handleEnterEditMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            case EDIT_ACTION:
                handleEditMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            case EXIT_EDIT:
                handleExitEditMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            default:
                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
                pictureEditResponseMessage.setMessage("消息类型错误");
                pictureEditResponseMessage.setUser(userService.getUserVo(user));
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(pictureEditResponseMessage)));
        }
    }

    /**
     * 用户进入编辑状态，设置当前用户为编辑用户，并广播给同一张图片的用户
     */
    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage,
                                       WebSocketSession session, User user, Long pictureId) throws Exception {
        // 没有用户正在编辑改图片，才能进行编辑
        if (!pictureEditingUsers.containsKey(pictureId)) {
            // 设置当前用户为编辑用户
            pictureEditingUsers.put(pictureId, user.getId());
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            String message = String.format("用户【%s】已进入编辑状态", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVo(user));
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    /**
     * 用户执行编辑操作是，将该操作同步给除自己外的其他客户端
     */
    public void handleEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditActionEnum actionEnum = PictureEditActionEnum.getEnumByValue(editAction);

        if (ObjUtil.isEmpty(actionEnum)) {
            return;
        }
        // 确认是当前编辑者
        if (ObjUtil.isNotEmpty(editingUserId) && editingUserId.equals(user.getId())) {
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            String message = String.format("用户【%s】执行【%s】操作", user.getUserName(), actionEnum.getText());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setEditAction(editAction);
            pictureEditResponseMessage.setUser(userService.getUserVo(user));
            // 广播给除自己外的其他客户端
            broadcastToPicture(pictureId, pictureEditResponseMessage, session);
        }
    }

    /**
     * 用户退出编辑操作时，移除当前用户的编辑状态，并广播给同一张图片的用户
     **/
    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        if (ObjUtil.isNotEmpty(editingUserId) && editingUserId.equals(user.getId())) {
            // 移除当前用户的编辑状态
            pictureEditingUsers.remove(pictureId);
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            String message = String.format("用户【%s】已退出编辑状态", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVo(user));
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    /**
     * 关闭链接时，移除当前用户的编辑状态，并从集合中删除当前会话，并广播给同一张图片的用户
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) throws Exception {
        // 从Session属性中获取公共参数
        Map<String, Object> attributes = session.getAttributes();
        User user = (User) attributes.get("user");
        Long pictureId = (Long) attributes.get("pictureId");

        // 移除当前用户的编辑状态
        handleExitEditMessage(null, session, user, pictureId);

        // 删除会话
        Set<WebSocketSession> sessions = pictureSessions.get(pictureId);
        if (ObjUtil.isNotEmpty(sessions)) {
            sessions.remove(session);
            if (ObjUtil.isEmpty(sessions)) {
                pictureSessions.remove(pictureId);
            }
        }
        log.info("用户【{}】退出编辑状态", user.getUserName());

        // 响应
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("用户【%s】已离开编辑状态", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVo(user));
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }
}
