package com.example.tasks.orderservice.unit;

import com.example.tasks.orderservice.model.Item;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ItemTestData {

	public static final UUID ITEM_ID_1 = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
	public static final UUID ITEM_ID_2 = UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
	public static final UUID NON_EXISTENT_ITEM_ID = UUID.fromString("999e4567-e89b-12d3-a456-426614174999");

	public static final String ITEM_NAME_1 = "iPhone 16";
	public static final String ITEM_NAME_2 = "HP ProBook 450G";

	public static final BigDecimal ITEM_PRICE_1 = BigDecimal.valueOf(3900);
	public static final BigDecimal ITEM_PRICE_2 = BigDecimal.valueOf(4100);

	public static final String DESCRIPTION_1 = "Apple iPhone 16 with 256Gb memory";
	public static final String DESCRIPTION_2 = "HP ProBook 450G, silver";

	public static final int STOCK_QUANTITY_1 = 10;
	public static final int STOCK_QUANTITY_2 = 15;

	public static final LocalDateTime CREATED_AT = LocalDateTime.now();
	public static final LocalDateTime UPDATED_AT = LocalDateTime.now();

	public static Item createDefaultItem() {
		return createItem(ITEM_ID_1, ITEM_NAME_1, ITEM_PRICE_1, STOCK_QUANTITY_1);
	}

	private ItemTestData() {
	}

	public static Item createItem(UUID id, String name, BigDecimal price, int stockQuantity) {
		Item item = new Item();
		item.setItemId(id);
		item.setName(name);
		item.setPrice(price);
		item.setStockQuantity(stockQuantity);
		item.setCreatedAt(CREATED_AT);
		item.setUpdatedAt(UPDATED_AT);
		return item;
	}

	public static Item createItem1() {
		return createItem(ITEM_ID_1, ITEM_NAME_1, ITEM_PRICE_1, STOCK_QUANTITY_1);
	}

	public static Item createItem2() {
		return createItem(ITEM_ID_2, ITEM_NAME_2, ITEM_PRICE_2, STOCK_QUANTITY_2);
	}

	public static Item createItemWithCustomStock(UUID itemId, int stockQuantity) {
		return createItem(itemId, ITEM_NAME_1, ITEM_PRICE_1, stockQuantity);
	}
}