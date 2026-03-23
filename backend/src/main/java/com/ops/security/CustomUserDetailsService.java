package com.ops.security;

import com.ops.entity.User;
import com.ops.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 自定义用户详情服务实现类
 * 实现Spring Security的UserDetailsService接口，用于加载用户认证信息
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * 用户Mapper，用于从数据库查询用户信息
     */
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                        .eq("username", username)
        );

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return buildUserDetails(user);
    }

    /**
     * 根据用户ID加载用户详情
     * 用于JWT认证成功后通过用户ID查询用户信息
     *
     * @param userId 用户的唯一标识ID
     * @return UserDetails 用户详情对象
     * @throws UsernameNotFoundException 如果用户不存在
     */
    public UserDetails loadUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + userId);
        }
        return buildUserDetails(user);
    }

    /**
     * 构建Spring Security的UserDetails对象
     * 将数据库中的User实体转换为Spring Security所需的用户详情格式
     *
     * @param user 用户实体对象
     * @return UserDetails Spring Security用户详情对象
     */
    private UserDetails buildUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
