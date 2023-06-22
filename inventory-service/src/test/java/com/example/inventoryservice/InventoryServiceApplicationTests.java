package com.example.inventoryservice;

import com.example.inventoryservice.dto.InventoryDto;
import com.example.inventoryservice.dto.InventoryUpdateRequest;
import com.github.database.rider.core.api.dataset.CompareOperation;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static com.example.inventoryservice.utils.Constants.INVENTORY_1;
import static com.example.inventoryservice.utils.Constants.INVENTORY_2;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DBRider
@DataSet(
        value = {"database.yml"},
        cleanAfter = true,
        cleanBefore = true)
class InventoryServiceApplicationTests {

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer().withRealmImportFile("keycloak/realm-export.json");
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("postgres-test")
            .withUsername("psql")
            .withPassword("s3cre3t");
    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        // keycloak
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> keycloak.getAuthServerUrl() + "realms/master");

        // postgres
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Test
    void contextLoads() {
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("Get product inventory - should return 404 as product does not exist")
    void getInventoryForProduct_productNotFound() {
        given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .get("/api/inventory/{productId}", UUID.randomUUID())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Get product inventory - should return 200 and the inventory for the specified product")
    void getInventoryForProduct() {
        InventoryDto response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .get("/api/inventory/{productId}", INVENTORY_1.getProductId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(InventoryDto.class);

        assertThat(response).isNotNull();
        assertThat(response.quantity()).isEqualTo(INVENTORY_1.getQuantity());
    }

    @Test
    @DisplayName("Get product inventory - should return 400 because of the invalid request body")
    void updateInventoryForPurchase_badRequest() {
        var requestBody = new InventoryUpdateRequest(null, 0);

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .body(requestBody)
                .put("/api/inventory/purchase")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Process purchase - should return 200 and update the inventory for the specified product")
    @ExpectedDataSet(value = "updateInventoryForPurchase_expected.yml", compareOperation = CompareOperation.CONTAINS)
    void updateInventoryForPurchase() {
        var requestBody = new InventoryUpdateRequest(INVENTORY_1.getProductId(), 50);

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .body(requestBody)
                .put("/api/inventory/purchase")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Process purchase - should return 400 because of insufficient units in inventory")
    void updateInventoryForPurchase_badRequest_insufficientProducts() {
        var requestBody = new InventoryUpdateRequest(INVENTORY_2.getProductId(), 30);

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .body(requestBody)
                .put("/api/inventory/purchase")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Restock inventory - should return 200 and update the inventory for the specified product")
    @ExpectedDataSet(value = "updateInventoryForRestock_expected.yml", compareOperation = CompareOperation.CONTAINS)
    void updateInventoryForRestock() {
        var requestBody = new InventoryUpdateRequest(INVENTORY_2.getProductId(), 100);

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .body(requestBody)
                .put("/api/inventory/restock")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Add new item to inventory - should return 200 add new item to the inventory")
    @ExpectedDataSet(value = "addNewItemToInventory_expected.yml",
            compareOperation = CompareOperation.CONTAINS, ignoreCols = "id")
    void addNewItemToInventory() {
        var requestBody = new InventoryUpdateRequest(UUID.fromString("00000000-1000-0000-0000-000000000000"), 950);

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .body(requestBody)
                .post("/api/inventory/add-stock")
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("Should return 204 delete product from inventory")
    @ExpectedDataSet(value = "removeItemFromInventory_expected.yml")
    void removeProductFromInventory() {

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .delete("/api/inventory/{productId}", INVENTORY_2.getProductId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    private String getBearerToken() {
        try (Keycloak keycloakAdminClient = KeycloakBuilder.builder()
                .serverUrl(keycloak.getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username(keycloak.getAdminUsername())
                .password(keycloak.getAdminPassword())
                .build()) {

            String accessToken = keycloakAdminClient.tokenManager().getAccessTokenString();

            return "Bearer " + accessToken;
        } catch (Exception e) {
            return null;
        }
    }

}
