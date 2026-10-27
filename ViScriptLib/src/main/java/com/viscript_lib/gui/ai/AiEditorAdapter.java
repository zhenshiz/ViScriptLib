package com.viscript_lib.gui.ai;

import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib2.configurator.EditAction;
import com.lowdragmc.lowdraglib2.editor.project.IProject;
import com.viscript_lib.gui.editor.ViScriptEditor;

import java.util.List;

/**
 * 将附属编辑器的领域数据接入客户端 AI 会话。
 *
 * <p>所有方法均在客户端线程执行。模型只能调用这里声明的结构化操作；适配器不能执行
 * 命令、上传文件或直接修改服务端状态。草稿由会话隔离，成功后才创建可撤销的历史动作。
 */
public interface AiEditorAdapter {
    /**
     * 提供当前编辑器的领域说明。
     *
     * @return 描述编辑器用法、字段含义和能力边界的上下文
     */
    String guide();

    /**
     * 描述适配器接受的领域修改参数。
     *
     * @return {@code edit_project} 参数的 JSON Schema
     */
    JsonObject editSchema();

    /**
     * 分页读取草稿中的领域数据。
     *
     * @param project 当前草稿
     * @param offset 起始条目，非负
     * @param limit 最大条目数，范围为 1 至 128
     * @return 可提供给模型的结构化数据，不包含配置密钥或本地文件路径
     */
    JsonObject describe(IProject project, int offset, int limit);

    /**
     * 描述领域读取工具的参数；分页范围由会话补充。
     *
     * @return 包含分页及可选领域筛选项的对象 Schema
     */
    default JsonObject readSchema() {
        var schema = AiJson.object();
        AiJson.property(schema, "offset", "integer", "Zero-based page offset.");
        AiJson.property(schema, "limit", "integer", "Maximum entries per page.");
        return schema;
    }

    /**
     * 按领域筛选项分页读取草稿。
     *
     * @param project 当前草稿
     * @param arguments 读取参数
     * @param offset 非负起始条目
     * @param limit 本页最大条目数
     * @return 可提供给模型的领域上下文
     * @throws IllegalArgumentException 存在未支持的筛选项
     */
    default JsonObject describe(IProject project, JsonObject arguments, int offset, int limit) {
        AiJson.keys(arguments, "offset", "limit");
        return describe(project, offset, limit);
    }

    /**
     * 校验并修改隔离草稿，不触及编辑器当前项目。
     *
     * @param draft 可修改的隔离项目
     * @param arguments 符合已声明 Schema 的领域操作
     * @return 根据实际操作生成的变更说明，包含相关旧值和新值
     * @throws IllegalArgumentException 请求无效或超出能力范围
     */
    List<String> edit(IProject draft, JsonObject arguments);

    /**
     * 为当前工程创建完整的撤销、重做动作。
     *
     * <p>此方法本身不修改工程。动作必须同步刷新界面，保留先前历史所引用的对象，
     * 并且不得向历史栈递归追加记录。执行失败时必须恢复原状态。
     *
     * @param editor 当前编辑器
     * @param draft 已校验的完整草稿
     * @return 由 LDLib2 历史栈执行和撤销的动作
     */
    EditAction createAction(ViScriptEditor editor, IProject draft);

    /**
     * 切换主编辑器中的临时预览交互；允许浏览，暂停修改领域数据。
     *
     * @param editor 当前编辑器
     * @param previewing 是否正在等待玩家应用或放弃
     */
    void setPreviewMode(ViScriptEditor editor, boolean previewing);

    /**
     * 历史游标改变后刷新领域视图，不向历史栈追加记录。
     *
     * @param editor 当前编辑器
     */
    void refresh(ViScriptEditor editor);
}
