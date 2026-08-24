package com.vss_market.gui;

import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigNumber;
import com.lowdragmc.lowdraglib2.configurator.ui.NumberConfigurator;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.GridTemplate;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEventListener;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.math.Size;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.systems.RenderSystem;
import com.viscript_lib.util.CountTextUtil;
import com.viscript_lib.util.item.SimpleItemStackFilter;
import com.viscriptshop.gui.components.Message;
import com.vss_market.data.*;
import com.vss_market.network.c2s.C2SPayload;
import dev.vfyjxf.taffy.style.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class MarketClientScreen extends UIElement {
    private static final float LISTING_CARD_WIDTH = 58f;
    private static final float LISTING_CARD_HEIGHT = 72f;
    private static final float LISTING_CARD_GAP = 7f;
    private static final String LISTING_FILTER_SELL = "vss_market.ui.listing_filter_sell";
    private static final String LISTING_FILTER_BUY = "vss_market.ui.listing_filter_buy";

    private enum View {
        MARKET,
        MANAGE,
        UPLOAD,
        DETAIL
    }

    private final MarketSavedData snapshot;
    private final UUID viewerId;
    private final int money;
    private final String messageKey;
    private final Message.Type messageType;
    private View view = View.MARKET;
    private String selectedShopId;
    private String selectedListingId = "";
    private String searchWord = "";
    private boolean showPurchaseOrders;
    private ItemStack uploadStack = ItemStack.EMPTY;
    private int uploadPrice = 1;
    private int uploadBundleSize = 1;
    private int uploadStock = 1;
    private boolean uploadPurchaseOrder;
    private float shopListScroll;
    private float manageListingScroll;

    private MarketClientScreen(MarketScreenPayload payload) {
        this.snapshot = payload.getMarket();
        this.viewerId = payload.getViewer();
        this.money = payload.getMoney();
        this.selectedShopId = normalizeSelectedShop(payload.getSelectedShop());
        this.selectedListingId = payload.getSelectedListing();
        this.view = parseView(payload.getView());
        this.messageKey = payload.getMessageKey();
        this.messageType = payload.parsedMessageType();
        this.uploadStack = payload.getUploadStack().isEmpty() ? ItemStack.EMPTY : payload.getUploadStack().copyWithCount(1);
        this.uploadPrice = Math.max(1, payload.getUploadPrice());
        this.uploadBundleSize = Math.max(1, payload.getUploadBundleSize());
        this.uploadStock = Math.max(1, payload.getUploadStock());
        this.uploadPurchaseOrder = payload.isUploadPurchaseOrder();
        this.shopListScroll = clampScroll(payload.getShopListScroll());
        this.manageListingScroll = clampScroll(payload.getManageListingScroll());
        initRoot();
    }

    public static void open(MarketScreenPayload payload) {
        Minecraft.getInstance().execute(() -> {
            var screen = new MarketClientScreen(payload);
            var modularUI = new ModularUI(UI.of(
                    screen,
                    List.of(StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP)),
                    MarketClientScreen::getAutoGuiScaledSize
            ));
            Minecraft.getInstance().setScreen(new ModularUIScreen(modularUI, Component.empty()));
        });
    }

    private void initRoot() {
        layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.justifyContent(AlignContent.CENTER);
            layout.alignItems(AlignItems.CENTER);
        });
        rebuild();
        if (!messageKey.isBlank()) {
            Message.send(messageType, messageKey, this);
        }
    }

    @Override
    public void initScreen(int screenWidth, int screenHeight) {
        super.initScreen(screenWidth, screenHeight);
        applyAutoGuiScaleTransform();
    }

    public static Size getAutoGuiScaledSize(Size screenSize) {
        float scale = getAutoGuiScaleFactor();
        if (scale <= 0f) {
            return screenSize;
        }
        return Size.of(
                Math.max(1, Math.round(screenSize.getWidth() / scale)),
                Math.max(1, Math.round(screenSize.getHeight() / scale))
        );
    }

    private void applyAutoGuiScaleTransform() {
        float scale = getAutoGuiScaleFactor();
        // 和 ViScriptShop 一样按 Auto GUI Scale 缩放，避免固定尺寸在高 GUI Scale 下跑出屏幕。
        transform(transform -> transform.pivot(0.5f, 0.5f).scale(scale));
    }

    private static float getAutoGuiScaleFactor() {
        Minecraft minecraft = Minecraft.getInstance();
        var window = minecraft.getWindow();
        double currentScale = window.getGuiScale();
        if (currentScale <= 0d) {
            return 1f;
        }
        int autoScale = window.calculateScale(0, minecraft.isEnforceUnicode());
        return Math.max(1f, (float) (autoScale / currentScale));
    }

    private void rebuild() {
        clearAllChildren();
        var shell = rowAuto().layout(layout -> {
            layout.widthPercent(90);
            layout.heightPercent(91);
            layout.gapAll(3);
            layout.justifyContent(AlignContent.CENTER);
            layout.alignItems(AlignItems.CENTER);
        }).addClass("panel_bg");
        shell.addChildren(createSidebar(), createMainPanel());
        addChild(shell);
    }

    private void requestMarket() {
        RPCPacketDistributor.rpcToServer(C2SPayload.REQUEST_MARKET_STATE, selectedShopId, view.name(), shopListScroll, manageListingScroll);
    }

    private static float clampScroll(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, value));
    }

    private UIElement createSidebar() {
        var sidebar = columnAuto().layout(layout -> {
            layout.widthPercent(27);
            layout.heightPercent(100);
            layout.gapAll(3);
        });

        var profile = rowAuto().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(18);
            layout.paddingAll(6);
            layout.gapAll(6);
            layout.alignItems(AlignItems.CENTER);
        }).addClass("panel_bg");

        var profileActions = columnAuto().layout(layout -> {
            layout.flex(1);
            layout.heightPercent(100);
            layout.gapAll(3);
            layout.justifyContent(AlignContent.CENTER);
        });
        var balance = new Label();
        balance.setText(Component.translatable("vss_market.ui.balance", countText(money)));
        balance.textStyle(style -> style
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER)
                .textWrap(TextWrap.HIDE)
                .lineSpacing(0));
        balance.layout(layout -> layout.widthPercent(100).height(14));
        profileActions.addChildren(
                balance,
                button("vss_market.ui.manage_shop", false, event -> {
                    selectedShopId = viewerId.toString();
                    selectedListingId = "";
                    view = View.MANAGE;
                    requestMarket();
                }).layout(layout -> {
                    layout.widthPercent(100);
                    layout.flex(1);
                })
        );
        profile.addChildren(
                new PlayerFaceElement(viewerId, currentPlayerName()).layout(layout -> {
                    layout.width(24);
                    layout.height(24);
                }),
                profileActions
        );

        var listPanel = columnAuto().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.paddingAll(5);
            layout.gapAll(4);
        }).addClass("panel_bg");

        var shopList = new ScrollerView();
        shopList.layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
        });
        shopList.viewContainer.layout(layout -> {
            layout.paddingAll(3);
            layout.gapAll(5);
            layout.flexDirection(FlexDirection.COLUMN);
        });
        snapshot.getShops().stream()
                .sorted(Comparator.comparing(PlayerShopData::getUpdatedTime).reversed())
                .forEach(shop -> shopList.addScrollViewChild(createShopRow(shop)));
        shopList.verticalScroller.setValue(shopListScroll, false);
        shopList.verticalScroller.setOnValueChanged(value -> shopListScroll = clampScroll(value));

        listPanel.addChildren(centerLabel("vss_market.ui.shops"), shopList);
        sidebar.addChildren(profile, listPanel);
        return sidebar;
    }

    private UIElement createShopRow(PlayerShopData shop) {
        var row = buttonBase(false)
                .noText()
                .setOnClick(event -> {
                    selectedShopId = shop.getOwnerId().toString();
                    selectedListingId = "";
                    view = View.MARKET;
                    requestMarket();
                });
        row.layout(layout -> {
            layout.widthPercent(100);
            layout.height(34);
            layout.paddingAll(4);
            layout.gapAll(5);
            layout.alignItems(AlignItems.CENTER);
        });
        var text = columnAuto().layout(layout -> {
            layout.flex(1);
            layout.heightPercent(100);
            layout.justifyContent(AlignContent.CENTER);
            layout.gapAll(2);
        });
        text.addChildren(
                shopRowLabel(shop.getName().isBlank() ? shop.getOwnerName() : shop.getName(), 8f),
                shopRowLabel(shop.getOwnerName(), 7f)
        );
        row.addChildren(
                new PlayerFaceElement(shop).layout(layout -> layout.width(18).height(18)),
                text
        );
        return row;
    }

    private UIElement createMainPanel() {
        return switch (view) {
            case MARKET -> createMarketPanel();
            case MANAGE -> createManagePanel();
            case UPLOAD -> createUploadPanel();
            case DETAIL -> createDetailPanel();
        };
    }

    private UIElement createMarketPanel() {
        var shop = selectedShop();
        var panel = mainColumn();

        var header = rowAuto().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(12);
            layout.paddingTop(2);
            layout.paddingHorizontal(8);
            layout.justifyContent(AlignContent.SPACE_BETWEEN);
            layout.alignItems(AlignItems.CENTER);
        }).addClass("panel_bg");

        var headerText = columnAuto().layout(layout -> {
            layout.flex(1);
            layout.heightPercent(100);
            layout.gapAll(2);
            layout.justifyContent(AlignContent.CENTER);
        });
        if (!searchWord.isBlank()) {
            headerText.addChildren(
                    label("vss_market.ui.search_results", searchWord),
                    label("vss_market.ui.search_scope")
            );
        } else if (shop.isPresent()) {
            var data = shop.get();
            headerText.addChildren(
                    literalLabel(data.getName().isBlank() ? data.getOwnerName() : data.getName()),
                    label("vss_market.ui.owner", data.getOwnerName())
            );
        } else {
            headerText.addChild(label("vss_market.ui.no_shop"));
        }

        var search = textField(searchWord, "vss_market.ui.search_placeholder");
        search.setTextResponder(value -> searchWord = value);
        var listingFilter = new Selector<String>()
                .setCandidates(List.of(LISTING_FILTER_SELL, LISTING_FILTER_BUY))
                .setCandidateUIProvider(value -> value == null ? new UIElement() : label(value)
                        .setId(LISTING_FILTER_BUY.equals(value) ? "listing_filter_buy_option" : "listing_filter_sell_option")
                        .layout(layout -> {
                    layout.widthPercent(100);
                    layout.height(14);
                }))
                .setSelected(showPurchaseOrders ? LISTING_FILTER_BUY : LISTING_FILTER_SELL)
                .setOnValueChanged(value -> {
                    showPurchaseOrders = LISTING_FILTER_BUY.equals(value);
                    rebuild();
                });
        var searchButton = button("vss_market.ui.search", false, event -> rebuild());
        header.addChildren(headerText, listingFilter.setId("listing_filter").layout(layout -> {
            layout.widthPercent(22);
            layout.heightPercent(78);
        }), search.layout(layout -> {
            layout.widthPercent(28);
            layout.heightPercent(78);
        }), searchButton.layout(layout -> {
            layout.widthPercent(13);
            layout.heightPercent(78);
        }));

        var body = bodyPanel();
        var listingArea = new ScrollerView();
        listingArea.layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
        });
        configureListingGrid(listingArea);
        int listingCount = addMarketListings(listingArea, shop);
        if (listingCount == 0) {
            configureCenteredPlaceholder(listingArea);
            listingArea.addScrollViewChild(emptyState(searchWord.isBlank() ? "vss_market.ui.no_listings" : "vss_market.ui.no_search_result"));
        }
        body.addChild(listingArea);
        panel.addChildren(header, body);
        return panel;
    }

    private UIElement createManagePanel() {
        var shop = ownShop();
        var panel = titledPanel("vss_market.ui.my_shop");
        var body = panel.getChildren().get(panel.getChildren().size() - 1);

        if (shop.isEmpty()) {
            body.addChildren(
                    label("vss_market.ui.no_shop"),
                    button("vss_market.ui.create_shop", false, event ->
                            RPCPacketDistributor.rpcToServer(C2SPayload.CREATE_SHOP))
                            .layout(layout -> layout.widthPercent(40).height(22))
            );
            return panel;
        }

        var data = shop.get();
        var name = textField(data.getName(), "vss_market.ui.shop_name_placeholder");
        body.addChildren(
                name.layout(layout -> {
                    layout.widthPercent(100);
                    layout.height(22);
                }),
                label("vss_market.ui.listing_count", countText(data.getListings().size())),
                label("vss_market.ui.pending_balance", countText(data.getBalance()))
        );

        var actions = rowAuto().layout(layout -> {
            layout.widthPercent(100);
            layout.height(23);
            layout.gapAll(6);
        });
        actions.addChildren(
                button("vss_market.ui.upload", false, event -> {
                    uploadStack = ItemStack.EMPTY;
                    uploadPrice = 1;
                    uploadBundleSize = 1;
                    uploadStock = 1;
                    uploadPurchaseOrder = false;
                    view = View.UPLOAD;
                    rebuild();
                }).layout(layout -> layout.flex(1)),
                button("vss_market.ui.purchase_records", false, event -> showPurchaseRecordsDialog(data)).layout(layout -> layout.flex(1)),
                button("vss_market.ui.withdraw", false, event ->
                        RPCPacketDistributor.rpcToServer(C2SPayload.WITHDRAW)).layout(layout -> layout.flex(1)),
                button("vss_market.ui.delete_shop", false, event -> showDeleteShopDialog()).layout(layout -> layout.flex(1)),
                button("vss_market.ui.save", true, event ->
                        RPCPacketDistributor.rpcToServer(C2SPayload.RENAME_SHOP, name.getText())).layout(layout -> layout.flex(1))
        );
        body.addChild(actions);

        var listingArea = new ScrollerView();
        listingArea.layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
        });
        configureListingGrid(listingArea);
        if (data.getListings().isEmpty()) {
            configureCenteredPlaceholder(listingArea);
            listingArea.addScrollViewChild(emptyState("vss_market.ui.no_listings"));
        } else {
            data.getListings().forEach(listing -> listingArea.addScrollViewChild(createListingCard(data, listing)));
        }
        listingArea.verticalScroller.setValue(manageListingScroll, false);
        listingArea.verticalScroller.setOnValueChanged(value -> manageListingScroll = clampScroll(value));
        body.addChild(listingArea);
        return panel;
    }

    private UIElement createUploadPanel() {
        var panel = titledPanel(uploadPurchaseOrder ? "vss_market.ui.upload_purchase_title" : "vss_market.ui.upload_title");
        var body = panel.getChildren().get(panel.getChildren().size() - 1);

        var listingType = rowAuto().layout(layout -> {
            layout.widthPercent(100);
            layout.height(22);
            layout.gapAll(6);
        });
        listingType.addChildren(
                button("vss_market.ui.listing_type_sell", !uploadPurchaseOrder, event -> {
                    uploadPurchaseOrder = false;
                    rebuild();
                }).setId("listing_type_sell").layout(layout -> layout.flex(1)),
                button("vss_market.ui.listing_type_buy", uploadPurchaseOrder, event -> {
                    uploadPurchaseOrder = true;
                    rebuild();
                }).setId("listing_type_buy").layout(layout -> layout.flex(1))
        );

        var selected = rowAuto().layout(layout -> {
            layout.widthPercent(100);
            layout.height(64);
            layout.gapAll(8);
            layout.paddingAll(5);
            layout.alignItems(AlignItems.CENTER);
        }).addClass("preview_bg");
        selected.addChildren(
                displayItemSlot(uploadStack).layout(layout -> layout.width(28).height(28)),
                columnAuto().layout(layout -> {
                    layout.flex(1);
                    layout.gapAll(2);
                }).addChildren(
                        label("vss_market.ui.selected_item"),
                        literalLabel(uploadStack.isEmpty() ? "-" : uploadStack.getHoverName().getString()),
                        literalLabel(uploadStack.isEmpty() ? "" : itemId(uploadStack))
                ),
                button("vss_market.ui.select_item", false, event -> showSelectItemDialog())
                        .layout(layout -> layout.widthPercent(24).height(22))
        );

        String priceKey = uploadPurchaseOrder ? "vss_market.ui.purchase_price" : "vss_market.ui.price";
        String stockKey = uploadPurchaseOrder ? "vss_market.ui.purchase_groups" : "vss_market.ui.stock_groups";
        var priceField = textField(Integer.toString(uploadPrice), priceKey);
        priceField.setNumbersOnlyInt(1, Integer.MAX_VALUE);
        priceField.setTextResponder(value -> uploadPrice = parseInt(value, uploadPrice));
        var bundleField = textField(Integer.toString(uploadBundleSize), "vss_market.ui.bundle_size");
        bundleField.setNumbersOnlyInt(1, Integer.MAX_VALUE);
        bundleField.setTextResponder(value -> uploadBundleSize = parseInt(value, uploadBundleSize));
        var stockField = textField(Integer.toString(uploadStock), stockKey);
        stockField.setNumbersOnlyInt(1, Integer.MAX_VALUE);
        stockField.setTextResponder(value -> uploadStock = parseInt(value, uploadStock));

        var inputs = rowAuto().layout(layout -> {
            layout.widthPercent(100);
            layout.gapAll(8);
        });
        inputs.addChildren(
                fieldGroup(priceKey, priceField),
                fieldGroup("vss_market.ui.bundle_size", bundleField),
                fieldGroup(stockKey, stockField)
        );

        var actions = rowAuto().layout(layout -> {
            layout.widthPercent(100);
            layout.height(24);
            layout.gapAll(8);
        });
        actions.addChildren(
                button("vss_market.ui.back", false, event -> {
                    view = View.MANAGE;
                    rebuild();
                }).layout(layout -> layout.flex(1)),
                button(uploadPurchaseOrder ? "vss_market.ui.upload_purchase" : "vss_market.ui.upload", true, event ->
                        RPCPacketDistributor.rpcToServer(C2SPayload.UPLOAD_LISTING,
                                uploadStack,
                                parseInt(priceField.getText(), 1),
                                parseInt(bundleField.getText(), 1),
                                parseInt(stockField.getText(), 1),
                                uploadPurchaseOrder)).setId("upload_submit")
                        .layout(layout -> layout.flex(1))
        );

        body.addChildren(listingType, selected, inputs, actions);
        return panel;
    }

    private UIElement createDetailPanel() {
        var shop = selectedShop();
        var listing = shop.flatMap(data -> data.findListing(selectedListingId));
        if (shop.isEmpty() || listing.isEmpty()) {
            view = View.MARKET;
            return createMarketPanel();
        }

        var shopData = shop.get();
        var listingData = listing.get();
        boolean owner = shopData.getOwnerId().equals(viewerId);
        boolean purchaseOrder = listingData.isPurchaseOrder();
        var panel = titledPanel("vss_market.ui.detail_title");
        var body = panel.getChildren().get(panel.getChildren().size() - 1);

        var detail = rowAuto().layout(layout -> {
            layout.widthPercent(100);
            layout.height(92);
            layout.gapAll(12);
            layout.paddingAll(8);
            layout.alignItems(AlignItems.CENTER);
        }).addClass("preview_bg");
        detail.addChildren(
                displayItemSlot(listingData.displayStack()).layout(layout -> layout.width(36).height(36)),
                columnAuto().layout(layout -> {
                    layout.flex(1);
                    layout.gapAll(3);
                }).addChildren(
                        literalLabel(listingData.getItem().getHoverName().getString()),
                        literalLabel(itemId(listingData.getItem())),
                        label("vss_market.ui.bundle_size_value", countText(listingData.getBundleSize())),
                        label(purchaseOrder ? "vss_market.ui.purchase_price_value" : "vss_market.ui.price_value", countText(listingData.getPrice())),
                        label(purchaseOrder ? "vss_market.ui.purchase_groups_value" : "vss_market.ui.stock_groups_value", countText(listingData.getStock())),
                        purchaseOrder
                                ? label("vss_market.ui.collected_groups_value", countText(listingData.getCollectedStock()))
                                : literalLabel("")
                )
        );
        body.addChild(detail);

        if (owner) {
            var restockKey = purchaseOrder ? "vss_market.ui.purchase_groups" : "vss_market.ui.stock_groups";
            var restock = textField("1", restockKey);
            restock.setNumbersOnlyInt(1, Integer.MAX_VALUE);
            var price = textField(Integer.toString(listingData.getPrice()), purchaseOrder ? "vss_market.ui.purchase_price" : "vss_market.ui.price");
            price.setNumbersOnlyInt(1, Integer.MAX_VALUE);

            var ownerInputs = rowAuto().layout(layout -> {
                layout.widthPercent(100);
                layout.gapAll(8);
            });
            ownerInputs.addChildren(fieldGroup(purchaseOrder ? "vss_market.ui.restock_purchase_count" : "vss_market.ui.restock_count", restock), fieldGroup("vss_market.ui.new_price", price));

            var buttons = actionRow();
            buttons.addChildren(
                    actionButton(purchaseOrder ? "vss_market.ui.add_purchase" : "vss_market.ui.restock", false, event ->
                            RPCPacketDistributor.rpcToServer(C2SPayload.RESTOCK_LISTING, listingData.getId(), parseInt(restock.getText(), 1), shopListScroll, manageListingScroll)),
                    actionButton("vss_market.ui.update_price", false, event ->
                            RPCPacketDistributor.rpcToServer(C2SPayload.UPDATE_PRICE, listingData.getId(), parseInt(price.getText(), listingData.getPrice()), shopListScroll, manageListingScroll)),
                    actionButton("vss_market.ui.remove_listing", false, event ->
                            RPCPacketDistributor.rpcToServer(C2SPayload.REMOVE_LISTING, listingData.getId(), shopListScroll, manageListingScroll)),
                    actionButton("vss_market.ui.back", false, event -> {
                        view = View.MANAGE;
                        selectedListingId = "";
                        rebuild();
                    })
            );
            body.addChildren(ownerInputs, buttons);
        } else {
            var buttons = actionRow();
            if (listingData.getStock() <= 0) {
                buttons.addChild(backToMarketButton());
                body.addChildren(label("vss_market.message.out_of_stock"), buttons);
            } else {
                int maxBuyGroups = listingData.getStock();
                int[] buyGroups = {1};
                var totalPrice = label(purchaseOrder ? "vss_market.ui.total_earnings" : "vss_market.ui.total_price", countText(listingData.getPrice()));
                totalPrice.layout(layout -> layout.width(80).height(18));

                var buyGroupsConfigurator = new NumberConfigurator(
                        "",
                        () -> buyGroups[0],
                        value -> {
                            int count = value == null ? 1 : value.intValue();
                            buyGroups[0] = Math.max(1, Math.min(maxBuyGroups, count));
                            totalPrice.setText(Component.translatable(purchaseOrder ? "vss_market.ui.total_earnings" : "vss_market.ui.total_price", countText((long) listingData.getPrice() * buyGroups[0])));
                        },
                        1,
                        false
                ).setType(ConfigNumber.Type.INTEGER).setRange(1, maxBuyGroups).setWheel(1);
                buyGroupsConfigurator.setLabel(Component.translatable(purchaseOrder ? "vss_market.ui.sell_groups" : "vss_market.ui.buy_groups"));
                buyGroupsConfigurator.layout(layout -> layout.width(122).height(22));
                buyGroupsConfigurator.lineContainer.layout(layout -> {
                    layout.widthPercent(100);
                    layout.height(22);
                    layout.alignItems(AlignItems.CENTER);
                });
                buyGroupsConfigurator.inlineContainer.layout(layout -> layout.height(22));
                buyGroupsConfigurator.textField.layout(layout -> {
                    layout.widthPercent(100);
                    layout.height(20);
                });

                var buyControls = rowAuto().layout(layout -> {
                    layout.widthPercent(100);
                    layout.height(26);
                    layout.paddingHorizontal(7);
                    layout.gapAll(8);
                    layout.justifyContent(AlignContent.CENTER);
                    layout.alignItems(AlignItems.CENTER);
                });
                buyControls.addChildren(buyGroupsConfigurator, totalPrice);

                buttons.addChildren(
                        actionButton(purchaseOrder ? "vss_market.ui.sell" : "vss_market.ui.buy", true, event ->
                                RPCPacketDistributor.rpcToServer(
                                        C2SPayload.BUY_LISTING,
                                        shopData.getOwnerId().toString(),
                                        listingData.getId(),
                                        Math.max(1, Math.min(maxBuyGroups, buyGroups[0])))),
                        backToMarketButton()
                );
                body.addChildren(buyControls, buttons);
            }
        }
        return panel;
    }

    private UIElement createListingCard(PlayerShopData shop, MarketListing listing) {
        var card = columnAuto().layout(layout -> {
            layout.width(LISTING_CARD_WIDTH);
            layout.height(LISTING_CARD_HEIGHT);
            layout.paddingAll(3);
            layout.gapAll(2);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addClass("preview_bg").addClass("listing_card");
        card.addEventListener(UIEvents.CLICK, event -> {
            selectedShopId = shop.getOwnerId().toString();
            selectedListingId = listing.getId();
            view = View.DETAIL;
            rebuild();
        });
        card.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> event.hoverTooltips = new HoverTooltips(List.of(
                Component.translatable(listing.isPurchaseOrder() ? "vss_market.ui.purchase_groups_value" : "vss_market.ui.stock_groups_value", countText(listing.getStock())),
                Component.translatable("vss_market.ui.bundle_size_value", countText(listing.getBundleSize())),
                Component.translatable("vss_market.ui.owner", shop.getOwnerName())
        ), null, null, null));

        boolean owner = shop.getOwnerId().equals(viewerId);
        var cardButton = button(owner ? "vss_market.ui.manage" : (listing.isPurchaseOrder() ? "vss_market.ui.sell" : "vss_market.ui.buy"), !owner, event -> {
            selectedShopId = shop.getOwnerId().toString();
            selectedListingId = listing.getId();
            view = View.DETAIL;
            rebuild();
        });
        cardButton.layout(layout -> {
            layout.width(42);
            layout.height(13);
            layout.marginTop(1);
        });
        cardButton.textStyle(style -> style.fontSize(7).textWrap(TextWrap.HIDE));

        card.addChildren(
                cardLabel(Component.literal(listing.getItem().isEmpty() ? "-" : listing.getItem().getHoverName().getString()), 6.5f, 10),
                displayItemSlot(listing.displayStack()).layout(layout -> layout.width(22).height(22)),
                cardLabel(Component.translatable("vss_market.ui.price_value", countText(listing.getPrice())), 7.5f, 9),
                cardButton
        );
        return card;
    }

    private void showDeleteShopDialog() {
        Dialog.showCheckBox(
                "vss_market.dialog.delete_shop.title",
                "vss_market.dialog.delete_shop.content",
                confirmed -> {
                    if (confirmed) {
                        RPCPacketDistributor.rpcToServer(C2SPayload.DELETE_SHOP);
                    }
                }
        ).show(this);
    }

    private void showPurchaseRecordsDialog(PlayerShopData shop) {
        var dialog = new Dialog();
        dialog.setTitle("vss_market.ui.purchase_records");
        dialog.overlay.layout(layout -> layout.width(264));

        if (shop.getPurchaseRecords().isEmpty()) {
            dialog.addContent(emptyState("vss_market.ui.no_purchase_records").layout(layout -> {
                layout.widthPercent(100);
                layout.height(32);
            }));
        } else {
            var records = new ScrollerView();
            records.layout(layout -> {
                layout.widthPercent(100);
                layout.height(178);
            });
            records.viewContainer.layout(layout -> {
                layout.widthPercent(100);
                layout.paddingAll(2);
                layout.gapAll(4);
                layout.flexDirection(FlexDirection.COLUMN);
            });
            for (var record : shop.getPurchaseRecords()) {
                records.addScrollViewChild(createPurchaseRecordRow(record));
            }
            dialog.addContent(records);
        }

        dialog.addButton(new Button()
                .setOnClick(event -> dialog.close())
                .setText("ldlib.gui.tips.confirm")
                .addClass("__confirm-button__"));
        dialog.show(this);
    }

    private static UIElement createPurchaseRecordRow(MarketPurchaseRecord record) {
        var row = rowAuto().layout(layout -> {
            layout.widthPercent(100);
            layout.height(42);
            layout.paddingAll(4);
            layout.gapAll(6);
            layout.alignItems(AlignItems.CENTER);
        }).addClass("preview_bg");

        var itemName = record.getItem().isEmpty() ? "-" : record.getItem().getHoverName().getString();
        var info = columnAuto().layout(layout -> {
            layout.flex(1);
            layout.heightPercent(100);
            layout.justifyContent(AlignContent.CENTER);
            layout.gapAll(2);
        });
        info.addChildren(
                recordLabel(Component.translatable(record.isPurchaseOrder() ? "vss_market.ui.purchase_record_seller" : "vss_market.ui.purchase_record_buyer", record.buyerDisplayName()), 7.5f, 11),
                recordLabel(Component.literal(itemName), 7f, 10)
        );

        var amount = columnAuto().layout(layout -> {
            layout.width(64);
            layout.heightPercent(100);
            layout.justifyContent(AlignContent.CENTER);
            layout.gapAll(2);
        });
        amount.addChildren(
                recordLabel(Component.translatable("vss_market.ui.purchase_record_quantity", countText(record.getQuantity())), 8f, 11),
                recordLabel(Component.translatable(record.isPurchaseOrder() ? "vss_market.ui.purchase_record_paid" : "vss_market.ui.purchase_record_spent", countText(record.getMoneySpent())), 7f, 10)
        );

        row.addChildren(
                displayItemSlot(record.displayStack()).layout(layout -> layout.width(24).height(24)),
                info,
                amount
        );
        return row;
    }

    private void showSelectItemDialog() {
        var dialog = new Dialog();
        dialog.setTitle("vss_market.ui.select_item");
        dialog.overlay.layout(layout -> layout.width(194));

        var slots = new InventorySlots();
        bindClientInventorySlots(slots);
        slots.layout(layout -> {
            layout.width(176);
            layout.gapAll(2);
        });
        slots.apply(slot -> slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 0) {
                var stack = slot.getValue();
                if (!stack.isEmpty()) {
                    uploadStack = stack.copyWithCount(1);
                    dialog.close();
                    rebuild();
                }
            }
            event.stopImmediatePropagation();
        }));

        dialog.addContent(slots);
        dialog.addButton(new Button()
                .setOnClick(event -> dialog.close())
                .setText("ldlib.gui.tips.cancel")
                .addClass("__cancel-button__"));
        dialog.show(this);
    }

    private static void bindClientInventorySlots(InventorySlots slots) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        var inventory = player.getInventory();
        // 当前市场是纯客户端 ModularUIScreen，InventorySlots 只会在容器菜单里自动绑定槽位。
        for (int row = 0; row < slots.rows.length; row++) {
            for (int column = 0; column < slots.rows[row].slots.length; column++) {
                slots.rows[row].slots[column].bind(new Slot(inventory, row * 9 + column + 9, 0, 0));
            }
        }
        for (int i = 0; i < slots.hotbar.slots.length; i++) {
            slots.hotbar.slots[i].bind(new Slot(inventory, i, 0, 0));
        }
    }

    private static UIElement actionRow() {
        return rowAuto().layout(layout -> {
                layout.widthPercent(100);
                layout.height(24);
                layout.paddingHorizontal(7);
                layout.gapAll(6);
                layout.justifyContent(AlignContent.CENTER);
                layout.alignItems(AlignItems.CENTER);
            });
    }

    private static Button actionButton(String key, boolean buying, UIEventListener onClick) {
        var button = button(key, buying, onClick);
        button.layout(layout -> layout.width(58).height(22));
        return button;
    }

    private Button backToMarketButton() {
        return actionButton("vss_market.ui.back", false, event -> {
            view = View.MARKET;
            selectedListingId = "";
            rebuild();
        });
    }

    private int addMarketListings(ScrollerView listingArea, Optional<PlayerShopData> selectedShop) {
        int count = 0;
        if (searchWord.isBlank()) {
            if (selectedShop.isPresent()) {
                var shop = selectedShop.get();
                for (var listing : shop.getListings()) {
                    if (listing.getStock() > 0 && matchesListingType(listing)) {
                        listingArea.addScrollViewChild(createListingCard(shop, listing));
                        count++;
                    }
                }
            }
            return count;
        }

        for (var shop : snapshot.getShops()) {
            for (var listing : shop.getListings()) {
                if (listing.getStock() > 0 && matchesListingType(listing) && matchesSearch(listing, shop)) {
                    listingArea.addScrollViewChild(createListingCard(shop, listing));
                    count++;
                }
            }
        }
        return count;
    }

    private static UIElement emptyState(String key) {
        return centerLabel(key).layout(layout -> {
            layout.widthPercent(100);
            layout.height(24);
        });
    }

    private Optional<PlayerShopData> selectedShop() {
        return snapshot.findShop(selectedShopId);
    }

    private Optional<PlayerShopData> ownShop() {
        return snapshot.findShop(viewerId);
    }

    private String normalizeSelectedShop(String requested) {
        if (requested != null && !requested.isBlank() && snapshot.findShop(requested).isPresent()) {
            return requested;
        }
        return snapshot.getShops().stream()
                .findFirst()
                .map(shop -> shop.getOwnerId().toString())
                .orElse("");
    }

    private boolean matchesSearch(MarketListing listing, PlayerShopData shop) {
        if (searchWord.isBlank()) {
            return true;
        }
        var word = searchWord.trim();
        return containsSearchText(shop.getName(), word)
                || containsSearchText(shop.getOwnerName(), word)
                || SimpleItemStackFilter.matchItemSearch(listing.getItem(), word);
    }

    private boolean matchesListingType(MarketListing listing) {
        return listing.isPurchaseOrder() == showPurchaseOrders;
    }

    private static boolean containsSearchText(String source, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        return source != null && source.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT).trim());
    }

    private static Optional<UUID> parseUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static Message.Type parseMessageType(String value) {
        try {
            return Message.Type.valueOf(value);
        } catch (Exception ignored) {
            return Message.Type.INFO;
        }
    }

    private static View parseView(String value) {
        try {
            return View.valueOf(value);
        } catch (Exception ignored) {
            return View.MARKET;
        }
    }

    private static String itemId(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static String countText(long value) {
        return CountTextUtil.formatCount(value);
    }

    private static UIElement mainColumn() {
        return columnAuto().layout(layout -> {
            layout.widthPercent(73);
            layout.heightPercent(100);
            layout.flexDirection(FlexDirection.COLUMN);
        });
    }

    private static UIElement titledPanel(String titleKey) {
        var panel = mainColumn();
        var header = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(12);
            layout.justifyContent(AlignContent.CENTER);
            layout.alignItems(AlignItems.CENTER);
        }).addClass("panel_bg");
        header.addChild(centerFillLabel(titleKey).layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
        }));
        panel.addChildren(header, bodyPanel());
        return panel;
    }

    private static UIElement bodyPanel() {
        return columnAuto().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.paddingAll(6);
            layout.paddingBottom(7);
            layout.gapAll(6);
        }).addClass("panel_bg");
    }

    private static UIElement fieldGroup(String titleKey, TextField field) {
        return columnAuto().layout(layout -> {
            layout.flex(1);
            layout.gapAll(3);
        }).addChildren(label(titleKey), field.layout(layout -> {
            layout.widthPercent(100);
            layout.height(22);
        }));
    }

    private static UIElement columnAuto() {
        return new UIElement().layout(layout -> layout.flexDirection(FlexDirection.COLUMN));
    }

    private static UIElement rowAuto() {
        return new UIElement().layout(layout -> layout.flexDirection(FlexDirection.ROW));
    }

    private static Label label(String key, Object... args) {
        var label = new Label();
        label.setText(Component.translatable(key, args));
        label.textStyle(style -> style.adaptiveHeight(true));
        return label;
    }

    private static Label centerLabel(String key) {
        var label = label(key);
        label.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER)
                .adaptiveHeight(true));
        return label;
    }

    private static Label centerFillLabel(String key) {
        var label = new Label();
        label.setText(Component.translatable(key));
        label.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER)
                .textWrap(TextWrap.HIDE)
                .lineSpacing(0));
        return label;
    }

    private static Label centerFillLabel(String key, Object... args) {
        var label = new Label();
        label.setText(Component.translatable(key, args));
        label.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER)
                .textWrap(TextWrap.HIDE)
                .lineSpacing(0));
        return label;
    }

    private static Label literalLabel(String text) {
        var label = new Label();
        label.setText(Component.literal(text));
        label.textStyle(style -> style.adaptiveHeight(true).textWrap(TextWrap.HIDE));
        return label;
    }

    private static Label shopRowLabel(String text, float fontSize) {
        var label = new Label();
        label.setText(Component.literal(text));
        label.layout(layout -> layout.widthPercent(100).height(10));
        label.textStyle(style -> style
                .textWrap(TextWrap.HIDE)
                .textAlignVertical(Vertical.CENTER)
                .fontSize(fontSize)
                .lineSpacing(0));
        return label;
    }

    private static Label cardLabel(Component text, float fontSize, float height) {
        var label = new Label();
        label.setText(text);
        label.layout(layout -> layout.widthPercent(100).height(height));
        label.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER)
                .textWrap(TextWrap.HIDE)
                .lineSpacing(0)
                .fontSize(fontSize));
        return label;
    }

    private static Label recordLabel(Component text, float fontSize, float height) {
        var label = new Label();
        label.setText(text);
        label.layout(layout -> layout.widthPercent(100).height(height));
        label.textStyle(style -> style
                .textAlignVertical(Vertical.CENTER)
                .textWrap(TextWrap.HIDE)
                .lineSpacing(0)
                .fontSize(fontSize));
        return label;
    }

    private static void configureListingGrid(ScrollerView listingArea) {
        listingArea.viewContainer.layout(layout -> {
            layout.display(TaffyDisplay.GRID);
            layout.gridAutoFlow(GridAutoFlow.ROW);
            layout.paddingAll(4);
            layout.gapAll(LISTING_CARD_GAP);
            layout.justifyItems(AlignItems.CENTER);
            layout.alignItems(AlignItems.FLEX_START);
            layout.justifyContent(AlignContent.CENTER);
            layout.alignContent(AlignContent.FLEX_START);
        });
        listingArea.viewPort.addEventListener(UIEvents.LAYOUT_CHANGED, event -> updateListingGridColumns(listingArea));
    }

    private static void configureCenteredPlaceholder(ScrollerView listingArea) {
        listingArea.viewContainer.layout(layout -> {
            layout.display(TaffyDisplay.FLEX);
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.paddingAll(4);
            layout.justifyContent(AlignContent.CENTER);
            layout.alignItems(AlignItems.CENTER);
        });
    }

    private static void updateListingGridColumns(ScrollerView listingArea) {
        float available = listingArea.viewPort.getContentWidth();
        if (available <= LISTING_CARD_WIDTH) {
            return;
        }
        int columns = Math.max(1, (int) Math.floor((available + LISTING_CARD_GAP) / (LISTING_CARD_WIDTH + LISTING_CARD_GAP)));
        while (columns > 1) {
            float required = columns * LISTING_CARD_WIDTH + (columns - 1) * LISTING_CARD_GAP;
            if (required <= available + 0.01f) {
                break;
            }
            columns--;
        }

        var tracks = new ArrayList<TrackSizingFunction>(columns);
        for (int i = 0; i < columns; i++) {
            tracks.add(TrackSizingFunction.fixed(LISTING_CARD_WIDTH));
        }
        listingArea.viewContainer.getLayout().gridTemplateColumns(new GridTemplate(tracks, List.of(), List.of()));
        listingArea.viewContainer.markTaffyStyleDirty();
    }

    private static TextField textField(String value, String placeholderKey) {
        var field = new TextField();
        field.setText(value);
        field.layout(layout -> layout.paddingLeft(5).justifyContent(AlignContent.CENTER));
        field.textFieldStyle(style -> style.placeholder(Component.translatable(placeholderKey)));
        return field;
    }

    private static Button button(String key, boolean buying, UIEventListener onClick) {
        var button = buttonBase(buying);
        button.setText(key);
        button.setOnClick(onClick);
        return button;
    }

    private static Button buttonBase(boolean buying) {
        var button = new Button();
        button.layout(layout -> {
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        });
        button.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER)
                .textWrap(TextWrap.HIDE)
                .lineSpacing(0));
        return button;
    }

    private static ItemSlot displayItemSlot(ItemStack stack) {
        var slot = new ItemSlot().setItem(stack);
        slot.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        slot.slotStyle(style -> style
                .slotOverlay(IGuiTexture.EMPTY)
                .hoverOverlay(IGuiTexture.EMPTY));
        return slot;
    }

    private static String currentPlayerName() {
        var player = Minecraft.getInstance().player;
        return player == null ? "" : player.getGameProfile().getName();
    }

    private static class PlayerFaceElement extends UIElement {
        private static final String TEXTURES_PROPERTY = "textures";
        private static final Map<UUID, Supplier<ResourceLocation>> REMOTE_SKIN_CACHE = new ConcurrentHashMap<>();
        private final Supplier<ResourceLocation> skinGetter;

        private PlayerFaceElement(UUID playerId, String playerName) {
            this(createProfile(playerId, playerName));
        }

        private PlayerFaceElement(PlayerShopData shop) {
            this(createProfile(shop));
        }

        private PlayerFaceElement(GameProfile profile) {
            this.skinGetter = createSkinGetter(profile);
            layout(layout -> {
                layout.width(18);
                layout.height(18);
            });
        }

        @Override
        public void drawBackgroundAdditional(GUIContext guiContext) {
            RenderSystem.depthMask(false);
            guiContext.graphics.drawManaged(() -> PlayerFaceRenderer.draw(
                    guiContext.graphics,
                    skinGetter.get(),
                    (int) getPositionX(),
                    (int) getPositionY(),
                    (int) Math.min(getSizeWidth(), getSizeHeight())
            ));
            RenderSystem.depthMask(true);
        }

        private static GameProfile createProfile(UUID playerId, String playerName) {
            return new GameProfile(playerId, playerName == null ? "" : playerName);
        }

        private static GameProfile createProfile(PlayerShopData shop) {
            var profile = createProfile(shop.getOwnerId(), shop.getOwnerName());
            if (!shop.getOwnerTexture().isBlank()) {
                profile.getProperties().put(TEXTURES_PROPERTY, createTextureProperty(shop));
            }
            return profile;
        }

        private static Property createTextureProperty(PlayerShopData shop) {
            if (shop.getOwnerTextureSignature().isBlank()) {
                return new Property(TEXTURES_PROPERTY, shop.getOwnerTexture());
            }
            return new Property(TEXTURES_PROPERTY, shop.getOwnerTexture(), shop.getOwnerTextureSignature());
        }

        private static Supplier<ResourceLocation> createSkinGetter(GameProfile profile) {
            var minecraft = Minecraft.getInstance();
            var playerId = profile.getId();
            var connection = minecraft.getConnection();
            if (connection != null && playerId != null) {
                var playerInfo = connection.getPlayerInfo(playerId);
                if (playerInfo != null) {
                    return playerInfo::getSkinLocation;
                }
            }
            if (profile.getProperties().containsKey(TEXTURES_PROPERTY) || playerId == null || Util.NIL_UUID.equals(playerId)) {
                return () -> minecraft.getSkinManager().getInsecureSkinLocation(profile);
            }
            return REMOTE_SKIN_CACHE.computeIfAbsent(playerId, ignored -> new RemoteSkinSupplier(profile));
        }

        private static class RemoteSkinSupplier implements Supplier<ResourceLocation> {
            private volatile Supplier<ResourceLocation> skinGetter;

            private RemoteSkinSupplier(GameProfile fallbackProfile) {
                var minecraft = Minecraft.getInstance();
                this.skinGetter = () -> minecraft.getSkinManager().getInsecureSkinLocation(fallbackProfile);
                SkullBlockEntity.updateGameprofile(fallbackProfile, gameProfile -> {
                    SkinManager manager = minecraft.getSkinManager();
                    var map = manager.getInsecureSkinInformation(gameProfile);
                    if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) skinGetter = () -> manager.registerTexture(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
                });
            }

            @Override
            public ResourceLocation get() {
                return skinGetter.get();
            }
        }
    }
}
