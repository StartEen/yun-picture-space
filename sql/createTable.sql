-- 用户表
create table if not exists user
(
    id            bigint auto_increment comment '用户id' primary key,
    userAccount   varchar(256)                           not null comment '用户账号',
    userPassword  varchar(512)                           not null comment '用户密码',
    userName      varchar(256)                           null comment '用户昵称',
    userAvatar    varchar(1024)                          null comment '用户头像',
    userProfile   varchar(512)                           null comment '用户简介',
    userRole      varchar(256) default 'user' comment '用户角色：user/admin',
    vipExpireTime datetime                               null comment '会员到期时间',
    vipCode       varchar(128)                           null comment '会员码',
    vipNumber     bigint                                 null comment '会员编号',
    shareCode     varchar(20)  DEFAULT NULL comment '分享码',
    inviteUser    bigint       DEFAULT NULL comment '邀请用户id',
    editTime      datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime    datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime    datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = 'utf8mb4_general_ci';


-- 图片表
create table if not exists picture
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(512)                       not null comment '图片 url',
    name         varchar(128)                       not null comment '图片名称',
    introduction varchar(512)                       null comment '简介',
    category     varchar(64)                        null comment '分类',
    tags         varchar(512)                       null comment '标签（Json数组）',
    picSize      bigint                             null comment '图片体积',
    picWidth     int                                null comment '图片宽度',
    picHeight    int                                null comment '图片高度',
    picScale     double                             null comment '图片宽高比例',
    picFormat    varchar(32)                        null comment '图片格式',
    userId       bigint                             not null comment '创建用户id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_name (name),                 -- 提升基于图片名称的查询性能
    INDEX idx_introduction (introduction), -- 用于模糊搜索图片简介
    INDEX idx_category (category),         -- 提升基于分类的查询性能
    INDEX idx_tags (tags),                 -- 提升基于标签的查询性能
    INDEX idx_userId (userId)              -- 提升基于用户ID的查询性能
) comment '图片' collate = utf8mb4_unicode_ci;

-- 新增图片表字段
ALTER TABLE picture
    -- 添加新列
    ADD COLUMN reviewStatus INT DEFAULT 0 not null comment '审核状态：0-待审核；1-通过；2-拒绝',
    ADD COLUMN reviewReason VARCHAR(512)  null comment '审核信息',
    ADD COLUMN reviewerId   BIGINT        null comment '审核人id',
    ADD COLUMN reviewTime   DATETIME      null comment '审核时间';
-- 创建基于reviewStatus列的索引
CREATE INDEX idx_reviewStatus ON picture (reviewStatus);


-- 新增图片表字段
ALTER TABLE picture
    -- 添加新列
    ADD COLUMN thumbnailUrl VARCHAR(512) null comment '缩略图url';


-- 空间表
create table if not exists space
(
    id         bigint auto_increment comment 'id' primary key,
    spaceName  varchar(128)                       null comment '空间名称',
    spaceLevel int      default 0                 null comment '空间等级:0-普通版 1-专业版 2-旗舰版',
    maxSize    bigint   default 0                 null comment '空间中图片的最大总大小',
    maxCount   bigint   default 0                 null comment '空间中图片的最大数量',
    totalSize  bigint   default 0                 null comment '当前空间中图片的总大小',
    totalCount bigint   default 0                 null comment '当前空间中图片的数量',
    userId     bigint                             not null comment '创建用户id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    -- 索引设计
    INDEX idx_userId (userId),        -- 提升基于用户ID的查询性能
    INDEX idx_spaceName (spaceName),  -- 提升基于空间名称的查询性能
    INDEX idx_spaceLevel (spaceLevel) -- 提升基于空间等级的查询性能
) comment '空间' collate = utf8mb4_unicode_ci;


-- 新增空间表字段
ALTER TABLE picture
    -- 添加新列
    ADD COLUMN spaceId BIGINT null comment '空间id(为空表示公共空间)';
-- 创建基于spaceId列的索引
CREATE INDEX idx_spaceId ON picture (spaceId);







