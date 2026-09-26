package com.viscript_lib.gui.configurator;

import com.lowdragmc.lowdraglib2.configurator.accessors.ItemStackAccessor;
import com.lowdragmc.lowdraglib2.configurator.accessors.TypesAccessor;
import com.lowdragmc.lowdraglib2.configurator.ui.Configurator;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.viscript_lib.util.item.ViScriptItemStack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 为 {@link ViScriptItemStack} 提供与 LDLib2 原版物品栈一致的编辑器控件。
 */
@LDLRegisterClient(name = "viscript_item_stack", registry = "ldlib2:configurator_accessor")
public class ViScriptItemStackAccessor extends TypesAccessor<ViScriptItemStack> {
    private final ItemStackAccessor delegate = new ItemStackAccessor();

    /**
     * 创建 ViScript 物品栈配置访问器。
     */
    public ViScriptItemStackAccessor() {
        super(ViScriptItemStack.class);
    }

    @Override
    public ViScriptItemStack defaultValue(@Nullable Field field, @Nullable Class<?> type) {
        return new ViScriptItemStack(delegate.defaultValue(field, ItemStack.class));
    }

    @Override
    public Configurator create(String name, Supplier<ViScriptItemStack> supplier,
                               Consumer<ViScriptItemStack> consumer, boolean forceUpdate,
                               @Nullable Field field, @Nullable Object owner) {
        return delegate.create(
                name,
                () -> {
                    var value = supplier.get();
                    return value == null ? ItemStack.EMPTY : value.toItemStack();
                },
                itemStack -> {
                    var current = supplier.get();
                    if (current != null
                            && current.isUnavailable()
                            && ItemStack.isSameItemSameTags(current.toItemStack(), itemStack)) {
                        consumer.accept(current.copyWithCount(itemStack.getCount()));
                    } else {
                        consumer.accept(new ViScriptItemStack(itemStack));
                    }
                },
                forceUpdate,
                field,
                owner
        );
    }
}
