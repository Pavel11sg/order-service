package com.example.tasks.orderservice.service;

import com.example.tasks.orderservice.exception.ItemNotFoundException;
import com.example.tasks.orderservice.model.Item;
import com.example.tasks.orderservice.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ItemService {
	private final ItemRepository itemRepository;

	public ItemService(ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

	@Transactional(readOnly = true)
	public Item getItemById(UUID itemId) {
		return itemRepository.findById(itemId)
				.orElseThrow(() -> new ItemNotFoundException("Requested item with id = " + itemId + "not found in database"));
	}

	public boolean isItemAvailable(UUID itemId, Integer requestedQuantity) {
		return itemRepository.existsAvailableItem(itemId, requestedQuantity);
	}

	@Transactional
	public void decreaseStockQuantity(UUID itemId, Integer quantity) {
		int updatedRows = itemRepository.decreaseStockQuantity(itemId, quantity);
		if (updatedRows == 0) {
			throw new IllegalStateException(String.format("Failed to decrease stock for item: %s, quantity: %d. Not enough stock or item not found",
					itemId, quantity));
		}
	}
}
