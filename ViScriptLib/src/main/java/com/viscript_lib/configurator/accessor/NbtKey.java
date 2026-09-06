package com.viscript_lib.configurator.accessor;

import com.lowdragmc.lowdraglib2.configurator.accessors.TypesAccessor;
import com.lowdragmc.lowdraglib2.configurator.ui.Configurator;
import com.lowdragmc.lowdraglib2.gui.ui.elements.SearchComponent;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import com.viscript_lib.util.item.ItemUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class NbtKey implements IPersistedSerializable {
    public static final HashSet<String> candidateKeys = new HashSet<>();

    public static void recordItemKey(ItemStack stack) {
        candidateKeys.addAll(ItemUtil.getNbt(stack).getAllKeys());
    }

    @Getter @Setter
    private String key = "";

    @LDLRegisterClient(name = "nbt_key", registry = "ldlib2:configurator_accessor")
    public static class Accessor extends TypesAccessor<NbtKey> {

        public Accessor() {super(NbtKey.class);}

        @Override
        public NbtKey defaultValue(@Nullable Field field, @Nullable Class<?> type) {return new NbtKey();}

        @Override
        public Configurator create(String name, Supplier<NbtKey> supplier, Consumer<NbtKey> consumer, boolean forceUpdate, @Nullable Field field, @Nullable Object owner) {
            var search = new SearchComponent<String>().setSelected(supplier.get().key, false).searchStyle(style -> {
                style.maxItemCount(8);
                style.scrollerViewHeight(128);
                style.closeAfterSelect(true);
            });
            var ui = new SearchComponent.ISearchUI<String>() {
                @Override
                public @NotNull String resultText(@NotNull String s) {return s;}
                @Override
                public void onResultSelected(@Nullable String s) {
                    if (s == null) return;
                    consumer.accept(new NbtKey(s));
                    search.setSelected(s, false);
                }
                @Override
                public void search(String s, IResultHandler<String> iResultHandler) {
                    var candidates = candidateKeys.stream().filter(key -> key.contains(s)).toList();
                    for (String string : candidates) iResultHandler.accept(string);
                }
            };
            search.setSearchUI(ui);
            search.preview.setOverflowVisible(false);
            search.textField.setOverflowVisible(false);
            search.layout(layout -> {
                layout.flex(1);
                layout.height(18);
            });
            return new Configurator().addChild(search);
        }
    }
}
