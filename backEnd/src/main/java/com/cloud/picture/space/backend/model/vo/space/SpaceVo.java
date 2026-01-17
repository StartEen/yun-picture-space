package com.cloud.picture.space.backend.model.vo.space;


import com.cloud.picture.space.backend.model.entity.Space;
import com.cloud.picture.space.backend.model.vo.user.UserVo;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-01  15:57
 * @Description: TODO
 */
@Data
public class SpaceVo implements Serializable {


    /**
     * 空间id
     */
    private Long id;


    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别 ： 0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;


    /**
     * 空间中图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间中图片的最大数量
     */
    private Long maxCount;

    /**
     * 当前空间中图片的总大小
     */
    private Long totalSize;

    /**
     * 当前空间中图片的数量
     */
    private Long totalCount;

    /**
     * 用户id
     */
    private Long userId;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 创建用户信息
     */
    private UserVo user;


    /**
     * 空间类型：0-个人空间 1-团队空间
     */
    private Integer spaceType;


    private static final long serialVersionUID = 1L;


    /**
     * 封装类vo转obj对象
     *
     * @param spaceVo vo对象
     * @return Space obj对象
     */
    public static Space voToObj(SpaceVo spaceVo) {
        if (spaceVo == null) {
            return null;
        }
        Space space = new Space();
        BeanUtils.copyProperties(spaceVo, space);
        return space;
    }

    /**
     * obj转vo对象
     *
     * @param space obj对象
     * @return SpaceVo vo对象
     */
    public static SpaceVo objToVo(Space space) {
        if (space == null) {
            return null;
        }
        SpaceVo spaceVo = new SpaceVo();
        BeanUtils.copyProperties(space, spaceVo);
        return spaceVo;
    }

}
