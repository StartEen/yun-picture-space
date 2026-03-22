package com.cloud.picture.space.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.picture.space.backend.api.aliYun.imageExpansion.model.CreateOutPaintingTaskResponse;
import com.cloud.picture.space.backend.api.aliYun.imageExpansion.model.CreatePictureOutPaintingTaskRequest;
import com.cloud.picture.space.backend.api.volcano.model.generatePicture.CreateGeneratePictureRequest;
import com.cloud.picture.space.backend.api.volcano.model.generatePicture.GeneratePictureTaskResponse;
import com.cloud.picture.space.backend.model.dto.picture.*;
import com.cloud.picture.space.backend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.vo.picture.PictureVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author yz2025120101
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-12-22 18:06:56
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param inputSource          上传的文件
     * @param pictureUploadRequest 上传图片请求体
     * @param loginUser            登录用户
     * @return 图片信息
     */
    PictureVo uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);


    /**
     * 获取图片信息,并关联用户信息
     *
     * @param picture            图片
     * @param httpServletRequest 请求
     * @return 图片信息
     */
    PictureVo getPictureVo(Picture picture, HttpServletRequest httpServletRequest);


    /**
     * 获取查询条件
     *
     * @param pictureQueryRequest 查询条件（支持从name和introduction中检索）
     * @return 查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);


    /**
     * 获取分页图片信息
     *
     * @param picturePage        分页信息
     * @param httpServletRequest 请求
     * @return 分页图片信息
     */
    Page<PictureVo> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest httpServletRequest);


    /**
     * 校验图片信息
     *
     * @param picture 图片信息
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核请求体
     * @param loginUser            登录用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);


    /**
     * 填充审核参数
     *
     * @param picture   图片信息
     * @param loginUser 登录用户
     */
    void fillReviewParams(Picture picture, User loginUser);


    /**
     * 批量抓取并上传图片
     *
     * @param pictureUploadByBatchRequest 图片上传批量请求通过抓取
     * @param loginUser                   登录用户
     * @return 创建的图片数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);


    /**
     * 清理图片文件
     *
     * @param oldPicture 旧图片信息
     */
    void clearPictureFile(Picture oldPicture);


    /**
     * 删除图片
     *
     * @param pictureId 图片id
     * @param loginUser 登录用户
     * @return 是否成功
     */
    void deletePicture(long pictureId, User loginUser);


    /**
     * 检查操作图片权限
     * （要删除的图片在私有空间中；也就是有空间id）
     *
     * @param loginUser 登录用户
     * @param picture   图片信息
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 编辑图片
     *
     * @param pictureEditRequest 图片编辑请求体
     * @param loginUser          登录用户
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);


    /**
     * 根据颜色搜索图片
     *
     * @param spaceId   空间id
     * @param picColor  图片颜色
     * @param loginUser 登录用户
     * @return 图片信息
     */
    List<PictureVo> searchPictureByColor(Long spaceId, String picColor, User loginUser);


    /**
     * 批量编辑图片
     *
     * @param pictureEditByBatchRequest 图片批量编辑请求体
     * @param loginUser                 登录用户
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);


    /**
     * 批量编辑图片分类和标签
     *
     * @param request     图片批量编辑请求体（批量编辑图片分类和标签）
     * @param spaceId     空间id
     * @param loginUserId 登录用户id
     */
    void batchEditPictureMetadata(PictureEditByBatchRequest request, Long spaceId, Long loginUserId);


    /**
     * 创建图片绘画任务
     *
     * @param createPictureOutPaintingTaskRequest 创建图片绘画任务请求体
     * @param loginUser                           登录用户
     * @return 创建的图片绘画任务信息
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);

    /**
     * 创建图片生成任务
     *
     * @param createGeneratePictureRequest 创建图片生成任务请求体
     * @param loginUser                    登录用户
     * @return 创建的图片生成任务信息
     */
    GeneratePictureTaskResponse createPictureOutGenerateTask(CreateGeneratePictureRequest createGeneratePictureRequest, User loginUser);
}
