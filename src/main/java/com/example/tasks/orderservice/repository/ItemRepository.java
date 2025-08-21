package com.example.tasks.orderservice.repository;

import com.example.tasks.orderservice.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {
	@Query("SELECT COUNT(i) > 0 FROM Item i WHERE i.itemId = :itemId AND i.stockQuantity >= :quantity")
	boolean existsAvailableItem(@Param("itemId") UUID itemId, @Param("quantity") Integer quantity);

	@Modifying
	@Query("UPDATE Item i SET i.stockQuantity = i.stockQuantity - :quantity WHERE i.itemId = :itemId AND i.stockQuantity >= :quantity")
	int decreaseStockQuantity(@Param("itemId") UUID itemId, @Param("quantity") Integer quantity);
}
