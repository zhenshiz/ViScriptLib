package com.viscript_lib.accessor;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.utils.ReflectionUtils;
import com.viscript_lib.ViScriptLib;
import com.viscript_lib.annotation.ViScriptRegisterAccessors;
import com.viscript_lib.event.RegisterAccessorEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 在 LDLib2 RPC 扫描前注册 VSL 和附属模组提供的访问器。
 */
public final class PreRpcAccessorBootstrap {
    private static boolean bootstrapped;

    private PreRpcAccessorBootstrap() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;

        var event = new RegisterAccessorEvent();
        var count = new AtomicInteger();
        ReflectionUtils.findAnnotationStaticMethod(
                ViScriptRegisterAccessors.class,
                PreRpcAccessorBootstrap::shouldRegister,
                method -> {
                    if (invokeRegisterMethod(method, event)) {
                        count.incrementAndGet();
                    }
                },
                () -> ViScriptLib.LOGGER.debug("Registered {} pre-RPC accessor methods", count.get())
        );
    }

    private static boolean shouldRegister(Map<String, Object> annotationData) {
        var modId = annotationData.getOrDefault("modId", "").toString();
        return modId.isBlank() || Platform.isModLoaded(modId);
    }

    private static boolean invokeRegisterMethod(Method method, RegisterAccessorEvent event) {
        var parameters = method.getParameterTypes();
        if (parameters.length != 1 || parameters[0] != RegisterAccessorEvent.class) {
            ViScriptLib.LOGGER.error("@ViScriptRegisterAccessors method {}#{} must only accept RegisterAccessorEvent",
                    method.getDeclaringClass().getName(), method.getName());
            return false;
        }

        try {
            method.setAccessible(true);
            method.invoke(null, event);
            return true;
        } catch (InvocationTargetException e) {
            ViScriptLib.LOGGER.error("Failed to run @ViScriptRegisterAccessors method {}#{}",
                    method.getDeclaringClass().getName(), method.getName(), e.getCause());
        } catch (ReflectiveOperationException e) {
            ViScriptLib.LOGGER.error("Failed to run @ViScriptRegisterAccessors method {}#{}",
                    method.getDeclaringClass().getName(), method.getName(), e);
        }
        return false;
    }
}
