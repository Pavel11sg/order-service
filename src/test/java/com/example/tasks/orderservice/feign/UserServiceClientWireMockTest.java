package com.example.tasks.orderservice.feign;

import com.example.tasks.orderservice.dto.response.UserResponseDto;
import com.example.tasks.orderservice.proxy.UserServiceClient;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UserServiceClientWireMockTest extends BaseWireMockTest {

	@Autowired
	private UserServiceClient userServiceClient;

	@Test
	@DisplayName("getUser() - should return user on successful response 200")
	void getUser_WhenUserExists_ReturnsUser() {
		// Arrange
		UUID userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
		String expectedJson = """
				{
				    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
				    "name": "John",
				    "surname": "Doe",
				    "email": "john.doe@example.com",
				    "birthDate": "1990-01-01",
				    "cards": []
				}
				""";
		wireMock.stubFor(WireMock.get(urlEqualTo("/userservice/users/" + userId))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.withBody(expectedJson)));
		// Act
		UserResponseDto result = userServiceClient.getUser(userId);
		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(userId);
		assertThat(result.getName()).isEqualTo("John");
		assertThat(result.getSurname()).isEqualTo("Doe");
		wireMock.verify(1, getRequestedFor(urlEqualTo("/userservice/users/" + userId)));
	}

	@Test
	@DisplayName("getUser() - should throw an exception on 404 (user not found)")
	void getUser_WhenUserNotFound_ThrowsException() {
		// Arrange
		UUID userId = UUID.randomUUID();
		wireMock.stubFor(get(urlEqualTo("/userservice/users/" + userId))
				.willReturn(aResponse()
						.withStatus(HttpStatus.NOT_FOUND.value())
						.withBody("User not found")));
		// Act & Assert
		assertThatThrownBy(() -> userServiceClient.getUser(userId))
				.isInstanceOf(Exception.class)
				.hasMessageContaining("404");
		wireMock.verify(1, getRequestedFor(urlEqualTo("/userservice/users/" + userId)));
	}

	@Test
	@DisplayName("getUsersByIds() - should return a list of users on a successful request")
	void getUsersByIds_WhenUsersExist_ReturnsUsersList() {
		// Arrange
		List<UUID> userIds = List.of(
				UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"),
				UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f23456789012")
		);
		String expectedJson = """
				[
				    {
				        "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
				        "firstName": "John",
				        "lastName": "Doe",
				        "email": "john@example.com"
				    },
				    {
				        "id": "b2c3d4e5-f6a7-8901-bcde-f23456789012",
				        "firstName": "Jane", 
				        "lastName": "Smith",
				        "email": "jane@example.com"
				    }
				]
				""";
		wireMock.stubFor(get(urlPathEqualTo("/userservice/users/batch"))
				.withQueryParam("id", equalTo("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
				.withQueryParam("id", equalTo("b2c3d4e5-f6a7-8901-bcde-f23456789012"))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.withBody(expectedJson)));
		// Act
		List<UserResponseDto> result = userServiceClient.getUsersByIds(userIds);
		// Assert
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getId()).isEqualTo(userIds.get(0));
		assertThat(result.get(1).getId()).isEqualTo(userIds.get(1));
		wireMock.verify(1, getRequestedFor(urlPathEqualTo("/userservice/users/batch"))
				.withQueryParam("id", equalTo("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
				.withQueryParam("id", equalTo("b2c3d4e5-f6a7-8901-bcde-f23456789012")));
	}

	@Test
	@DisplayName("getUser() - should handle timeout correctly")
	void getUser_WhenTimeout_ShouldHandleGracefully() {
		// Arrange
		UUID userId = UUID.randomUUID();
		wireMock.stubFor(get(urlEqualTo("/userservice/users/" + userId))
				.willReturn(aResponse()
						.withFixedDelay(2000)
						.withStatus(HttpStatus.OK.value())
						.withBody("{\"id\": \"" + userId + "\", \"firstName\": \"Test\"}")));
		// Act & Assert
		assertThatThrownBy(() -> userServiceClient.getUser(userId))
				.isInstanceOf(Exception.class);
		wireMock.verify(1, getRequestedFor(urlEqualTo("/userservice/users/" + userId)));
	}

	@Test
	@DisplayName("getUser() - must handle 500 server error")
	void getUser_WhenServerError_ThrowsException() {
		// Arrange
		UUID userId = UUID.randomUUID();
		wireMock.stubFor(get(urlEqualTo("/userservice/users/" + userId))
				.willReturn(aResponse()
						.withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
						.withBody("Internal Server Error")));
		// Act & Assert
		assertThatThrownBy(() -> userServiceClient.getUser(userId))
				.isInstanceOf(Exception.class)
				.hasMessageContaining("500");
		wireMock.verify(1, getRequestedFor(urlEqualTo("/userservice/users/" + userId)));
	}
}
