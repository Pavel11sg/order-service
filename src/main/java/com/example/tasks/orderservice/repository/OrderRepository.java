package com.example.tasks.orderservice.repository;

import com.example.tasks.orderservice.model.Order;
import com.example.tasks.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
	List<Order> findByOrderIdIn(List<UUID> orderIds);

	List<Order> findByOrderStatus(OrderStatus orderStatus);

	List<Order> findByOrderStatusIn(List<OrderStatus> orderStatuses);
}
