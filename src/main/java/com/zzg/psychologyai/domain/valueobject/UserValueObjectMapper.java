package com.zzg.psychologyai.domain.valueobject;

/**
 * 用户值对象转换辅助类（供 MapStruct 的 uses 引用）：
 * 只承载有业务不变量的值对象（用户名 / 邮箱 / 手机号 / 密码）与字符串列的双向转换。
 * 昵称等纯展示字段直接用 String，不在此转换。
 * <p>
 * 方法均为 static：MapStruct 生成实现时会静态调用，不会将其作为 Bean 注入，
 * 因此本类保持为普通领域工具类，无需注册为 Spring 组件。
 */
public final class UserValueObjectMapper {

    private UserValueObjectMapper() {
    }

    public static Username toUsername(String value) {
        return value == null ? null : new Username(value);
    }

    public static String fromUsername(Username value) {
        return value == null ? null : value.value();
    }

    public static Email toEmail(String value) {
        return Email.ofNullable(value);
    }

    public static String fromEmail(Email value) {
        return value == null ? null : value.value();
    }

    public static Phone toPhone(String value) {
        return Phone.ofNullable(value);
    }

    public static String fromPhone(Phone value) {
        return value == null ? null : value.value();
    }

    public static Password toPassword(String value) {
        return value == null ? null : new Password(value);
    }

    public static String fromPassword(Password value) {
        return value == null ? null : value.value();
    }
}
