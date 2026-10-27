package com.viscript_lib.ai;

/** 定义 AI 请求需要由服务端单独授予的能力。 */
public enum AiCapability {
    /** 读取静态注册表、数据包注册表和标签。 */
    REGISTRY_READ(false),
    /** 读取服务端世界状态；默认上下文不授予此能力。 */
    WORLD_READ(false),
    /** 编辑工程数据，要求至少二级命令权限。 */
    PROJECT_EDIT(true),
    /** 应用运行时数据，要求至少二级命令权限。 */
    RUNTIME_APPLY(true);

    private final boolean operatorOnly;

    AiCapability(boolean operatorOnly) {
        this.operatorOnly = operatorOnly;
    }

    /**
     * 检查此能力是否要求管理员权限。
     *
     * @return 要求至少二级命令权限时为 {@code true}
     */
    public boolean isOperatorOnly() {
        return operatorOnly;
    }
}
