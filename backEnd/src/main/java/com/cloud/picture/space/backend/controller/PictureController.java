package com.cloud.picture.space.backend.controller;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.picture.space.backend.annotation.AuthCheck;
import com.cloud.picture.space.backend.api.aliYun.api.AliYunAPI;
import com.cloud.picture.space.backend.api.aliYun.model.EditPicture.CreateEditPictureTaskResponse;
import com.cloud.picture.space.backend.api.aliYun.model.EditPicture.CreatePictureEditPictureTaskRequest;
import com.cloud.picture.space.backend.api.aliYun.model.GeneratePictureUsePrompt.CreateGeneratePictureUsePromptRequest;
import com.cloud.picture.space.backend.api.aliYun.model.GeneratePictureUsePrompt.GeneratePictureUsePromptTaskResponse;
import com.cloud.picture.space.backend.api.aliYun.model.getTaskInfo.GetAITaskResponse;
import com.cloud.picture.space.backend.api.imageSearch.model.ImageSearchResult;
import com.cloud.picture.space.backend.api.imageSearch.sub.ImageSearchApiFacade;
import com.cloud.picture.space.backend.common.BaseResponse;
import com.cloud.picture.space.backend.common.DeleteRequest;
import com.cloud.picture.space.backend.common.ResultUtils;
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import com.cloud.picture.space.backend.manager.auth.SaSpaceCheckPermission;
import com.cloud.picture.space.backend.manager.auth.SpaceUserAuthManager;
import com.cloud.picture.space.backend.manager.auth.model.SpaceUserPermissionConstant;
import com.cloud.picture.space.backend.model.dto.picture.*;
import com.cloud.picture.space.backend.model.entity.Picture;
import com.cloud.picture.space.backend.model.entity.Space;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.enums.PictureReviewStatusEnum;
import com.cloud.picture.space.backend.model.vo.picture.PictureTagCategory;
import com.cloud.picture.space.backend.model.vo.picture.PictureVo;
import com.cloud.picture.space.backend.service.PictureService;
import com.cloud.picture.space.backend.service.SpaceService;
import com.cloud.picture.space.backend.service.UserConstant;
import com.cloud.picture.space.backend.service.UserService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-24  11:49
 * @Description: TODO 图片控制类
 */
@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Resource
    private AliYunAPI aliYunAPI;

    /**
     * 上传图片(可重新上传)
     */
    @PostMapping("/upload")
    // @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVo> uploadPicture(@RequestPart("file") MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVo pictureVo = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVo);
    }

    /**
     * 上传图片通过URL(可重新上传)
     */
    @PostMapping("/upload/url")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVo> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVo pictureVo = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVo);
    }


    /**
     * 删除图片
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 操作数据库
        pictureService.deletePicture(id, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 【管理员功能】修改图片
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和DTO进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 标签类型转化成所需的
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验
        pictureService.validPicture(picture);
        // 判断图片是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        // 补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);

        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    /**
     * 【管理员功能】从网页批量抓取图片并插入
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Integer uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }


    /**
     * 【管理员功能】根据id获取图片
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);

        return ResultUtils.success(picture);
    }


    /**
     * 根据id获取图片（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVo> getPictureVoById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);

        // 校验空间权限
        Space space = null;
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            // boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            // ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
            // space = spaceService.getById(spaceId);
            // ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, picture);
        }

        // 获取权限列表
        User loginUser = userService.getLoginUser(request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        PictureVo pictureVo = pictureService.getPictureVo(picture, request);
        pictureVo.setPermissionList(permissionList);

        return ResultUtils.success(pictureVo);
    }

    /**
     * 【管理员功能】分页获取图片列表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        if (pictureQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        Page<Picture> page = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(page);
    }

    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVo>> listPictureVoByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        if (pictureQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        // 公开图库
        if (spaceId == null) {
            // 普通用户默认只能查看已过审的数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            // 私有空间
            // boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            // ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
        }

        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVo> pictureVoPage = pictureService.getPictureVoPage(picturePage, request);
        return ResultUtils.success(pictureVoPage);
    }


    /**
     * 编辑图片
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditRequest == null || pictureEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 校验用户是否为空
        ThrowUtils.throwIf(ObjectUtil.isEmpty(loginUser), ErrorCode.PARAMS_ERROR);
        pictureService.editPicture(pictureEditRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 获取所有图片标签和分类
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "游戏", "创意","AI创作");
        List<String> categoryList = Arrays.asList("模版", "电商", "UI", "UX", "平面", "摄影", "插画");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 【管理员功能】图片审核
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null || pictureReviewRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取图片列表（Redis缓存）
     */
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVo>> listPictureVoByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        if (pictureQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 普通用户默认只能查看已过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 构建缓存Key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String redisKey = "yunPicture:listPictureVoByPage:" + hashKey;

        // 从Redis缓存中查询
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        String cachedValue = valueOps.get(redisKey);
        if (StringUtil.isNotBlank(cachedValue)) {
            // 如果缓存命中，返回结果
            Page<PictureVo> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }

        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVo> pictureVoPage = pictureService.getPictureVoPage(picturePage, request);

        // 存入Redis缓存中
        String cacheValue = JSONUtil.toJsonStr(pictureVoPage);
        // 5-10分钟随机过期，防止雪崩
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
        valueOps.set(redisKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);

        return ResultUtils.success(pictureVoPage);
    }


    // 构造本地缓存
    private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder().initialCapacity(1024).maximumSize(10000L).expireAfterWrite(5, TimeUnit.MINUTES).build();


    /**
     * 分页获取图片列表（多级缓存）
     */
    @PostMapping("/list/page/vo/redisAndCaffeine")
    public BaseResponse<Page<PictureVo>> listPictureVoByPageWithRedisAndCaffeine(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        if (pictureQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 普通用户默认只能查看已过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 构建缓存Key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = "yunPicture:listPictureVoByPage:" + hashKey;

        // 1.查询本地缓存（Caffeine）
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (StringUtil.isNotBlank(cachedValue)) {
            Page<PictureVo> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }

        // 2.从Redis缓存中查询
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        cachedValue = valueOps.get(cacheKey);
        if (StringUtil.isNotBlank(cachedValue)) {
            // 如果缓存命中，保存到本地缓存，返回结果
            LOCAL_CACHE.put(cacheKey, cachedValue);
            Page<PictureVo> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }


        // 3. 从数据库中查询
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVo> pictureVoPage = pictureService.getPictureVoPage(picturePage, request);

        // 4. 更新缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVoPage);

        // 存入本地缓存
        LOCAL_CACHE.put(cacheKey, cacheValue);

        // 存入Redis缓存中
        // 5-10分钟随机过期，防止雪崩
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
        valueOps.set(cacheKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);

        return ResultUtils.success(pictureVoPage);
    }

    /**
     * 以图搜图
     */
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null, ErrorCode.PARAMS_ERROR);
        Picture oldPicture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        List<ImageSearchResult> imageSearchResults = ImageSearchApiFacade.searchImage(oldPicture.getUrl());
        return ResultUtils.success(imageSearchResults);
    }


    /**
     * 以颜色搜图
     */
    @PostMapping("/search/color")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<PictureVo>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        User loginUser = userService.getLoginUser(request);
        List<PictureVo> results = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(results);
    }


    /**
     * 批量编辑
     */
    @PostMapping("/edit/batch")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量修改图片标签和分类
     */
    @PostMapping("/edit/batch/tagAndCategory")
    public BaseResponse<Boolean> batchEditPictureMetadata(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.batchEditPictureMetadata(pictureEditByBatchRequest, pictureEditByBatchRequest.getSpaceId(), loginUser.getId());
        return ResultUtils.success(true);
    }




    /**
     * AI P图 (阿里百炼云）
     */
    @PostMapping("/out_Edit/create_task")
    public BaseResponse<CreateEditPictureTaskResponse> createEditPictureTask(
            @RequestBody CreatePictureEditPictureTaskRequest createPictureEditPictureTaskRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(createPictureEditPictureTaskRequest == null ||
                createPictureEditPictureTaskRequest.getPictureId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        CreateEditPictureTaskResponse response = pictureService.createPictureEditTask(createPictureEditPictureTaskRequest, loginUser);
        log.info("创建图片编辑任务成功,返回结果：{}", response);
        return ResultUtils.success(response);
    }

    /**
     * 查询 AI扩图任务
     */
    @GetMapping("/out_aliAI/get_task")
    public BaseResponse<GetAITaskResponse> getAliYunAITask(String taskId) {
        ThrowUtils.throwIf(StringUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetAITaskResponse response = aliYunAPI.getOutPaintingTask(taskId);
        return ResultUtils.success(response);
    }

    /**
     * AI 文生图（阿里云qwen-image-2.0）
     */
    @PostMapping("/out_generate/create_picture")
    public BaseResponse<GeneratePictureUsePromptTaskResponse> generatePictureUseWordTask(
            @RequestBody CreateGeneratePictureUsePromptRequest createGeneratePictureUsePromptRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(createGeneratePictureUsePromptRequest == null ||
                        StrUtil.isBlank(createGeneratePictureUsePromptRequest.getText()),
                ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        GeneratePictureUsePromptTaskResponse response = pictureService.generatePictureUseWordTask(createGeneratePictureUsePromptRequest, loginUser);
        log.info("AI 文生图接口原始响应：{}", response);
        return ResultUtils.success(response);
    }




    /**
     * AI 以图生图 (阿里百炼云）
     */
    // @PostMapping("/out_generate/create_picture_by_picture")
    // public BaseResponse<GeneratePictureTaskResponse> generatePictureUsePictureTask(
    //         @RequestBody CreateGeneratePictureByPictureRequest createGeneratePictureByPictureRequest,
    //         HttpServletRequest request) {
    //     return ResultUtils.success();
    // }





}
