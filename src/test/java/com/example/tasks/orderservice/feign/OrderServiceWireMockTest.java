package com.example.tasks.orderservice.feign;

import com.example.tasks.orderservice.TestcontainersConfiguration;
import com.example.tasks.orderservice.dto.request.OrderCreateRequestDto;
import com.example.tasks.orderservice.dto.request.OrderItemRequestDto;
import com.example.tasks.orderservice.dto.response.OrderWithUserResponseDto;
import com.example.tasks.orderservice.exception.UserNotFoundException;
import com.example.tasks.orderservice.model.Item;
import com.example.tasks.orderservice.repository.ItemRepository;
import com.example.tasks.orderservice.service.ItemService;
import com.example.tasks.orderservice.service.OrderService;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderServiceWireMockTest extends BaseWireMockTest {

	@Autowired
	private OrderService orderService;

	@Autowired
	private ItemRepository itemRepository;

	@MockitoBean
	private ItemService itemService;
	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");
	@Test
	void createOrder_WhenUserExists_ShouldCreateOrder() {
		// Arrange
		UUID userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
		Item mockItem = new Item();
		mockItem.setName("Test Item");
		mockItem.setPrice(BigDecimal.valueOf(10.0));
		mockItem.setStockQuantity(10);
		mockItem.setDescription("");
		mockItem.setCreatedAt(LocalDateTime.now());
		mockItem.setUpdatedAt(LocalDateTime.now());
		Item savedItem = itemRepository.save(mockItem);
		UUID itemId = savedItem.getItemId();
		String userJson = """
				{
				    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
				    "name": "John",
				    "surname": "Doe",
				    "email": "john@example.com",
				    "birthDate": "1990-01-01",
				    "cards": []
				}
				""";
		when(itemService.isItemAvailable(itemId, 1)).thenReturn(true);
		when(itemService.getItemById(itemId)).thenReturn(mockItem);
		doNothing().when(itemService).decreaseStockQuantity(itemId, 1);
		wireMock.stubFor(WireMock.get(urlEqualTo("/userservice/users/" + userId))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.withBody(userJson)));
		OrderCreateRequestDto request = new OrderCreateRequestDto(
				userId,
				List.of(new OrderItemRequestDto(itemId, 1))
		);
		// Act
		OrderWithUserResponseDto result = orderService.createOrder(request);
		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getUser().getId()).isEqualTo(userId);
		assertThat(result.getUser().getName()).isEqualTo("John");
		wireMock.verify(1, getRequestedFor(urlEqualTo("/userservice/users/" + userId)));
	}

	@Test
	void createOrder_WhenUserNotFound_ShouldThrowException() {
		// Arrange
		UUID userId = UUID.randomUUID();
		UUID itemId = UUID.randomUUID();
		wireMock.stubFor(get(urlEqualTo("/userservice/users/" + userId))
				.willReturn(aResponse()
						.withStatus(HttpStatus.NOT_FOUND.value())
						.withBody("User not found")));
		OrderCreateRequestDto request = new OrderCreateRequestDto(
				userId,
				List.of(new OrderItemRequestDto(itemId, 1))
		);
		// Act & Assert
		assertThatThrownBy(() -> orderService.createOrder(request))
				.isInstanceOf(UserNotFoundException.class)
				.hasMessageContaining("User not found");
		wireMock.verify(1, getRequestedFor(urlEqualTo("/userservice/users/" + userId)));
	}
}