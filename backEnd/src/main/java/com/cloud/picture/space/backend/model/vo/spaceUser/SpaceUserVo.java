package com.cloud.picture.space.backend.model.vo.spaceUser;


import com.cloud.picture.space.backend.model.entity.SpaceUser;
import com.cloud.picture.space.backend.model.vo.space.SpaceVo;
import com.cloud.picture.space.backend.model.vo.user.UserVo;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-19  09:56
 * @Description: TODO
 */
@Data
public class SpaceUserVo implements Serializable {

    /**
     * id
     */
    private Long id;


    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 用户id
     */
    private Long userId;


    /**
     * 空间角色：viewer-浏览者，editor-编辑者，admin-管理员
     */
    private String spaceRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户信息
     */
    private UserVo user;

    /**
     * 空间信息
     */
    private SpaceVo space;

    private static final long serialVersionUID = 1L;


    /**
     * 封装类转对象
     *
     * @param spaceUserVo 封装类
     * @return SpaceUser 对象
     */
    public static SpaceUser voToObj(SpaceUserVo spaceUserVo) {
        if (spaceUserVo == null) {
            return null;
        }
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserVo, spaceUser);
        return spaceUser;
    }

    /**
     * 对象转封装类
     *
     * @param spaceUser 封装类
     * @return SpaceUser 封装类
     */
    public static SpaceUserVo objToVo(SpaceUser spaceUser) {
        if (spaceUser == null) {
            return null;
        }
        SpaceUserVo spaceUserVo = new SpaceUserVo();
        BeanUtils.copyProperties(spaceUser, spaceUserVo);
        return spaceUserVo;
    }

}
