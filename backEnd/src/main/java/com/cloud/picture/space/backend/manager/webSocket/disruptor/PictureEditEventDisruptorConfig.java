package com.cloud.picture.space.backend.manager.webSocket.disruptor;


import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-04-14  09:45
 * @Description: TODO 将定义的事件关联到Disruptor实例中
 */
@Configuration
public class PictureEditEventDisruptorConfig {
    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;


    @Bean("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> messageModelRingBuffer() {
        // ringBuffer的大小
        int ringBufferSize = 1024 * 256;
        Disruptor<PictureEditEvent> disruptor = new Disruptor<>(
                PictureEditEvent::new,
                ringBufferSize,
                ThreadFactoryBuilder.create().setNamePrefix("pictureEditEventDisruptor").build()
        );
        //设置消费者
        disruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler);
        disruptor.start();
        return disruptor;
    }


}
