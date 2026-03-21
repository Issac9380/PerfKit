package com.ops.security;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class CommandValidator {

    // 危险字符模式：检测 shell 元字符和危险符号
    private static final Pattern DANGEROUS_CHARS = Pattern.compile(
            "[;|`$&\n\r\t]||\\|\\||&&|\\$\\(|`|<|>"
    );

    // 安全参数模式：仅允许字母、数字、下划线、连字符、点号、空格
    private static final Pattern SAFE_PARAM = Pattern.compile("^[a-zA-Z0-9_\\-./ ]+$");

    public boolean validateParameter(String param) {
        if (param == null || param.isEmpty()) {
            return true;
        }
        return SAFE_PARAM.matcher(param).matches();
    }

    public boolean validateCommand(String command) {
        if (command == null || command.isEmpty()) {
            return false;
        }
        return !DANGEROUS_CHARS.matcher(command).find();
    }

    public String sanitizeParameter(String param) {
        if (param == null) {
            return "";
        }
        return param.replaceAll("[^a-zA-Z0-9_\\-./]", "");
    }
}
