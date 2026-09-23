package com.viscript_lib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 对于实现了{@link com.viscript_lib.util.ISkipDefaultedSerialize}接口的数据类，可以使用此注解取消跳过默认值序列化
 * <p>若注解于字段上，则该字段的序列化不会被跳过
 * <p>若注解于类上，则该类的所有字段的序列化都不会被跳过
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface DontSkipPersisted {
}
