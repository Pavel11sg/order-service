package com.example.tasks.orderservice.integration;

import com.example.tasks.orderservice.TestcontainersConfiguration;
import com.example.tasks.orderservice.model.Item;
import com.example.tasks.orderservice.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ItemRepositoryIntegrationTest {
	@Autowired
	private ItemRepository itemRepository;

	private Item savedItem;

	@BeforeEach
	void setUp() {
		itemRepository.deleteAll();
		Item item = new Item();
		item.setName("Test Laptop");
		item.setDescription("A powerful gaming laptop");
		item.setPrice(BigDecimal.valueOf(1499.99));
		item.setStockQuantity(10);
		savedItem = itemRepository.save(item);
	}

	@Test
	void existsAvailableItem_WhenItemExistsAndStockIsSufficient_ReturnsTrue() {
		// Arrange
		UUID existingItemId = savedItem.getItemId();
		Integer requestedQuantity = 5;
		// Act
		boolean isAvailable = itemRepository.existsAvailableItem(existingItemId, requestedQuantity);
		// Assert
		assertThat(isAvailable).isTrue();
	}

	@Test
	void existsAvailableItem_WhenItemExistsButStockIsInsufficient_ReturnsFalse() {
		// Arrange
		UUID existingItemId = savedItem.getItemId();
		Integer requestedQuantity = 20;
		// Act
		boolean isAvailable = itemRepository.existsAvailableItem(existingItemId, requestedQuantity);
		// Assert
		assertThat(isAvailable).isFalse();
	}

	@Test
	void existsAvailableItem_WhenItemDoesNotExist_ReturnsFalse() {
		// Arrange
		UUID nonExistentItemId = UUID.fromString("00000000-0000-0000-0000-000000000000");
		Integer requestedQuantity = 1;
		// Act
		boolean isAvailable = itemRepository.existsAvailableItem(nonExistentItemId, requestedQuantity);
		// Assert
		assertThat(isAvailable).isFalse();
	}

	@Test
	void decreaseStockQuantity_WhenStockIsSufficient_DecreasesStockAndReturnsOne() {
		// Arrange
		UUID existingItemId = savedItem.getItemId();
		Integer quantityToDecrease = 3;
		// Act
		int updatedRows = itemRepository.decreaseStockQuantity(existingItemId, quantityToDecrease);
		// Assert
		assertThat(updatedRows).isEqualTo(1);
		Item updatedItem = itemRepository.findById(existingItemId).orElseThrow();
		assertThat(updatedItem.getStockQuantity()).isEqualTo(7);
	}

	@Test
	void decreaseStockQuantity_WhenStockIsInsufficient_DoesNothingAndReturnsZero() {
		// Arrange
		UUID existingItemId = savedItem.getItemId();
		Integer quantityToDecrease = 15;
		// Act
		int updatedRows = itemRepository.decreaseStockQuantity(existingItemId, quantityToDecrease);
		// Assert
		assertThat(updatedRows).isZero();
		Item unchangedItem = itemRepository.findById(existingItemId).orElseThrow();
		assertThat(unchangedItem.getStockQuantity()).isEqualTo(10);
	}
}
