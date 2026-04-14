package com.cloud.picture.space.backend.manager.webSocket.disruptor;


import com.cloud.picture.space.backend.manager.webSocket.model.PictureEditRequestMessage;
import com.cloud.picture.space.backend.model.entity.User;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-04-14  10:49
 * @Description: TODO 定义事件生产者
 * <p>
 * 生产者负责将数据发到Disruptor的唤醒缓冲区，
 * 维保正停机时所有的消磁能够被处理，我们通过shutdown方法完成停机
 */

@Slf4j
@Component
public class PictureEditEventProducer {

    @Resource
    Disruptor<PictureEditEvent> pictureEditEventDisruptor;

    public void publishEvent(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) {
        RingBuffer<PictureEditEvent> ringBuffer = pictureEditEventDisruptor.getRingBuffer();

        // 获取可以生成的位置
        long next = ringBuffer.next();
        PictureEditEvent pictureEditEvent = ringBuffer.get(next);
        pictureEditEvent.setSession(session);
        pictureEditEvent.setPictureEditRequestMessage(pictureEditRequestMessage);
        pictureEditEvent.setUser(user);
        pictureEditEvent.setPictureId(pictureId);

        // 发布事件
        ringBuffer.publish(next);
    }


    /**
     * 停机
     */
    @PreDestroy// 用于标记Bean 销毁前执行的方法。
    public void close() {
        pictureEditEventDisruptor.shutdown();
    }

}
