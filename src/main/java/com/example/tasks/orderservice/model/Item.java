package com.example.tasks.orderservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id")
	private UUID itemId;
	@Column(name = "name", nullable = false)
	private String name;
	@Column(name = "price", nullable = false, precision = 10, scale = 2)
	private BigDecimal price;
	@Column(name = "description")
	private String description;
	@Column(name = "stock_quantity", nullable = false)
	private Integer stockQuantity;
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	@OneToMany(mappedBy = "item")
	private List<OrderItem> orderItems;
}
