package com.viscript_lib.gui.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.graph.CustomGraphModelImpl;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.*;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.variable.ModifierFlags;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.variable.VariableScope;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.variable.VariableDeclarationModelBase;
import net.minecraft.network.chat.Component;
import org.joml.Vector2f;

import java.util.UUID;

/** 使用 LDLib2 图模型的创建、删除及连线检查提供通用 AI 图操作。 */
public final class AiGraphTools {
    private AiGraphTools() { }

    /** 按领域注册表创建图节点的回调。 */
    @FunctionalInterface
    public interface NodeCreator {
        /**
         * 创建已注册类型的节点。
         *
         * @param type 节点类型标识
         * @param id 节点 UUID
         * @param position 画布位置
         * @return 已加入图的节点
         */
        NodeModel create(String type, UUID id, Vector2f position);
    }

    /**
     * 描述当前节点及其可编辑常量和端口。
     *
     * @param node 草稿节点
     * @return 含稳定标识及实际编码的节点信息
     */
    public static JsonObject node(NodeModel node) {
        var row = new JsonObject();
        row.addProperty("kind", "node");
        row.addProperty("id", node.getUid().toString());
        if (node instanceof ICustomNodeModel custom && custom.getNode() != null) {
            var annotation = custom.getNode().getClass().getAnnotation(NodeAttribute.class);
            if (annotation != null) row.addProperty("type", annotation.name());
            row.addProperty("name", custom.getNode().getDisplayName().getString());
        } else row.addProperty("type", node instanceof ConstantNodeModel ? "constant" : "variable");
        row.addProperty("x", node.getPosition().x);
        row.addProperty("y", node.getPosition().y);
        var values = new JsonObject();
        for (var entry : node.getInputConstantsById().entrySet()) {
            try { values.add(entry.getKey(), AiStructuredData.constant(entry.getValue())); }
            catch (IllegalArgumentException ignored) { /* 无持久化编码的运行时常量不对 AI 开放。 */ }
        }
        if (node instanceof ConstantNodeModel constant) values.add("value", AiStructuredData.constant(constant.getConstant()));
        row.add("values", values);
        row.add("inputs", ports(node, true));
        row.add("outputs", ports(node, false));
        return row;
    }

    private static JsonArray ports(NodeModel node, boolean inputs) {
        var result = new JsonArray();
        for (var port : (inputs ? node.getInputsById() : node.getOutputsById()).values()) {
            var row = new JsonObject();
            row.addProperty("id", port.getUniqueName());
            row.addProperty("type", port.getDataTypeHandle().getIdentification());
            result.add(row);
        }
        return result;
    }

    /**
     * 返回图的节点、连线和变量声明。
     *
     * @param graph 当前领域图
     * @return 按节点、连线、变量排列的条目
     */
    public static JsonArray rows(CustomGraphModelImpl graph) {
        var result = new JsonArray();
        for (var model : graph.getNodeModels()) if (model instanceof NodeModel node) result.add(node(node));
        for (var wire : graph.getWireModels()) {
            if (wire.getFromPort() == null || wire.getToPort() == null) continue;
            var row = new JsonObject();
            row.addProperty("kind", "wire");
            row.addProperty("id", wire.getUid().toString());
            row.addProperty("from", wire.getFromPort().getNodeModel().getUid().toString());
            row.addProperty("output", wire.getFromPort().getUniqueName());
            row.addProperty("to", wire.getToPort().getNodeModel().getUid().toString());
            row.addProperty("input", wire.getToPort().getUniqueName());
            result.add(row);
        }
        for (var variable : graph.getGraphVariableModels()) if (variable != null) {
            var row = new JsonObject();
            row.addProperty("kind", "variable");
            row.addProperty("id", variable.getUid().toString());
            row.addProperty("name", variable.getName());
            row.add("initial", AiStructuredData.constant(variable.getInitializationModel()));
            result.add(row);
        }
        return result;
    }

    /**
     * 创建共享图操作的 Schema 列表。
     *
     * @return 可附加领域字段的操作 Schema 数组
     */
    public static JsonArray schemas() {
        var result = new JsonArray();
        for (String kind : new String[]{"add_node", "set_node", "remove_node", "connect", "disconnect", "add_constant", "add_variable", "variable_node", "remove_variable"}) {
            var schema = operation(kind);
            switch (kind) {
                case "add_node", "add_constant", "add_variable", "variable_node" -> {
                    AiJson.property(schema, "id", "string", "New unique UUID, supplied by you so later operations can refer to it.");
                    AiJson.property(schema, "type", "string", "Registered node/type ID from read_editor catalog; variable_node instead uses variable_id.");
                    AiJson.property(schema, "x", "number", "Canvas X position, defaults to 0.");
                    AiJson.property(schema, "y", "number", "Canvas Y position, defaults to 0.");
                    if (kind.equals("add_node")) AiJson.property(schema, "values", "object", "Optional constants by exact field IDs from catalog; use the returned JSON value encoding.");
                    if (kind.equals("add_constant") || kind.equals("add_variable")) AiJson.property(schema, "value", "object", "Wrapper {data:<typed JSON value>}; type from catalog value_types.");
                    if (kind.equals("add_variable")) AiJson.property(schema, "name", "string", "Variable name.");
                    if (kind.equals("variable_node")) AiJson.property(schema, "variable_id", "string", "Existing variable declaration UUID.");
                    AiJson.required(schema, "op", "id", kind.equals("variable_node") ? "variable_id" : "type");
                }
                case "set_node" -> {
                    AiJson.property(schema, "id", "string", "Existing node UUID.");
                    AiJson.property(schema, "values", "object", "Partial constant values by exact keys returned from read_editor (without their type/value wrapper).");
                    AiJson.property(schema, "x", "number", "New canvas X position.");
                    AiJson.property(schema, "y", "number", "New canvas Y position.");
                    AiJson.required(schema, "op", "id");
                }
                case "connect" -> {
                    for (String field : new String[]{"from", "output", "to", "input"}) AiJson.property(schema, field, "string", "Exact node UUID or port ID returned by read_editor.");
                    AiJson.required(schema, "op", "from", "output", "to", "input");
                }
                default -> {
                    AiJson.property(schema, "id", "string", "Existing node, wire or variable UUID.");
                    AiJson.required(schema, "op", "id");
                }
            }
            result.add(schema);
        }
        return result;
    }

    /**
     * 创建仅声明操作名的基础 Schema。
     *
     * @param name 操作名
     * @return 含必填操作名的对象 Schema
     */
    public static JsonObject operation(String name) {
        var result = AiJson.object();
        var values = new JsonArray(); values.add(name);
        AiJson.property(result, "op", "string", "Operation name.").add("enum", values);
        AiJson.required(result, "op");
        return result;
    }

    /**
     * 执行一个已声明的图操作，不操作正在显示的图。
     *
     * @param graph 隔离草稿图
     * @param op 操作参数
     * @param creator 领域节点工厂
     * @return 操作对应的真实变更说明
     * @throws IllegalArgumentException 标识、字段、类型或连接无效
     */
    public static String edit(CustomGraphModelImpl graph, JsonObject op, NodeCreator creator) {
        String kind = AiJson.string(op, "op");
        var before = op.has("id") ? element(graph.getModel(UUID.fromString(AiJson.string(op, "id")))) : com.google.gson.JsonNull.INSTANCE;
        JsonElement after = com.google.gson.JsonNull.INSTANCE;
        switch (kind) {
            case "add_node", "add_constant", "add_variable", "variable_node" -> {
                AiJson.keys(op, "op", "id", "type", "x", "y", "values", "value", "name", "variable_id", "graph");
                UUID id = UUID.fromString(AiJson.string(op, "id"));
                if (graph.getModel(id) != null) throw new IllegalArgumentException("Duplicate graph ID: " + id);
                var position = position(op, new Vector2f());
                if (kind.equals("add_node")) {
                    var node = creator.create(AiJson.string(op, "type"), id, position);
                    if (op.has("values")) setValues(node, op.getAsJsonObject("values"));
                } else if (kind.equals("variable_node")) {
                    var model = graph.getModel(UUID.fromString(AiJson.string(op, "variable_id")));
                    if (!(model instanceof VariableDeclarationModelBase variable)) throw new IllegalArgumentException("Unknown variable declaration");
                    graph.createVariableNode(variable, position, id, null);
                } else {
                    var typeName = AiJson.string(op, "type");
                    var type = (kind.equals("add_variable") ? graph.getVariableSupportTypes() : graph.getLibrarySupportTypes()).stream()
                            .filter(t -> t.getIdentification().equals(typeName)).findFirst().orElseThrow(() -> new IllegalArgumentException("Unavailable value type: " + typeName));
                    var constant = graph.createConstantValue(type);
                    if (op.has("value")) {
                        var value = op.getAsJsonObject("value"); AiJson.keys(value, "data");
                        if (!value.has("data")) throw new IllegalArgumentException("Missing value.data");
                        AiStructuredData.setConstant(constant, value.get("data"));
                    }
                    if (kind.equals("add_variable")) graph.createGraphVariableDeclaration(type, AiJson.string(op, "name"), ModifierFlags.NONE,
                            VariableScope.LOCAL, null, Integer.MAX_VALUE, constant, id, null);
                    else graph.createConstantNode(type, "", position, id, n -> n.getConstant().setValue(constant.getValue()), null);
                }
                after = element(graph.getModel(id));
            }
            case "set_node" -> {
                AiJson.keys(op, "op", "id", "values", "x", "y", "graph");
                var node = findNode(graph, AiJson.string(op, "id"));
                if (op.has("values")) setValues(node, op.getAsJsonObject("values"));
                node.setPosition(position(op, node.getPosition()));
                after = node(node);
            }
            case "remove_node" -> {
                AiJson.keys(op, "op", "id", "graph");
                graph.deleteElements(java.util.List.of(findNode(graph, AiJson.string(op, "id"))));
            }
            case "connect" -> {
                AiJson.keys(op, "op", "from", "output", "to", "input", "graph");
                var from = findNode(graph, AiJson.string(op, "from")).getOutputsById().get(AiJson.string(op, "output"));
                var to = findNode(graph, AiJson.string(op, "to")).getInputsById().get(AiJson.string(op, "input"));
                if (from == null || to == null || !graph.canAssignTo(to, from))
                    throw new IllegalArgumentException("Invalid graph connection; use the actual free ports and compatible types");
                var wire = graph.createWire(to, from);
                if (wire == null) throw new IllegalArgumentException("Invalid graph connection");
                after = element(wire);
            }
            case "disconnect", "remove_variable" -> {
                AiJson.keys(op, "op", "id", "graph");
                var element = graph.getModel(UUID.fromString(AiJson.string(op, "id")));
                if (element == null || (kind.equals("remove_variable") ? !(element instanceof VariableDeclarationModelBase)
                        : !(element instanceof com.lowdragmc.lowdraglib2.nodegraphtookit.model.wire.WireModel)))
                    throw new IllegalArgumentException("Unknown graph element or wrong type");
                graph.deleteElements(java.util.List.of(element));
            }
            default -> throw new IllegalArgumentException("Unsupported graph operation: " + kind);
        }
        return Component.translatable("viscript_lib.ai.change.field", kind, before.toString(), after.toString()).getString();
    }

    private static JsonElement element(Object model) {
        if (model instanceof NodeModel node) return node(node);
        var row = new JsonObject();
        if (model instanceof com.lowdragmc.lowdraglib2.nodegraphtookit.model.wire.WireModel wire
                && wire.getFromPort() != null && wire.getToPort() != null) {
            row.addProperty("kind", "wire"); row.addProperty("id", wire.getUid().toString());
            row.addProperty("from", wire.getFromPort().getNodeModel().getUid().toString());
            row.addProperty("output", wire.getFromPort().getUniqueName());
            row.addProperty("to", wire.getToPort().getNodeModel().getUid().toString());
            row.addProperty("input", wire.getToPort().getUniqueName());
        } else if (model instanceof VariableDeclarationModelBase variable) {
            row.addProperty("kind", "variable"); row.addProperty("id", variable.getUid().toString());
            row.addProperty("name", variable.getName());
            row.add("initial", AiStructuredData.constant(variable.getInitializationModel()));
        } else return com.google.gson.JsonNull.INSTANCE;
        return row;
    }

    /**
     * 修改节点的已声明常量。
     *
     * @param node 草稿节点
     * @param values 以常量 ID 为键的值
     * @throws IllegalArgumentException 存在未知或连接中的字段
     */
    public static void setValues(NodeModel node, JsonObject values) {
        for (var entry : values.entrySet()) {
            var constant = node instanceof ConstantNodeModel n && entry.getKey().equals("value") ? n.getConstant() : node.getInputConstantsById().get(entry.getKey());
            if (constant == null) throw new IllegalArgumentException("Unknown node constant: " + entry.getKey());
            var port = node.getInputsById().get(entry.getKey());
            if (port != null && !port.getConnectedWires().isEmpty()) throw new IllegalArgumentException("Disconnect the input before changing its literal value: " + entry.getKey());
            AiStructuredData.setConstant(constant, entry.getValue());
            node.defineNode();
        }
    }

    private static NodeModel findNode(CustomGraphModelImpl graph, String id) {
        if (graph.getModel(UUID.fromString(id)) instanceof NodeModel node) return node;
        throw new IllegalArgumentException("Unknown graph node: " + id);
    }

    private static Vector2f position(JsonObject op, Vector2f fallback) {
        float x = coordinate(op, "x", fallback.x), y = coordinate(op, "y", fallback.y);
        return new Vector2f(x, y);
    }

    private static float coordinate(JsonObject op, String key, float fallback) {
        if (!op.has(key)) return fallback;
        JsonElement value = op.get(key);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber() || !Float.isFinite(value.getAsFloat())) throw new IllegalArgumentException("Invalid coordinate: " + key);
        return value.getAsFloat();
    }
}
