package com.cloud.picture.space.backend.model.vo.user;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户脱敏数据
 *
 * @TableName user
 */
@Data
public class UserVo implements Serializable {
    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户账号
     */
    private String userAccount;


    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 会员到期时间
     */
    private Date vipExpireTime;

    /**
     * 会员码
     */
    private String vipCode;

    /**
     * 会员编号
     */
    private Long vipNumber;

    /**
     * 分享码
     */
    private String shareCode;

    /**
     * 邀请用户id
     */
    private Long inviteUser;

    /**
     * 创建时间
     */
    private Date createTime;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}