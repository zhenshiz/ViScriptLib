package com.viscript_lib.configurator.accessor;

import com.lowdragmc.lowdraglib2.configurator.accessors.TypesAccessor;
import com.lowdragmc.lowdraglib2.configurator.ui.Configurator;
import com.lowdragmc.lowdraglib2.configurator.ui.RegistrySearchComponent;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 物品组件类型配置器访问器，用自动补全输入框选择已注册的 DataComponentType。
 */
@LDLRegisterClient(name = "data_component_type", registry = "ldlib2:configurator_accessor")
public class DataComponentTypeAccessor extends TypesAccessor<DataComponentType<?>> {

    public DataComponentTypeAccessor() {
        super(DataComponentType.class);
    }

    @Override
    public DataComponentType<?> defaultValue(Field field, Class<?> type) {
        return DataComponents.CUSTOM_NAME;
    }

    @Override
    public Configurator create(String name,
                               Supplier<DataComponentType<?>> supplier,
                               Consumer<DataComponentType<?>> consumer,
                               boolean forceUpdate,
                               Field field,
                               Object owner) {
        return new RegistrySearchComponent<>(
                name,
                supplier,
                consumer,
                defaultValue(field, DataComponentType.class),
                forceUpdate,
                getDataComponentTypeRegistry(),
                UIElementProvider.text(componentType -> Component.literal(getComponentId(componentType)))
        ).setTranslator(DataComponentTypeAccessor::getComponentId);
    }

    private static String getComponentId(DataComponentType<?> componentType) {
        if (componentType == null) {
            return "";
        }
        ResourceLocation id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType);
        return id == null ? "" : id.toString();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Registry<DataComponentType<?>> getDataComponentTypeRegistry() {
        return (Registry) BuiltInRegistries.DATA_COMPONENT_TYPE;
    }
}
