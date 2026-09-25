package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 只选择实体类型本身的自动补全框。
 */
public class EntityTypeSearchBox extends RegistrySearchBox<EntityType<?>> {
    private static final Map<EntityType<?>, Class<? extends Entity>> VANILLA_ENTITY_CLASSES = createVanillaEntityClasses();

    public EntityTypeSearchBox() {
        this(EntityType.PIG);
    }

    public EntityTypeSearchBox(EntityType<?> defaultValue) {
        super(
                defaultValue,
                () -> BuiltInRegistries.ENTITY_TYPE,
                BuiltInRegistries.ENTITY_TYPE::getKey,
                entityType -> idString(BuiltInRegistries.ENTITY_TYPE.getKey(entityType)),
                EntityTypeSearchBox::searchEntityTypes,
                UIElementProvider.optionalIconText(
                        EntityTypeSearchBox::createEntityTypeIcon,
                        entityType -> Component.translatable(entityType.getDescriptionId())
                )
        );
    }

    @Nullable
    public ResourceLocation getSelectedEntityTypeId() {
        return getSelectedId();
    }

    public String getSelectedEntityTypeIdString() {
        return getSelectedIdString();
    }

    public EntityTypeSearchBox setEntityTypeFilter(Predicate<? super EntityType<?>> filter) {
        setCandidateFilter(filter);
        return this;
    }

    public EntityTypeSearchBox clearEntityTypeFilter() {
        clearCandidateFilter();
        return this;
    }

    public EntityTypeSearchBox filterEntityClass(Class<? extends Entity> entityClass) {
        return filterEntityClass(entityClass, null);
    }

    public EntityTypeSearchBox filterEntityClass(Class<? extends Entity> entityClass, @Nullable Level level) {
        Objects.requireNonNull(entityClass);
        return setEntityTypeFilter(entityType -> isEntityClass(entityType, entityClass, level));
    }

    public EntityTypeSearchBox onlyLivingEntities() {
        return filterEntityClass(LivingEntity.class);
    }

    public EntityTypeSearchBox onlyLivingEntities(@Nullable Level level) {
        return filterEntityClass(LivingEntity.class, level);
    }

    public EntityTypeSearchBox onlyMobs() {
        return filterEntityClass(Mob.class);
    }

    public EntityTypeSearchBox onlyMobs(@Nullable Level level) {
        return filterEntityClass(Mob.class, level);
    }

    @Nullable
    public static ResourceLocation getEntityTypeId(@Nullable EntityType<?> entityType) {
        return entityType == null ? null : BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
    }

    public static String getEntityTypeIdString(@Nullable EntityType<?> entityType) {
        var id = getEntityTypeId(entityType);
        return id == null ? "" : id.toString();
    }

    public static boolean isEntityClass(EntityType<?> entityType, Class<? extends Entity> entityClass) {
        return isEntityClass(entityType, entityClass, null);
    }

    public static boolean isEntityClass(EntityType<?> entityType, Class<? extends Entity> entityClass, @Nullable Level level) {
        Objects.requireNonNull(entityType);
        Objects.requireNonNull(entityClass);

        var vanillaEntityClass = VANILLA_ENTITY_CLASSES.get(entityType);
        if (vanillaEntityClass != null) {
            return entityClass.isAssignableFrom(vanillaEntityClass);
        }

        if (level == null) {
            return false;
        }

        try {
            var entity = entityType.create(level);
            return entity != null && entityClass.isAssignableFrom(entity.getClass());
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static void searchEntityTypes(String word, IResultHandler<EntityType<?>> searchHandler) {
        searchRegistry(
                BuiltInRegistries.ENTITY_TYPE,
                word,
                searchHandler,
                entityType -> LocalizationUtils.format(entityType.getDescriptionId())
        );
    }

    static IGuiTexture createEntityTypeIcon(EntityType<?> entityType) {
        var egg = SpawnEggItem.byId(entityType);
        if (egg == null) {
            return IGuiTexture.EMPTY;
        }
        return new ItemStackTexture(egg);
    }

    private static Map<EntityType<?>, Class<? extends Entity>> createVanillaEntityClasses() {
        var result = new IdentityHashMap<EntityType<?>, Class<? extends Entity>>();
        for (var field : EntityType.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !EntityType.class.isAssignableFrom(field.getType())) {
                continue;
            }
            if (!(field.getGenericType() instanceof ParameterizedType parameterizedType)) {
                continue;
            }
            var entityClass = getEntityClass(parameterizedType);
            if (entityClass == null) {
                continue;
            }
            try {
                result.put((EntityType<?>) field.get(null), entityClass);
            } catch (IllegalAccessException ignored) {
            }
        }
        return Collections.unmodifiableMap(result);
    }

    @Nullable
    private static Class<? extends Entity> getEntityClass(ParameterizedType entityTypeFieldType) {
        var typeArguments = entityTypeFieldType.getActualTypeArguments();
        if (typeArguments.length != 1 || !(typeArguments[0] instanceof Class<?> entityClass)) {
            return null;
        }
        if (!Entity.class.isAssignableFrom(entityClass)) {
            return null;
        }
        return entityClass.asSubclass(Entity.class);
    }
}
