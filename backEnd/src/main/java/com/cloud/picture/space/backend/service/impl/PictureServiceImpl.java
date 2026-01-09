package com.cloud.picture.space.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import com.cloud.picture.space.backend.manager.CosManager;
import com.cloud.picture.space.backend.manager.FileManager;
import com.cloud.picture.space.backend.manager.upload.FilePictureUpload;
import com.cloud.picture.space.backend.manager.upload.PictureUploadTemplate;
import com.cloud.picture.space.backend.manager.upload.UrlPictureUpload;
import com.cloud.picture.space.backend.model.dto.file.UploadPictureResult;
import com.cloud.picture.space.backend.model.dto.picture.*;
import com.cloud.picture.space.backend.model.entity.Picture;
import com.cloud.picture.space.backend.model.entity.Space;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.enums.PictureReviewStatusEnum;
import com.cloud.picture.space.backend.model.vo.PictureVo;
import com.cloud.picture.space.backend.model.vo.UserVo;
import com.cloud.picture.space.backend.service.PictureService;
import com.cloud.picture.space.backend.mapper.PictureMapper;
import com.cloud.picture.space.backend.service.SpaceService;
import com.cloud.picture.space.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yz2025120101
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-12-22 18:06:56
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;


    @Resource
    private CosManager cosManager;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private SpaceService spaceService;


    @Resource
    private TransactionTemplate transactionTemplate;


    /**
     * 上传图片
     */
    @Override
    public PictureVo uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 1.校验用户是否为空
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 校验空间是否存在
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 必须空间创建人（管理员）才能上传
            ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "您没有权限上传图片");

            // 校验空间额度
            ThrowUtils.throwIf(space.getTotalCount() >= space.getMaxCount(), ErrorCode.OPERATION_ERROR, "空间条数不够");
            ThrowUtils.throwIf(space.getTotalSize() >= space.getMaxSize(), ErrorCode.OPERATION_ERROR, "空间容量大小不够");

        }

        ThrowUtils.throwIf(ObjectUtil.isEmpty(inputSource), ErrorCode.PARAMS_ERROR, "图片不存在");


        // 2.判断当前操作是新增还是修改
        Long pictureId = null;
        Picture oldPicture = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 2.1如果是新增校验图片是否存在（如果不存在抛出异常）
        /* if (pictureId != null) {
            boolean exist = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exist, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        } */
        //【新增权限验证】
        if (pictureId != null) {
            oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(ObjectUtil.isEmpty(oldPicture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人获取管理员可编辑
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "您的权限不足！");
            }

            // 校验空间是否一致
            // 如果没传空间id，就复用原有的图片
            if (spaceId == null) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                // 传了，就必须和原有图片保持一致
                ThrowUtils.throwIf(ObjUtil.notEqual(spaceId, oldPicture.getSpaceId()), ErrorCode.PARAMS_ERROR, "空间id不一致");
            }
        }

        // 3.上传图片，得到信息
        // 3.1根据用户id划分目录 => 按照空间划分目录
        String uploadPathPrefix;
        if (spaceId == null) {
            uploadPathPrefix = String.format("/public/%s", loginUser.getId());
        } else {
            uploadPathPrefix = String.format("/space/%s", spaceId);
        }
        // 根据inputSource类型区分上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);

        // 4.构造入库的图片信息，使用数据万象获取
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());

        // 优化入库名称
        String picName = uploadPictureResult.getPicName();
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }

        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        picture.setSpaceId(spaceId);
        picture.setOriginalUrl(uploadPictureResult.getOriginalUrl());

        // 补充审核参数
        fillReviewParams(picture, loginUser);


        // 如果pictureId不为空，表示更新，否则更新
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }

        // 开启事务
        Long finalSpaceId = spaceId;
        Picture finalOldPicture = oldPicture;
        transactionTemplate.execute(status -> {
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");

            if (finalSpaceId != null) {

                long sizeDiff = picture.getPicSize(); // 默认为新图片大小
                long countDiff = 1; // 默认增加1个文件

                // 如果是更新操作，需要计算大小差异
                if (finalOldPicture != null) {
                    sizeDiff = picture.getPicSize() - finalOldPicture.getPicSize(); // 新旧图片大小差值
                    countDiff = 0; // 更新操作文件数不变
                }


                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize +" + sizeDiff)
                        .setSql("totalCount = totalCount + " + countDiff)
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "空间额度更新失败");
            }

            return picture;
        });

        // 如果是更新操作，清理旧的COS文件
        if (finalOldPicture != null) {
            this.clearPictureFile(finalOldPicture);
        }
        return PictureVo.objToVo(picture);
    }

    /**
     * 获取图片信息,并关联用户信息
     */
    @Override
    public PictureVo getPictureVo(Picture picture, HttpServletRequest httpServletRequest) {
        // 对象转封装类
        PictureVo pictureVo = PictureVo.objToVo(picture);
        // 管理查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVo userVo = userService.getUserVo(user);
            pictureVo.setUser(userVo);
        }
        return pictureVo;
    }

    /**
     * 获取查询包装类
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();

        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        String userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();

        // 添加审核状态参数
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewReason = pictureQueryRequest.getReviewReason();
        Long reviewerId = pictureQueryRequest.getReviewerId();

        // 如果是多字段搜索要拼接查询条件
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText));
        }
        queryWrapper.eq(ObjectUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjectUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjectUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjectUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjectUtil.isNotEmpty(picScale), "picScale", picScale);

        // 添加搜索条件
        queryWrapper.eq(ObjectUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewReason), "reviewReason", reviewReason);
        queryWrapper.eq(ObjectUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);

        queryWrapper.eq(ObjectUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.ge(ObjectUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        queryWrapper.lt(ObjectUtil.isNotEmpty(endEditTime), "editTime", endEditTime);


        // JSON数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tag", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 获取分页查询包装类
     */
    @Override
    public Page<PictureVo> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest httpServletRequest) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVo> pictureVoPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVoPage;
        }
        // 对像列表 =》封装对象列表
        List<PictureVo> pictureVoList = pictureList.stream().map(PictureVo::objToVo).collect(Collectors.toList());
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        pictureVoList.forEach(pictureVo -> {
            Long userId = pictureVo.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVo.setUser(userService.getUserVo(user));
        });
        pictureVoPage.setRecords(pictureVoList);
        return pictureVoPage;
    }

    /**
     * 验证图片数据
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据是，id不能为空，有参数则校验
        ThrowUtils.throwIf(ObjectUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }


    /**
     * 图片审核
     */
    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 获取校验信息，并验证
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        // 参数校验
        ThrowUtils.throwIf(ObjectUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id不能为空");
        ThrowUtils.throwIf(ObjectUtil.isNull(reviewStatus), ErrorCode.PARAMS_ERROR, "审核状态不能为空");
        // 判断图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 判断是否是重复操作
        ThrowUtils.throwIf(oldPicture.getReviewStatus().equals(reviewStatus), ErrorCode.OPERATION_ERROR, "请勿重复操作");

        // 更新审核状态
        Picture updatePicture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "操作失败");
    }

    /**
     * 填充审核参数
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        // 判断当前是否是管理员
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewReason("管理员自动过审");
            picture.setReviewTime(new Date());
        } else {
            // 非管理员，创建和编辑都要将图片改为待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    /**
     * 批量抓取并上传图片
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        // 校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(StrUtil.isBlank(searchText), ErrorCode.PARAMS_ERROR, "搜索内容不能为空");
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "一次抓取数量最多30条");

        // 名称前缀
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }

        // 定义抓取的地址
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("抓取图片失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "抓取图片失败");
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjectUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElementList = div.select("img.mimg");

        // 抓取
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过：{}", fileUrl);
                continue;
            }
            // 处理图片地址，防止出现转义问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
                log.info("处理后的图片地址：{}", fileUrl);
            }
            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            if (StrUtil.isNotBlank(namePrefix)) {
                pictureUploadRequest.setPicName(namePrefix + "_" + (uploadCount + 1));
            }

            try {
                PictureVo pictureVo = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("上传成功：{}", pictureVo);
                uploadCount++;
            } catch (Exception e) {
                log.error("上传图片失败：{}", e);
                continue;
            }
            // 添加延迟，降低请求频率，减少被封禁风险
            if (uploadCount < count) { // 最后一次不需要延迟
                try {
                    // 随机延迟1-3秒，避免固定的请求模式
                    int delay = 1000 + (int) (Math.random() * 2000); // 1000-3000毫秒
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    log.warn("线程睡眠被中断", e);
                    // 恢复中断状态
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }


    /*
     * 清理图片cos文件
     */
    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        // 判断该图片是否被多条记录使用
        String pictureUrl = oldPicture.getUrl();
        long count = this.lambdaQuery().eq(Picture::getUrl, pictureUrl).count();
        // 有不止一条记录用到了该图片，便不清理
        if (count > 1) {
            return;
        }
        // FIXME 注意，这里的url包含了域名，实际上只要传key值（存储路径）就够了；
        cosManager.deleteObject(extractObjectKey(pictureUrl));
        // 清理缩率图
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(extractObjectKey(thumbnailUrl));
        }
    }

    private String extractObjectKey(String fullUrl) {
        if (StrUtil.isBlank(fullUrl)) {
            return fullUrl;
        }
        // 从完整 URL 中提取对象键
        int startIndex = fullUrl.indexOf("//");
        if (startIndex != -1) {
            int endIndex = fullUrl.indexOf("/", startIndex + 2);
            if (endIndex != -1) {
                return fullUrl.substring(endIndex + 1); // 去除域名部分
            }
        }
        return fullUrl;
    }


    /**
     * 图片删除操作
     */
    @Override
    public void deletePicture(long pictureId, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);


        // 判断当前文件是否存在
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        // 校验权限
        checkPictureAuth(loginUser, oldPicture);

        // 开启事务

        transactionTemplate.execute(status -> {
            boolean result = this.removeById(pictureId);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败");
            }
            // 释放额度
            Long spaceId = oldPicture.getSpaceId();
            if (spaceId != null) {
                boolean update = spaceService.lambdaUpdate().eq(Space::getId, spaceId)
                        .setSql("totalSize = totalSize -" + oldPicture.getPicSize())
                        .setSql("totalCount = totalCount - 1 ")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "释放额度失败");
            }
            return true;
        });

        // 清理图片cos文件
        this.clearPictureFile(oldPicture);
    }

    /**
     * 操作图片权限校验（要删除的图片在私有空间中；也就是有空间id）
     */
    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            // 公共图库，仅本人或管理员可操作
            if (!userService.isAdmin(loginUser) && !picture.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            // 私有空间，仅本空间管理员（即本人）可操作
            if (!picture.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }

        }

    }

    /**
     * 编辑图片
     */
    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        // 将DTO转成实体类
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 标签类型转换
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 校验数据
        this.validPicture(picture);


        long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        // 设置权限
        checkPictureAuth(loginUser, oldPicture);
        // 补充审核参数
        this.fillReviewParams(picture, loginUser);
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

    }


}




