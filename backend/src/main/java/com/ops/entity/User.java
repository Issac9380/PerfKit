package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库中的user表，存储系统用户信息
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
@TableName("user")
public class User {
    /**
     * 用户ID
     * 使用雪花算法生成的唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名
     * 登录账号，具有唯一性
     */
    private String username;

    /**
     * 密码
     * BCrypt加密存储
     */
    private String password;

    /**
     * 邮箱
     * 用户的电子邮箱地址
     */
    private String email;

    /**
     * 角色
     * 用户权限角色，如 ADMIN、USER
     */
    private String role;

    /**
     * 账户状态
     * ACTIVE: 正常
     * INACTIVE: 禁用
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
