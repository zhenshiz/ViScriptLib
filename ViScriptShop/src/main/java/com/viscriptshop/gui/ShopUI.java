package com.viscriptshop.gui;

import com.lowdragmc.lowdraglib2.configurator.ui.NumberConfigurator;
import com.lowdragmc.lowdraglib2.configurator.ui.StringConfigurator;
import com.lowdragmc.lowdraglib2.gui.texture.*;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.GridTemplate;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.math.Size;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import com.viscript_lib.util.CountTextUtil;
import com.viscript_lib.util.item.SimpleItemStackFilter;
import com.viscriptshop.Config;
import com.viscriptshop.ViscriptShop;
import com.viscriptshop.event.neoforge.ShopClientEvent;
import com.viscriptshop.gui.components.Message;
import com.viscriptshop.gui.components.PlayerHeadElement;
import com.viscriptshop.gui.components.SceneToggleBuilder;
import com.viscriptshop.gui.components.theme.ShopButton;
import com.viscriptshop.gui.components.theme.ShopScrollerView;
import com.viscriptshop.gui.components.theme.ShopTheme;
import com.viscriptshop.gui.data.*;
import com.viscriptshop.network.c2s.BuyMerchantPayload;
import com.viscriptshop.network.c2s.GetItemCountC2SPayload;
import com.viscriptshop.util.ShopHelper;
import com.viscriptshop.util.UIElementUtil;
import com.viscriptshop.util.ViScriptShopClientUtil;
import dev.vfyjxf.taffy.style.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ShopUI extends UIElement {
    Minecraft minecraft = Minecraft.getInstance();
    //ui
    public ScrollerView categoryView = new ShopScrollerView();
    public ScrollerView merchantsView = new ShopScrollerView();
    public ScrollerView shoppingCarView = new ShopScrollerView();
    public ScrollerView inventoryView = new ShopScrollerView();
    public SearchComponent<ItemStack> searchComponent;
    private final Toggle currencyLayoutToggle;

    //主题样式
    private final ShopTheme theme = ShopTheme.current();
    private final IGuiTexture LIST_BACKGROUND = theme.merchantList();
    private final IGuiTexture GRID_BACKGROUND = theme.merchantGrid();
    private final SpriteTexture RIGHT_ARROW = SpriteTexture.of(ViscriptShop.formattedMod("textures/right_arrow.png"));
    private final SpriteTexture LOCK = SpriteTexture.of(ViscriptShop.formattedMod("textures/lock.png"));
    private final SpriteTexture COIN = SpriteTexture.of(ViscriptShop.formattedMod("textures/coin.png"));
    private static final float CURRENCY_GRID_CARD_WIDTH = 50f;
    private static final float CURRENCY_GRID_GAP = 3f;

    //data
    //玩家身上对应物品的数量
    public List<AggregatedResources.ItemEntry> playerItems = new ArrayList<>();
    //打开的商店信息
    public ShopInfo currentShopInfo;
    //商店文件位置（用于购买后保存数据）
    private String shopLocation;
    //玩家选择的商店信息
    @Getter
    @Setter
    private CategoryInfo selectedCategory;
    @Getter
    @Setter
    private ItemStack searchItem = ItemStack.EMPTY;
    @Getter
    @Setter
    private String searchId = "";
    //当前模式 true为物品查询 false为序号查询
    @Getter
    @Setter
    private boolean searchMode = true;

    @Getter
    @Setter
    private boolean currencyGridLayout = false;
    private int currencyGridColumns = -1;

    public ShopUI(String shopLocation, ShopInfo shopInfo, String title) {
        this(shopLocation, shopInfo, title, null, null);
    }

    public ShopUI(String shopLocation, ShopInfo shopInfo, String title, String categoryId, String merchantId) {
        this.shopLocation = shopLocation;
        this.playerItems.clear();
        this.currentShopInfo = initCurrentShopInfo(shopInfo);
        if (minecraft.player != null) {
            // 根据 categoryId 查找对应分类
            if (categoryId != null && !categoryId.isEmpty()) {
                for (CategoryInfo category : this.currentShopInfo.getCategoryInfos()) {
                    if (categoryId.equals(category.getId())) {
                        selectedCategory = category;
                        break;
                    }
                }
            }
            // 如果没找到指定分类，使用第一个分类
            if (selectedCategory == null) {
                selectedCategory = this.currentShopInfo.getCategoryInfos().get(0);
            }

            // 根据 merchantId 查找对应商品的索引
            if (merchantId != null && !merchantId.isEmpty()) {
                for (int i = 0; i < selectedCategory.getMerchants().size(); i++) {
                    MerchantInfo merchant = selectedCategory.getMerchants().get(i);
                    if (merchantId.equals(merchant.getId())) {
                        this.searchId = String.valueOf(i + 1);
                        this.searchMode = false;
                        break;
                    }
                }
            }

            RPCPacketDistributor.rpcToServer(GetItemCountC2SPayload.GET_ITEM_COUNT, selectedCategory);
        }
        this.layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.justifyContent(AlignContent.CENTER);
            layout.alignItems(AlignItems.CENTER);
        }).addEventListener(UIEvents.TICK, event -> MinecraftForge.EVENT_BUS.post(new ShopClientEvent.Tick(this)));
        UIElement root = new UIElement()
                .setId("shop_ui_shell")
                .addClass(theme.styleClass());
        root.layout((layout) -> {
            layout.widthPercent(90);
            layout.heightPercent(91);
            layout.gapAll(3);
            layout.flexDirection(FlexDirection.ROW);
            layout.justifyContent(AlignContent.CENTER);
            layout.alignItems(AlignItems.CENTER);
        });
        this.addChildren(root);
        //左
        UIElement left = new UIElement().setId("shop_ui_categories").layout(layout -> {
            layout.heightPercent(100);
            layout.widthPercent(22);
            layout.gapAll(3);
            layout.flexDirection(FlexDirection.COLUMN);
        });
        UIElement leftTop = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(10);
        }).style(style -> {
            style.backgroundTexture(theme.categoryHeader());
        }).addChild(new Label().setText("viscript_shop.data.shop.categoryInfos").textStyle(textStyle -> {
            textStyle.textAlignHorizontal(Horizontal.CENTER).textAlignVertical(Vertical.CENTER);
        }).layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
        }));

        UIElement leftBottom = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
        }).style(style -> {
            style.backgroundTexture(theme.categoryPanel());
        });

        UIElement leftBottomTop = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(91);
        });

        categoryView.layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
        }).addEventListener(UIEvents.TICK, event -> {
            reloadCategoryList();
        });
        categoryView.verticalScroller.layout(layout -> layout.marginRight(3));
        categoryView.viewPort.getStyle().backgroundTexture(IGuiTexture.EMPTY);
        categoryView.viewContainer.layout(layout -> {
            layout.gapColumn(5);
            layout.paddingAll(3);
            layout.flexDirection(FlexDirection.COLUMN);
        });

        leftBottomTop.addChild(categoryView);

        UIElement leftBottomBottom = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.alignItems(AlignItems.CENTER);
            layout.flexDirection(FlexDirection.ROW);
        }).addChildren(
                new UIElement().layout(layout -> {
                    layout.widthPercent(21);
                    layout.justifyContent(AlignContent.CENTER);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(new UIElement().layout(layout -> {
                    layout.width(14);
                    layout.height(14);
                }).style(style -> style.backgroundTexture(COIN))),
                new Label().textStyle(textStyle -> {
                    textStyle.textAlignHorizontal(Horizontal.CENTER).textAlignVertical(Vertical.CENTER);
                }).layout(layout -> {
                    layout.widthPercent(100);
                    layout.flex(1);
                }).addEventListener(UIEvents.TICK, event -> {
                    ((Label) event.currentElement).setText(String.valueOf(ViScriptShopClientUtil.getMoney(minecraft.player)));
                })
        );

        leftBottom.addChildren(leftBottomTop, leftBottomBottom);

        left.addChildren(leftTop, leftBottom);
        //中
        UIElement center = new UIElement().layout(layout -> {
            layout.widthPercent(55);
            layout.heightPercent(100);
            layout.gapAll(theme.centerPanelGap());
            layout.flexDirection(FlexDirection.COLUMN);
        });
        UIElement head = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(10);
            layout.paddingTop(2);
            layout.flexDirection(FlexDirection.ROW);
            layout.justifyContent(AlignContent.SPACE_BETWEEN);
            layout.alignItems(AlignItems.CENTER);
        }).style(style -> style.backgroundTexture(theme.topBar()));
        //搜索图片
        UIElement searchIcon = new UIElement().setId("shop_search_icon").layout(layout -> {
            layout.marginLeft(5);
            // 保持方形布局，并在布局盒内缩小贴图，避免搜索框位置随图标尺寸变化。
            layout.width(18);
            layout.setAspectRatio(1f);
            layout.flexShrink(0);
        }).style(style -> style.backgroundTexture(
                GuiTextureGroup.of(
                        theme.searchIconBackground(),
                        SpriteTexture.of(ViscriptShop.formattedMod("textures/gui/search_icon.png")).scale(0.8f)
                )
        ));
        //物品输入框
        searchComponent = UIElementUtil.createItemStackSearchComponentConfigurator("", this::getSearchItem, search -> {
            this.searchItem = search;
            reloadMerchants();
        }, getCategoryItems()).searchComponent;
        searchComponent.layout(layout -> {
            layout.width(70);
            layout.heightPercent(85);
            layout.paddingLeft(4);
        });
        IGuiTexture SEARCH_BAR = theme.searchField();
        searchComponent.getStyle().backgroundTexture(SEARCH_BAR);
        searchComponent.searchStyle(style -> style.focusOverlay(IGuiTexture.EMPTY));
        //序号输入框
        StringConfigurator idInput = (StringConfigurator) new StringConfigurator("", this::getSearchId, search -> {
            if (search.chars().allMatch(Character::isDigit)) {
                this.searchId = search;
                reloadMerchants();
            }
        }, searchId, true)
                .layout(layout -> layout.width(70).heightPercent(85).justifyContent(AlignContent.CENTER).paddingLeft(4))
                .setDisplay(TaffyDisplay.NONE);
        idInput.getStyle().backgroundTexture(SEARCH_BAR);
        idInput.textField.getStyle().backgroundTexture(IGuiTexture.EMPTY);
        idInput.textField.textFieldStyle(textStyle -> {
            textStyle.placeholder(Component.empty());
            textStyle.focusOverlay(IGuiTexture.EMPTY);
        });
        Toggle toggle = (Toggle) new SceneToggleBuilder(this::isSearchMode, this::setSearchMode)
                .icon(new ItemStackTexture(Items.GRASS_BLOCK), SpriteTexture.of(ViscriptShop.formattedMod("textures/id.png")))
                .build()
                .setOnToggleChanged(isOn -> {
                    reloadMerchants();
                    searchComponent.setDisplay(isOn ? TaffyDisplay.FLEX : TaffyDisplay.NONE);
                    idInput.setDisplay(isOn ? TaffyDisplay.NONE : TaffyDisplay.FLEX);
                })
                .addEventListener(UIEvents.TICK, event -> {
                    event.target.getStyle().tooltips(Component.translatable(searchMode ? "viscript_shop.ui.searchMode.item" : "viscript_shop.ui.searchMode.id"));
                })
                .layout(layout -> {
                    layout.width(16);
                    layout.height(16);
                    layout.marginHorizontal(2);
                });

        SpriteTexture GRID = SpriteTexture.of(ViscriptShop.formattedMod("textures/grid.png"));
        SpriteTexture LIST = SpriteTexture.of(ViscriptShop.formattedMod("textures/list.png"));
        Toggle layoutToggle = (Toggle) new SceneToggleBuilder(this::isCurrencyGridLayout, this::setCurrencyGridLayout)
                .icon(GRID, LIST)
                .build()
                .setOnToggleChanged(isOn -> {
                    setCurrencyGridLayout(isOn);
                    reloadMerchants();
                })
                .layout(layout -> {
                    layout.width(16);
                    layout.height(16);

                });
        this.currencyLayoutToggle = layoutToggle;
        updateCurrencyLayoutToggleState();

        head.addChildren(new UIElement().layout(layout -> {
            layout.heightPercent(100);
            layout.alignItems(AlignItems.CENTER);
            layout.flexDirection(FlexDirection.ROW);
        }).addChildren(searchIcon, searchComponent, idInput, toggle, layoutToggle), new UIElement().layout(layout -> {
            layout.heightPercent(100);
            layout.alignItems(AlignItems.CENTER);
            layout.flexDirection(FlexDirection.ROW);
        }).addChildren(new PlayerHeadElement().layout(layout -> layout.marginRight(5))));

        UIElement body = new UIElement().setId("shop_ui_merchants").layout(layout -> {
            layout.widthPercent(100);
            layout.justifyContent(AlignContent.CENTER);
            layout.alignItems(AlignItems.CENTER);
            layout.paddingAll(3);
            layout.paddingBottom(5);
            layout.flex(1);
        }).style(style -> style.backgroundTexture(theme.merchantPanel()));

        merchantsView.layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
        });

        merchantsView.viewPort.getStyle().backgroundTexture(IGuiTexture.EMPTY);

        merchantsView.viewContainer.layout(layout -> {
            layout.flexDirection(FlexDirection.COLUMN);
            layout.gapAll(5);
        });
        merchantsView.viewPort.addEventListener(UIEvents.LAYOUT_CHANGED, event -> updateCurrencyGridColumns());

        reloadMerchants();

        body.addChildren(merchantsView);

        center.addChildren(head, body);
        //右
        UIElement right = new UIElement().setId("shop_ui_summary").layout(layout -> {
            layout.widthPercent(25);
            layout.heightPercent(100);
            layout.gapAll(3);
            layout.flexDirection(FlexDirection.COLUMN);
        });
        UIElement rightTop = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(10);
        }).style(style -> {
            style.backgroundTexture(theme.titleHeader());
        }).addChild(new Label().setText(title)
                .textStyle(textStyle -> {
                    textStyle.textAlignHorizontal(Horizontal.CENTER).textAlignVertical(Vertical.CENTER);
                })
                .layout(layout -> {
                    layout.widthPercent(100);
                    layout.heightPercent(100);
                }));

        UIElement rightBottom = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.flexDirection(FlexDirection.COLUMN);
            layout.paddingAll(5);
        });
        rightBottom.getStyle().backgroundTexture(theme.summaryPanel());

        UIElement shoppingCar = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(39);
        }).style(style -> style.backgroundTexture(theme.shoppingCartPanel()));

        shoppingCarView.layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(85);
        });
        shoppingCarView.viewContainer.layout(layout -> {
            layout.paddingLeft(6);
            layout.flexDirection(FlexDirection.ROW);
            layout.wrap(FlexWrap.WRAP);
        });
        shoppingCarView.viewPort.getLayout().paddingAll(3);
        shoppingCarView.viewPort.getStyle().backgroundTexture(IGuiTexture.EMPTY);
        reloadShoppingItem();

        shoppingCar.addChildren(shoppingCarView);

        inventoryView.layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(35);
        });
        inventoryView.viewContainer.layout(layout -> {
            layout.paddingLeft(6);
            layout.flexDirection(FlexDirection.ROW);
            layout.wrap(FlexWrap.WRAP);
        });
        inventoryView.viewPort.getLayout().paddingAll(3);
        inventoryView.viewPort.setId("shop_consumption_panel");
        inventoryView.viewPort.getStyle().backgroundTexture(theme.consumptionPanel());
        reloadInventoryItem();

        ShopButton clearButton = (ShopButton) ShopButton.buying(theme).setText("viscript_shop.button.clear").setOnClick(event -> {
            currentShopInfo.getCategoryInfos().forEach(categoryInfo -> {
                categoryInfo.getMerchants().forEach(merchantInfo -> merchantInfo.setBuyCount(0));
            });
            reloadShoppingItem();
            reloadInventoryItem();
        }).layout(layout -> {
            layout.widthPercent(45);
        });

        ShopButton tsButton = (ShopButton) ShopButton.buying(theme).setText("viscript_shop.button.ts").setOnClick(event -> {
            ShopHelper.cacheShopInfo = this.currentShopInfo;
            if (minecraft.screen != null) minecraft.screen.onClose();
        }).layout(layout -> {
            layout.widthPercent(45);
        });

        ShopButton buyButton = (ShopButton) ShopButton.buying(theme).setText("viscript_shop.button.buy").setOnClick(event -> {
            AggregatedResources costSummary = AggregatedResources.getCostSummary(this.currentShopInfo);
            AggregatedResources gainSummary = AggregatedResources.getGainSummary(this.currentShopInfo);
            if (costSummary.isEmpty() || gainSummary.isEmpty()) {
                Message.warn("viscript_shop.message.shoppingCar.empty", this);
                return;
            }
            int maxShopUiGiveItemsPerPurchase = Config.maxShopUiGiveItemsPerPurchase.get();
            if (maxShopUiGiveItemsPerPurchase >= 0 && gainSummary.getTotalItemCount() > maxShopUiGiveItemsPerPurchase) {
                Message.error(Component.translatable("viscript_shop.message.buy.too_many_items", maxShopUiGiveItemsPerPurchase).getString(), this);
                return;
            }
            RPCPacketDistributor.rpcToServer(BuyMerchantPayload.BUY_MERCHANT, this.shopLocation, costSummary, gainSummary);
        }).layout(layout -> {
            layout.widthPercent(100);
        });

        rightBottom.addChildren(new Label().setText("viscript_shop.ui.shoppingCar").textStyle(textStyle -> textStyle.adaptiveHeight(true)).layout(layout -> layout.marginLeft(3)), shoppingCar,
                new Label().setText("viscript_shop.ui.inventory").textStyle(textStyle -> textStyle.adaptiveHeight(true)).layout(layout -> layout.marginLeft(3)), inventoryView,
                new UIElement().layout(layout -> {
                    layout.marginTop(5);
                    layout.marginBottom(2);
                    layout.flexDirection(FlexDirection.ROW);
                    layout.justifyContent(AlignContent.SPACE_BETWEEN);
                }).addChildren(tsButton, clearButton), buyButton);

        right.addChildren(rightTop, rightBottom);

        root.addChildren(left, center, right);
    }

    private ShopInfo initCurrentShopInfo(ShopInfo shopInfo) {
        if (ShopHelper.cacheShopInfo == null) {
            return shopInfo;
        }
        copyCachedBuyCounts(ShopHelper.cacheShopInfo, shopInfo);
        return shopInfo;
    }

    private void copyCachedBuyCounts(ShopInfo cachedShopInfo, ShopInfo freshShopInfo) {
        for (CategoryInfo freshCategory : freshShopInfo.getCategoryInfos()) {
            CategoryInfo cachedCategory = cachedShopInfo.getCategoryInfos().stream()
                    .filter(category -> category.getId().equals(freshCategory.getId()))
                    .findFirst()
                    .orElse(null);
            if (cachedCategory == null) continue;

            for (MerchantInfo freshMerchant : freshCategory.getMerchants()) {
                cachedCategory.getMerchants().stream()
                        .filter(merchant -> merchant.getId().equals(freshMerchant.getId()))
                        .findFirst()
                        .ifPresent(cachedMerchant -> {
                            int buyCount = cachedMerchant.getBuyCount().intValue();
                            int stock = freshMerchant.getStock();
                            freshMerchant.setBuyCount(stock >= 0 ? Math.min(buyCount, stock) : buyCount);
                        });
            }
        }
    }

    @Override
    public void initScreen(int screenWidth, int screenHeight) {
        super.initScreen(screenWidth, screenHeight);
        applyAutoGuiScaleTransform();
    }

    public static Size getAutoGuiScaledSize(Size screenSize) {
        float scale = getAutoGuiScaleFactor();
        if (scale <= 0f) return screenSize;

        return Size.of(
                Math.max(1, Math.round(screenSize.getWidth() / scale)),
                Math.max(1, Math.round(screenSize.getHeight() / scale))
        );
    }

    private void applyAutoGuiScaleTransform() {
        float scale = getAutoGuiScaleFactor();
        // 让固定尺寸控件在任意 GUI Scale 下都保持 Auto 缩放时的视觉大小。
        transform(transform -> transform.pivot(0.5f, 0.5f).scale(scale));
    }

    private static float getAutoGuiScaleFactor() {
        Minecraft minecraft = Minecraft.getInstance();

        var window = minecraft.getWindow();
        double currentScale = window.getGuiScale();
        if (currentScale <= 0d) return 1f;

        int autoScale = window.calculateScale(0, minecraft.isEnforceUnicode());
        return Math.max(1f, (float) (autoScale / currentScale));
    }

    public void reloadCategoryList() {
        categoryView.clearAllScrollViewChildren();

        for (int i = 0; i < currentShopInfo.getCategoryInfos().size(); i++) {
            CategoryInfo categoryInfo = currentShopInfo.getCategoryInfos().get(i);
            UIElement categoryUI = UIElementUtil.createCategoryUI(
                    categoryInfo,
                    categoryInfo.equals(this.selectedCategory),
                    value -> {
                        setSelectedCategory(value);
                        if (minecraft.player != null) {
                            RPCPacketDistributor.rpcToServer(GetItemCountC2SPayload.GET_ITEM_COUNT, selectedCategory);
                        }
                        reloadMerchants();
                    },
                    theme.categoryDefault(),
                    theme.categorySelected()
            );
            categoryView.viewContainer.addChildren(categoryUI);
        }
    }

    public void reloadMerchants() {
        merchantsView.clearAllScrollViewChildren();
        updateCurrencyLayoutToggleState();
        configureMerchantsContainerLayout();

        // 重新添加所有商品
        for (int i = 0; i < selectedCategory.getMerchants().size(); i++) {
            MerchantInfo merchantInfo = selectedCategory.getMerchants().get(i);
            //商品上锁样式：隐藏
            if (currentShopInfo.getLockedMerchantVisibility().equals(ShopInfo.LockedMerchantVisibility.HIDDEN) && isMerchantLocked(merchantInfo)) {
                continue;
            }
            //搜索筛选 物品筛选和序号筛选
            if (this.searchMode) {
                if (!this.searchItem.isEmpty()) {
                    boolean isMatch = ItemStack.isSameItemSameTags(merchantInfo.getItemResult(), this.searchItem) ||
                            merchantInfo.getItemAMatchRule().matches(merchantInfo.getItemA(), this.searchItem) ||
                            merchantInfo.getItemBMatchRule().matches(merchantInfo.getItemB(), this.searchItem);
                    if (!isMatch) {
                        continue;
                    }
                }
            } else {
                if (!this.searchId.isEmpty()) {
                    try {
                        int targetIndex = Integer.parseInt(this.searchId);
                        if ((i + 1) != targetIndex) {
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
            if (isCurrencyGridActive()) {
                merchantsView.addScrollViewChild(createCurrencyMerchantGrid(merchantInfo, i));
            } else {
                merchantsView.addScrollViewChild(createMerchant(merchantInfo, i));
            }
        }
    }

    private boolean isCurrencyGridActive() {
        return selectedCategory != null
                && selectedCategory.getShopType() == CategoryInfo.ShopType.CURRENCY
                && currencyGridLayout;
    }

    private void updateCurrencyLayoutToggleState() {
        if (currencyLayoutToggle == null) return;

        boolean show = selectedCategory != null && selectedCategory.getShopType() == CategoryInfo.ShopType.CURRENCY;
        currencyLayoutToggle.setDisplay(show ? TaffyDisplay.FLEX : TaffyDisplay.NONE);
        currencyLayoutToggle.getStyle().tooltips(Component.translatable(currencyGridLayout ? "viscript_shop.ui.layout.grid" : "viscript_shop.ui.layout.list"));
        currencyLayoutToggle.setValue(currencyGridLayout, false);
    }

    private void configureMerchantsContainerLayout() {
        if (isCurrencyGridActive()) {
            merchantsView.viewContainer.layout(layout -> {
                layout.display(TaffyDisplay.GRID);
                layout.gridAutoFlow(GridAutoFlow.ROW);
                layout.justifyItems(AlignItems.CENTER);
                layout.alignItems(AlignItems.FLEX_START);
                layout.justifyContent(AlignContent.CENTER);
                layout.alignContent(AlignContent.FLEX_START);
                layout.gapAll(CURRENCY_GRID_GAP);
            });
            updateCurrencyGridColumns();
        } else {
            merchantsView.viewContainer.layout(layout -> {
                layout.display(TaffyDisplay.FLEX);
                layout.flexDirection(FlexDirection.COLUMN);
                layout.wrap(FlexWrap.NO_WRAP);
                layout.gapAll(5);
            });
            currencyGridColumns = -1;
        }
    }

    private void updateCurrencyGridColumns() {
        if (!isCurrencyGridActive()) return;
        if (merchantsView == null || merchantsView.viewPort == null) return;

        float available = merchantsView.viewPort.getContentWidth();
        if (available <= 1f) return;

        int cols = Math.max(1, (int) Math.floor((available + CURRENCY_GRID_GAP) / (CURRENCY_GRID_CARD_WIDTH + CURRENCY_GRID_GAP)));
        while (cols > 1) {
            float required = cols * CURRENCY_GRID_CARD_WIDTH + (cols - 1) * CURRENCY_GRID_GAP;
            if (required <= available + 0.01f) break;
            cols--;
        }
        if (cols == currencyGridColumns) return;
        currencyGridColumns = cols;

        List<TrackSizingFunction> tracks = new ArrayList<>(cols);
        for (int i = 0; i < cols; i++) {
            tracks.add(TrackSizingFunction.fixed(CURRENCY_GRID_CARD_WIDTH));
        }
        merchantsView.viewContainer.getLayout().gridTemplateColumns(new GridTemplate(tracks, List.of(), List.of()));
        merchantsView.viewContainer.markTaffyStyleDirty();
    }

    public void reloadShoppingItem() {
        shoppingCarView.clearAllScrollViewChildren();

        AggregatedResources gainSummary = AggregatedResources.getGainSummary(currentShopInfo);
        gainSummary.getItems().forEach((itemStack, count) -> {
            Label countLabel = (Label) new Label().setText(CountTextUtil.formatCount(count))
                    .textStyle(textStyle -> {
                        textStyle.textAlignHorizontal(Horizontal.CENTER).textAlignVertical(Vertical.BOTTOM);
                        textStyle.fontSize(5);
                    })
                    .layout(layout -> {
                        layout.width(10);
                        layout.heightPercent(100);
                    })
                    .addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                        event.hoverTooltips = new HoverTooltips(List.of(Component.nullToEmpty(String.valueOf(count))), null, null, null);
                    });
            shoppingCarView.addScrollViewChild(createItemInfoBox().addChildren(UIElementUtil.createItemSlot(itemStack, false, true), countLabel));
        });
        if (gainSummary.getTotalMoney() > 0) {
            UIElement moneyIcon = new UIElement().layout(layout -> {
                layout.width(16);
                layout.height(16);
                layout.marginLeft(2);
            }).style(style -> style.backgroundTexture(COIN));
            Label money = (Label) new Label().setText(CountTextUtil.formatCount(gainSummary.getTotalMoney())).textStyle(textStyle -> {
                textStyle.textAlignVertical(Vertical.BOTTOM).adaptiveWidth(true);
                textStyle.fontSize(5);
            }).layout(layout -> {
                layout.heightPercent(100);
            }).addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                event.hoverTooltips = new HoverTooltips(List.of(Component.nullToEmpty(String.valueOf(gainSummary.getTotalMoney()))), null, null, null);
            });
            shoppingCarView.addScrollViewChild(createItemInfoBox().addChildren(moneyIcon, money));
        }
    }

    public void reloadInventoryItem() {
        inventoryView.clearAllScrollViewChildren();
        AggregatedResources costSummary = AggregatedResources.getCostSummary(currentShopInfo);
        costSummary.getItemEntries().forEach(itemEntry -> {
            ItemStack itemStack = itemEntry.getItemStack();
            int count = itemEntry.getCount();
            int itemCount = getItemCount(itemEntry);
            String color = itemCount >= count ? "§a" : "§c";
            Label countLabel = (Label) new Label().setText(color + CountTextUtil.formatCount(count) + "§f/" + CountTextUtil.formatCount(itemCount))
                    .textStyle(textStyle -> {
                        textStyle.textAlignHorizontal(Horizontal.CENTER).textAlignVertical(Vertical.BOTTOM);
                        textStyle.fontSize(4);
                    })
                    .layout(layout -> {
                        layout.width(10);
                        layout.heightPercent(100);
                    })
                    .addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                        event.hoverTooltips = new HoverTooltips(List.of(Component.nullToEmpty(color + count + "§f/" + itemCount)), null, null, null);
                    });
            inventoryView.addScrollViewChild(createItemInfoBox().addChildren(UIElementUtil.createItemSlot(itemStack, false, true), countLabel));
        });
        if (costSummary.getTotalMoney() > 0 && minecraft.player != null) {
            String color = costSummary.getTotalMoney() <= ViScriptShopClientUtil.getMoney(minecraft.player) ? "§a" : "§c";
            UIElement moneyIcon = new UIElement().layout(layout -> {
                layout.width(16);
                layout.height(16);
                layout.marginLeft(2);
            }).style(style -> style.backgroundTexture(COIN));
            Label money = (Label) new Label().setText(color + CountTextUtil.formatCount(costSummary.getTotalMoney())).textStyle(textStyle -> {
                textStyle.textAlignVertical(Vertical.BOTTOM).adaptiveWidth(true);
                textStyle.fontSize(5);
            }).layout(layout -> {
                layout.heightPercent(100);
            }).addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                event.hoverTooltips = new HoverTooltips(List.of(Component.nullToEmpty(color + costSummary.getTotalMoney())), null, null, null);
            });
            inventoryView.addScrollViewChild(createItemInfoBox().addChildren(moneyIcon, money));
        }

    }

    public void reloadSearchComponent() {
        Set<ItemStack> items = getCategoryItems();
        searchComponent.setSearchUI(new SearchComponent.ISearchUI<>() {
            @Override
            public @NotNull String resultText(@NotNull ItemStack value) {
                return value.isEmpty() ? "" : value.getHoverName().getString();
            }

            @Override
            public void onResultSelected(@Nullable ItemStack value) {
                searchItem = value;
                reloadMerchants();
            }

            @Override
            public void search(String word, IResultHandler<ItemStack> handler) {
                Collection<ItemStack> candidatesItems = items;

                if (candidatesItems == null) {
                    candidatesItems = BuiltInRegistries.ITEM.stream()
                            .map(ItemStack::new)
                            .toList();
                }

                for (ItemStack stack : candidatesItems) {
                    if (Thread.currentThread().isInterrupted()) return;

                    if (stack.isEmpty()) {
                        handler.acceptResult(stack);
                        continue;
                    }

                    if (SimpleItemStackFilter.matchItemSearch(stack, word)) {
                        handler.acceptResult(stack);
                    }
                }
            }
        });
    }

    public UIElement createMerchant(MerchantInfo merchantInfo, int index) {
        UIElement merchant = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.height(20);
            layout.gapAll(6);
            layout.flexDirection(FlexDirection.ROW);
            layout.paddingHorizontal(4);
            layout.alignItems(AlignItems.CENTER);
        });
        merchant.getStyle().backgroundTexture(LIST_BACKGROUND);
        Label id = (Label) new Label().setText(String.valueOf(index + 1)).textStyle(textStyle -> {
            textStyle.textAlignHorizontal(Horizontal.LEFT).textAlignVertical(Vertical.CENTER);
            textStyle.fontSize(6);
        }).layout(layout -> {
            layout.width(20);
            layout.heightPercent(100);
        });

        UIElement uiElement = new UIElement().layout(layout -> {
            layout.widthPercent(20);
            layout.heightPercent(100);
            layout.gapAll(5);
            layout.flexDirection(FlexDirection.ROW);
            layout.alignItems(AlignItems.CENTER);
        });
        UIElement rightArrowIcon = new UIElement().style(style -> style.backgroundTexture(RIGHT_ARROW)).layout(layout -> {
            layout.width(12);
            layout.height(12);
        });
        UIElement resultItemSlot = UIElementUtil.createMerchantItemDisplay(
                merchantInfo.getItemResultInfo(),
                true
        ).setId("itemResult" + index);
        resultItemSlot.getLayout().marginRight(2);

        merchant.addChildren(id);

        switch (selectedCategory.getShopType()) {
            case ITEM_FOR_ITEM -> {
                UIElement itemASlot = UIElementUtil.createMerchantItemDisplay(
                        merchantInfo.getItemAInfo(),
                        true
                ).setId("itemA" + index);
                UIElement itemBSlot = UIElementUtil.createMerchantItemDisplay(
                        merchantInfo.getItemBInfo(),
                        true
                ).setId("itemB" + index);
                uiElement.addChildren(itemASlot, itemBSlot);
                merchant.addChildren(uiElement, rightArrowIcon, resultItemSlot);
            }
            case CURRENCY -> {
                Label money = (Label) new Label().setText("◎" + CountTextUtil.formatCount(merchantInfo.getMoney())).textStyle(textStyle -> {
                    textStyle.textAlignHorizontal(Horizontal.CENTER).textAlignVertical(Vertical.CENTER).adaptiveWidth(true);
                    textStyle.fontSize(8);
                }).layout(layout -> {
                    layout.heightPercent(100);
                }).addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                    event.hoverTooltips = new HoverTooltips(List.of(Component.nullToEmpty(String.valueOf(merchantInfo.getMoney()))), null, null, null);
                });
                uiElement.getLayout().justifyContent(AlignContent.SPACE_BETWEEN);
                uiElement.getLayout().widthPercent(45);
                UIElement moneyUI = new UIElement().layout(layout -> {
                    layout.widthPercent(40);
                    layout.heightPercent(100);
                    layout.justifyContent(AlignContent.CENTER);
                    layout.alignItems(AlignItems.CENTER);
                }).addChild(money);

                UIElement itemUI = new UIElement().layout(layout -> {
                    layout.widthPercent(40);
                    layout.heightPercent(100);
                    layout.justifyContent(AlignContent.CENTER);
                    layout.alignItems(AlignItems.CENTER);
                }).addChild(resultItemSlot);
                switch (merchantInfo.getTradeType()) {
                    case BUY -> uiElement.addChildren(moneyUI, rightArrowIcon, itemUI);
                    case SELL -> uiElement.addChildren(itemUI, rightArrowIcon, moneyUI);
                }
                merchant.addChildren(uiElement);
            }
        }
        final Button[] buttonHolder = new Button[2];

        buttonHolder[0] = ShopButton.other(theme).setText("-").setOnClick(event -> {
            if ((int) merchantInfo.getBuyCount() > 0) {
                merchantInfo.setBuyCount((int) merchantInfo.getBuyCount() - 1);
                reloadShoppingItem();
                reloadInventoryItem();
                updateStockButtons(merchantInfo, buttonHolder[0], buttonHolder[1]);
            }
        });

        buttonHolder[1] = ShopButton.other(theme).setText("+").setOnClick(event -> {
            int stock = merchantInfo.getStock();
            int maxCount = stock >= 0 ? stock : Integer.MAX_VALUE;
            if ((int) merchantInfo.getBuyCount() < maxCount) {
                merchantInfo.setBuyCount((int) merchantInfo.getBuyCount() + 1);
                reloadShoppingItem();
                reloadInventoryItem();
                updateStockButtons(merchantInfo, buttonHolder[0], buttonHolder[1]);
            }
        });

        NumberConfigurator countConfigurator = new NumberConfigurator("", merchantInfo::getBuyCount, count -> {
            merchantInfo.setBuyCount(count);
            reloadShoppingItem();
            reloadInventoryItem();
            updateStockButtons(merchantInfo, buttonHolder[0], buttonHolder[1]);
        }, 0, true);
        countConfigurator.layout(layout -> {
            switch (selectedCategory.getShopType()) {
                case ITEM_FOR_ITEM -> {
                    layout.width(35);
                }
                case CURRENCY -> {
                    layout.width(30);
                }
            }
        });
        countConfigurator.inlineContainer.getStyle().backgroundTexture(LIST_BACKGROUND);

        // 应用库存限制
        applyStockRestrictions(merchantInfo, countConfigurator, buttonHolder[0], buttonHolder[1]);

        if (isMerchantLocked(merchantInfo)) {
            countConfigurator.textField.setWheelDur(0);
            countConfigurator.textField.setActive(false);
            buttonHolder[0].setActive(false);
            buttonHolder[1].setActive(false);
        }

        // 添加库存悬浮提示或遮罩
        int stock = merchantInfo.getStock();
        if (stock > 0) {
            // 库存 > 0：添加悬浮提示显示库存
            addStockTooltip(merchant, stock);
        } else if (stock == 0) {
            // 库存 = 0：添加半透明遮罩
            merchant.addChildren(createStockOverlay());
        }
        UIElement LockIcon = new UIElement().style(style -> style.backgroundTexture(LOCK)).layout(layout -> {
            layout.width(16);
            layout.height(16);
        }).addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            List<Component> lockReasons = getMerchantLockReasons(merchantInfo);
            if (!lockReasons.isEmpty()) {
                event.hoverTooltips = new HoverTooltips(lockReasons, null, null, null);
            }
        });

        merchant.addChildren(new UIElement().layout(layout -> {
            layout.gapAll(1);
            layout.flexDirection(FlexDirection.ROW);
            layout.alignItems(AlignItems.CENTER);
            layout.heightPercent(100);
        }).addChildren(buttonHolder[0], countConfigurator, buttonHolder[1]));

        if (isMerchantLocked(merchantInfo)) merchant.addChildren(LockIcon);

        return merchant;
    }

    public UIElement createCurrencyMerchantGrid(MerchantInfo merchantInfo, int index) {
        UIElement merchant = new UIElement().layout(layout -> {
            layout.width(CURRENCY_GRID_CARD_WIDTH);
            layout.flexDirection(FlexDirection.COLUMN);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.FLEX_START);
            layout.paddingAll(5);
            layout.gapAll(2);
            layout.positionType(TaffyPosition.RELATIVE);
        });
        merchant.getStyle().backgroundTexture(GRID_BACKGROUND);

        Label id = (Label) new Label().setText(String.valueOf(index + 1)).textStyle(textStyle -> {
            textStyle.textAlignHorizontal(Horizontal.LEFT).textAlignVertical(Vertical.CENTER);
            textStyle.fontSize(8);
        }).layout(layout -> {
            layout.widthPercent(100);
            layout.height(6);
            layout.alignSelf(AlignItems.FLEX_START);
        });

        UIElement resultItemSlot = UIElementUtil.createMerchantItemDisplay(
                        merchantInfo.getItemResultInfo(),
                        true
                )
                .setId("itemResult" + index)
                .layout(layout -> {
                    layout.width(20);
                    layout.height(20);
                });

        String tradeText = merchantInfo.getTradeType().getSerializedName();
        Label tradeLabel = (Label) new Label()
                .setText(Component.translatable(tradeText))
                .textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER).fontSize(6))
                .layout(layout -> layout.widthPercent(100));

        Label priceLabel = (Label) new Label()
                .setText(Component.literal("◎" + CountTextUtil.formatCount(merchantInfo.getMoney())))
                .textStyle(textStyle -> textStyle
                        .textColor(0xFFFFAA00)
                        .textAlignHorizontal(Horizontal.CENTER)
                        .textAlignVertical(Vertical.CENTER)
                        .fontSize(8)
                )
                .layout(layout -> {
                    layout.widthPercent(100);
                    layout.marginTop(1);
                    layout.marginBottom(2);
                })
                .addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                    event.hoverTooltips = new HoverTooltips(List.of(Component.nullToEmpty(String.valueOf(merchantInfo.getMoney()))), null, null, null);
                });

        NumberConfigurator countConfigurator = new NumberConfigurator("", merchantInfo::getBuyCount, count -> {
            merchantInfo.setBuyCount(count);
            reloadShoppingItem();
            reloadInventoryItem();
        }, 0, true);
        countConfigurator.layout(layout -> layout.width(28));
        countConfigurator.inlineContainer.getStyle().backgroundTexture(GRID_BACKGROUND);

        // 应用库存限制
        int stock = merchantInfo.getStock();
        if (stock >= 0) {
            // 有限库存
            countConfigurator.setRange(0, stock);
            if (stock == 0) {
                countConfigurator.textField.setWheelDur(0);
                countConfigurator.textField.setActive(false);
            }
        } else {
            // 无限库存
            countConfigurator.setRange(0, Integer.MAX_VALUE);
        }

        if (isMerchantLocked(merchantInfo)) {
            countConfigurator.textField.setWheelDur(0);
            countConfigurator.textField.setActive(false);
        }

        // 添加库存悬浮提示或遮罩
        if (stock > 0) {
            // 库存 > 0：添加悬浮提示显示库存
            addStockTooltip(merchant, stock);
        } else if (stock == 0) {
            // 库存 = 0：添加半透明遮罩
            merchant.addChildren(createStockOverlay());
        }

        UIElement lockIcon = new UIElement().style(style -> style.backgroundTexture(LOCK)).layout(layout -> {
            layout.width(12);
            layout.height(12);
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.top(2);
            layout.right(2);
        }).addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            List<Component> lockReasons = getMerchantLockReasons(merchantInfo);
            if (!lockReasons.isEmpty()) {
                event.hoverTooltips = new HoverTooltips(lockReasons, null, null, null);
            }
        });
        lockIcon.setDisplay(isMerchantLocked(merchantInfo) ? TaffyDisplay.FLEX : TaffyDisplay.NONE);

        UIElement body = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.flexDirection(FlexDirection.COLUMN);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
            layout.gapAll(3);
        }).addChildren(resultItemSlot, tradeLabel, priceLabel);

        UIElement controls = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.flexDirection(FlexDirection.ROW);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        });

        UIElement qty = new UIElement().layout(layout -> {
            layout.gapAll(2);
            layout.flexDirection(FlexDirection.ROW);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(countConfigurator);

        controls.addChildren(qty);

        merchant.addChildren(id, lockIcon, body, controls);
        return merchant;
    }

    /**
     * 判断商品是否解锁
     *
     * @param merchantInfo 商品信息
     * @return null表示已解锁，非null返回锁定原因的Component
     */
    private boolean isMerchantLocked(MerchantInfo merchantInfo) {
        return !getMerchantLockReasons(merchantInfo).isEmpty();
    }

    private List<Component> getMerchantLockReasons(MerchantInfo merchantInfo) {
        if (minecraft.player == null) {
            return List.of();
        }

        return MerchantFlagGroup.getLockTooltips(merchantInfo.getFlagGroupMode(), merchantInfo.getFlagGroups(), ViScriptShopClientUtil.getStageFlags(minecraft.player));
    }

    /**
     * 创建库存遮罩层（当库存为0时显示）
     */
    private UIElement createStockOverlay() {
        UIElement overlay = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.top(0);
            layout.left(0);
        });
        overlay.getStyle().backgroundTexture(new ColorRectTexture(0x80000000)); // 半透明黑色
        overlay.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            event.hoverTooltips = new HoverTooltips(
                    List.of(Component.translatable("viscript_shop.message.stock.out").withStyle(ChatFormatting.RED)),
                    null, null, null
            );
        });
        return overlay;
    }

    /**
     * 创建库存悬浮提示（当库存>0时显示）
     */
    private void addStockTooltip(UIElement element, int stock) {
        element.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            event.hoverTooltips = new HoverTooltips(
                    List.of(Component.translatable("viscript_shop.message.stock.available", stock).withStyle(ChatFormatting.YELLOW)),
                    null, null, null
            );
        });
    }

    /**
     * 应用库存限制到输入框和按钮
     */
    private void applyStockRestrictions(MerchantInfo merchantInfo, NumberConfigurator countConfigurator, Button removeButton, Button addButton) {
        int stock = merchantInfo.getStock();

        // 库存 < 0：无限库存，不限制
        if (stock < 0) {
            countConfigurator.setRange(0, Integer.MAX_VALUE);
            return;
        }

        // 库存 = 0：禁用所有控件
        if (stock == 0) {
            countConfigurator.setRange(0, 0);
            countConfigurator.textField.setWheelDur(0);
            countConfigurator.textField.setActive(false);
            if (removeButton != null) removeButton.setActive(false);
            if (addButton != null) addButton.setActive(false);
            return;
        }

        // 库存 > 0：设置范围并控制按钮状态
        countConfigurator.setRange(0, stock);

        // 根据当前购买数量更新按钮状态
        updateStockButtons(merchantInfo, removeButton, addButton);
    }

    /**
     * 更新按钮状态（根据库存和当前购买数量）
     */
    private void updateStockButtons(MerchantInfo merchantInfo, Button removeButton, Button addButton) {
        int stock = merchantInfo.getStock();
        int currentCount = (int) merchantInfo.getBuyCount();

        if (stock < 0) {
            // 无限库存，按钮始终可用（除非其他锁定原因）
            if (removeButton != null) removeButton.setActive(true);
            if (addButton != null) addButton.setActive(true);
            return;
        }

        if (removeButton != null) {
            removeButton.setActive(currentCount > 0);
        }

        if (addButton != null) {
            addButton.setActive(currentCount < stock);
        }
    }

    public void setItemCount(AggregatedResources.ItemEntry itemEntry) {
        AggregatedResources.ItemEntry copy = itemEntry.copyWithCount(itemEntry.getCount());
        for (int i = 0; i < this.playerItems.size(); i++) {
            AggregatedResources.ItemEntry existing = this.playerItems.get(i);
            if (existing.canMerge(copy.getItemStack(), copy.getMatchRule())) {
                this.playerItems.set(i, copy);
                return;
            }
        }
        this.playerItems.add(copy);
    }

    public int getItemCount(AggregatedResources.ItemEntry itemEntry) {
        for (AggregatedResources.ItemEntry item : this.playerItems) {
            if (item.canMerge(itemEntry.getItemStack(), itemEntry.getMatchRule())) {
                return item.getCount();
            }
        }
        return 0;
    }

    public void removeItemCount(AggregatedResources.ItemEntry itemEntry) {
        for (AggregatedResources.ItemEntry item : this.playerItems) {
            if (item.canMerge(itemEntry.getItemStack(), itemEntry.getMatchRule())) {
                item.setCount(item.getCount() - itemEntry.getCount());
                return;
            }
        }
    }

    private UIElement createItemInfoBox() {
        return new UIElement().layout(layout -> {
            layout.widthPercent(50);
            layout.height(20);
            layout.justifyContent(AlignContent.FLEX_START);
            layout.alignItems(AlignItems.CENTER);
            layout.flexDirection(FlexDirection.ROW);
        });
    }

    public Set<ItemStack> getCategoryItems() {
        Set<ItemStack> items = new HashSet<>();
        items.add(ItemStack.EMPTY);
        List<MerchantInfo> merchants = selectedCategory.getMerchants();

        for (MerchantInfo merchant : merchants) {
            if (!isMerchantLocked(merchant)) {
                if (selectedCategory.getShopType() == CategoryInfo.ShopType.ITEM_FOR_ITEM) {
                    addItemStackIfUnique(items, merchant.getItemA());
                    addItemStackIfUnique(items, merchant.getItemB());
                }
                addItemStackIfUnique(items, merchant.getItemResult());
            }
        }
        return items;
    }

    private void addItemStackIfUnique(Set<ItemStack> list, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        for (ItemStack existing : list) {
            if (ItemStack.isSameItemSameTags(existing, stack)) {
                return;
            }
        }
        ItemStack displayStack = stack.copy();
        displayStack.setCount(1);

        list.add(displayStack);
    }
}
