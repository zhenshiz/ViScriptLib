package com.viscript_lib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 VSL 早期访问器注册方法。
 *
 * <p>被标记的方法必须是静态方法，并且只接收一个 {@code RegisterAccessorEvent}
 * 参数。VSL 会在 LDLib2 扫描 RPC 前调用这些方法，因此不要依赖模组构造器里的
 * NeoForge 事件监听器来注册 RPC 参数需要的访问器。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ViScriptRegisterAccessors {
    /**
     * 可选依赖模组 id；非空时只有目标模组已加载才会调用该注册方法。
     */
    String modId() default "";
}
