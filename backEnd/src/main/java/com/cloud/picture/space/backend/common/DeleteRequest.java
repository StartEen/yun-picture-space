package com.cloud.picture.space.backend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: LT
 * @CreateTime: 2025-12-17  17:18
 * @Description: TODO 删除请求体
 *
 */

@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;

}
