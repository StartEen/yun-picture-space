package com.cloud.picture.space.backend.manager.webSocket.disruptor;


import com.cloud.picture.space.backend.manager.webSocket.model.PictureEditRequestMessage;
import com.cloud.picture.space.backend.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-04-13  17:52
 * @Description: TODO 事件类
 */
@Data
public class PictureEditEvent {

    /**
     * 消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前用户的session
     */
    private WebSocketSession session;

    /**
     * 当前用户
     */
    private User user;

    /**
     * 图片id
     */
    private Long pictureId;


}




