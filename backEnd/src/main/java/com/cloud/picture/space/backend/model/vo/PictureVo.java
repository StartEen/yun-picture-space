package com.cloud.picture.space.backend.model.vo;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.cloud.picture.space.backend.model.entity.Picture;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-24  09:43
 * @Description: TODO 上传成功后返回给前端的响应类
 *
 */
@Data
public class PictureVo implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图片 url
     */
    private String url;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    List<String> tags;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 创建用户id
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

    private static final long serialVersionUID = 1L;

    /**
     * vo转obj(封装类转对象)
     *
     * @param pictureVo vo
     * @return obj
     */
    public static Picture voToObj(PictureVo pictureVo) {
        if (ObjectUtil.isEmpty(pictureVo)) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureVo, picture);
        // 类型不同需要转换
        picture.setTags(JSONUtil.toJsonStr(pictureVo.getTags()));
        return picture;
    }

    /**
     * obj转vo(对象转封装类)
     *
     * @param picture obj
     * @return vo
     */
    public static PictureVo objToVo(Picture picture) {
        if (ObjectUtil.isEmpty(picture)) {
            return null;
        }
        PictureVo pictureVo = new PictureVo();
        BeanUtil.copyProperties(picture, pictureVo);
        // 类型不同需要转换
        pictureVo.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVo;
    }


}
