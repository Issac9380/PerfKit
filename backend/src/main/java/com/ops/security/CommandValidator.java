package com.ops.security;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 命令验证器
 * 用于验证和清理用户输入的命令参数，防止命令注入攻击
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Component
public class CommandValidator {

    /**
     * 危险字符匹配模式：检测shell元字符和危险符号
     * 包括：分号、管道、反引号、美元符号、&符号、换行符、制表符、
     * 逻辑运算符、命令替换符、输入输出重定向等
     */
    private static final Pattern DANGEROUS_CHARS = Pattern.compile(
            "[;|`$&\n\r\t]||\\|\\||&&|\\$\\(|`|<|>"
    );

    /**
     * 安全参数匹配模式：仅允许字母、数字、下划线、连字符、点号、空格
     * 用于验证参数是否符合安全规范
     */
    private static final Pattern SAFE_PARAM = Pattern.compile("^[a-zA-Z0-9_\\-./ ]+$");

    /**
     * 验证参数是否符合安全规范
     * 检查参数是否仅包含允许的字符（字母、数字、下划线、连字符、点号、空格）
     *
     * @param param 待验证的参数字符串
     * @return 如果参数为空或符合安全规范返回true，否则返回false
     */
    public boolean validateParameter(String param) {
        if (param == null || param.isEmpty()) {
            return true;
        }
        return SAFE_PARAM.matcher(param).matches();
    }

    /**
     * 验证命令是否包含危险字符
     * 检测命令中是否存在shell元字符和可能导致命令注入的符号
     *
     * @param command 待验证的命令字符串
     * @return 如果命令不包含危险字符返回true，否则返回false
     */
    public boolean validateCommand(String command) {
        if (command == null || command.isEmpty()) {
            return false;
        }
        return !DANGEROUS_CHARS.matcher(command).find();
    }

    /**
     * 清理参数，移除所有不安全的字符
     * 将参数中的非安全字符替换为空字符串，仅保留字母、数字、下划线、连字符、点号和空格
     *
     * @param param 待清理的参数字符串
     * @return 清理后的安全参数字符串
     */
    public String sanitizeParameter(String param) {
        if (param == null) {
            return "";
        }
        return param.replaceAll("[^a-zA-Z0-9_\\-./]", "");
    }
}
