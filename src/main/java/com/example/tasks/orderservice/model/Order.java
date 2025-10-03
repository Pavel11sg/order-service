package com.example.tasks.orderservice.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id")
	private UUID orderId;
	@Column(name = "user_id", nullable = false)
	private UUID userId;
	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;
	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount;
	@Column(name = "creation_date", updatable = false, nullable = false)
//	@CreationTimestamp
	private LocalDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	@UpdateTimestamp
	private LocalDateTime updatedAt;
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> orderItems;
	@Column(name = "currency", nullable = false)
	private String currency;
	@Column(name = "payment_method_token")
	private String paymentMethodToken;
}
