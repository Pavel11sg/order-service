package com.example.tasks.orderservice.unit;

import com.example.tasks.orderservice.exception.ItemNotFoundException;
import com.example.tasks.orderservice.model.Item;
import com.example.tasks.orderservice.repository.ItemRepository;
import com.example.tasks.orderservice.service.ItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.example.tasks.orderservice.unit.ItemTestData.ITEM_ID_1;
import static com.example.tasks.orderservice.unit.ItemTestData.ITEM_NAME_1;
import static com.example.tasks.orderservice.unit.ItemTestData.ITEM_PRICE_1;
import static com.example.tasks.orderservice.unit.ItemTestData.NON_EXISTENT_ITEM_ID;
import static com.example.tasks.orderservice.unit.ItemTestData.STOCK_QUANTITY_1;
import static com.example.tasks.orderservice.unit.ItemTestData.createDefaultItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Item Service Unit Tests")
class ItemServiceTest {

	@Mock
	private ItemRepository itemRepository;

	@InjectMocks
	private ItemService itemService;

	@Nested
	@DisplayName("Get Item Tests")
	class GetItemTests {

		@Test
		@DisplayName("Should return item by id successfully")
		void getItemById_withValidId_shouldReturnItem() {
			// Arrange
			Item expectedItem = createDefaultItem();
			when(itemRepository.findById(ITEM_ID_1)).thenReturn(Optional.of(expectedItem));
			// Act
			Item result = itemService.getItemById(ITEM_ID_1);
			// Assert
			assertThat(result).isNotNull();
			assertThat(result.getItemId()).isEqualTo(ITEM_ID_1);
			assertThat(result.getName()).isEqualTo(ITEM_NAME_1);
			assertThat(result.getPrice()).isEqualTo(ITEM_PRICE_1);
			assertThat(result.getStockQuantity()).isEqualTo(STOCK_QUANTITY_1);
			// Verify
			verify(itemRepository).findById(ITEM_ID_1);
		}

		@Test
		@DisplayName("Should throw ItemNotFoundException when item not found")
		void getItemById_withNonExistentId_shouldThrowItemNotFoundException() {
			// Arrange
			when(itemRepository.findById(NON_EXISTENT_ITEM_ID)).thenReturn(Optional.empty());
			// Act & Assert
			assertThatThrownBy(() -> itemService.getItemById(NON_EXISTENT_ITEM_ID))
					.isInstanceOf(ItemNotFoundException.class)
					.hasMessageContaining("Requested item with id");
			// Verify
			verify(itemRepository).findById(NON_EXISTENT_ITEM_ID);
		}
	}

	@Nested
	@DisplayName("Item Availability Tests")
	class ItemAvailabilityTests {

		@Test
		@DisplayName("Should return true when item is available")
		void isItemAvailable_withSufficientStock_shouldReturnTrue() {
			// Arrange
			when(itemRepository.existsAvailableItem(ITEM_ID_1, 5)).thenReturn(true);
			// Act
			boolean result = itemService.isItemAvailable(ITEM_ID_1, 5);
			// Assert
			assertThat(result).isTrue();
			// Verify
			verify(itemRepository).existsAvailableItem(ITEM_ID_1, 5);
		}

		@Test
		@DisplayName("Should return false when item is not available")
		void isItemAvailable_withInsufficientStock_shouldReturnFalse() {
			// Arrange
			when(itemRepository.existsAvailableItem(ITEM_ID_1, 15)).thenReturn(false);
			// Act
			boolean result = itemService.isItemAvailable(ITEM_ID_1, 15);
			// Assert
			assertThat(result).isFalse();
			// Verify
			verify(itemRepository).existsAvailableItem(ITEM_ID_1, 15);
		}
	}

	@Nested
	@DisplayName("Decrease Stock Tests")
	class DecreaseStockTests {

		@Test
		@DisplayName("Should decrease stock quantity successfully")
		void decreaseStockQuantity_withValidQuantity_shouldDecreaseStock() {
			// Arrange
			when(itemRepository.decreaseStockQuantity(ITEM_ID_1, 3)).thenReturn(1);
			// Act
			itemService.decreaseStockQuantity(ITEM_ID_1, 3);
			// Verify
			verify(itemRepository).decreaseStockQuantity(ITEM_ID_1, 3);
		}

		@Test
		@DisplayName("Should throw IllegalStateException when stock decrease fails")
		void decreaseStockQuantity_withInvalidQuantity_shouldThrowIllegalStateException() {
			// Arrange
			when(itemRepository.decreaseStockQuantity(ITEM_ID_1, 20)).thenReturn(0);
			// Act & Assert
			assertThatThrownBy(() -> itemService.decreaseStockQuantity(ITEM_ID_1, 20))
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("Failed to decrease stock");
			// Verify
			verify(itemRepository).decreaseStockQuantity(ITEM_ID_1, 20);
		}
	}
}