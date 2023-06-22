package com.example.productservice;

import com.example.productservice.dto.KeycloakTokenResponse;
import com.example.productservice.dto.inventory.InventoryDto;
import com.example.productservice.dto.inventory.InventoryUpdateRequest;
import com.example.productservice.dto.product.CreateProductRequest;
import com.example.productservice.dto.product.ProductDto;
import com.example.productservice.dto.product.ProductPreviewDto;
import com.example.productservice.dto.product.PurchaseRequest;
import com.example.productservice.dto.product.PurchaseResponse;
import com.example.productservice.dto.product.RestockRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.CompareOperation;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static com.example.productservice.util.Constants.BASE_URI;
import static com.example.productservice.util.Constants.CLIENT_ID;
import static com.example.productservice.util.Constants.CLIENT_SECRET;
import static com.example.productservice.util.Constants.GRANT_TYPE;
import static com.example.productservice.util.Constants.MOCK_BUYER;
import static com.example.productservice.util.Constants.MOCK_SELLER;
import static com.example.productservice.util.Constants.MOCK_TOKEN;
import static com.example.productservice.util.Constants.PRODUCT_1;
import static com.example.productservice.util.Constants.PRODUCT_2;
import static com.example.productservice.util.Constants.TOKEN_ISSUER_PATH;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DBRider
@DataSet(
        value = {"database.yml"},
        cleanAfter = true,
        cleanBefore = true)
class ProductServiceApplicationTests {

    @LocalServerPort
    private int port;

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer().withRealmImportFile("keycloak/realm-export.json");

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("postgres-test")
            .withUsername("psql")
            .withPassword("s3cre3t");

    private static ClientAndServer mockServer;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        // keycloak
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> keycloak.getAuthServerUrl() + "realms/e-com");

        // mock keycloak client
        registry.add("spring.security.oauth2.client.registration.product-service.client-id", () -> CLIENT_ID);
        registry.add("spring.security.oauth2.client.registration.product-service.client-secret", () -> CLIENT_SECRET);
        registry.add("spring.security.oauth2.client.provider.keycloak-provider.token-uri", () -> BASE_URI + TOKEN_ISSUER_PATH);
        registry.add("spring.security.oauth2.client.registration.product-service.authorization-grant-type", () -> GRANT_TYPE);

        // postgres
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        registry.add("clients.inventory", () -> BASE_URI);
    }

    @BeforeAll
    static void init() throws JsonProcessingException {
        mockServer = ClientAndServer.startClientAndServer(9999);

        // create global stub for fetching the token from keycloak for service-to-service auth
        KeycloakTokenResponse tokenResponse = new KeycloakTokenResponse(MOCK_TOKEN);
        mockServer.when(
                request()
                        .withMethod(HttpMethod.POST.name())
                        .withPath(TOKEN_ISSUER_PATH)
                        .withHeader(Header.header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8"))
                        .withBody("client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&grant_type=" + GRANT_TYPE)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withHeader(Header.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withBody(objectMapper.writeValueAsString(tokenResponse))
        );
    }

    @AfterAll
    static void tearDown() {
        mockServer.stop();
    }

    @Test
    void contextLoads() {
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("Get all products - should return 200 with all the existing products")
    void getAllProducts() {
        ProductDto[] response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .get("/api/product")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ProductDto[].class);

        assertThat(response).hasSize(2);
    }

    @Test
    @DisplayName("Preview product - should return 200 with the found product")
    void previewProduct() throws JsonProcessingException {
        InventoryDto inventoryDto = new InventoryDto(200);
        mockServer.when(
                request()
                        .withMethod(HttpMethod.GET.name())
                        .withHeader(Header.header(HttpHeaders.AUTHORIZATION, "Bearer " + MOCK_TOKEN))
                        .withPath("/api/inventory/" + PRODUCT_1.getId().toString())
        ).respond(
                response()
                        .withStatusCode(200)
                        .withHeader(Header.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withBody(objectMapper.writeValueAsString(inventoryDto))
        );

        ProductPreviewDto response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .get("/api/product/{productId}", PRODUCT_1.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ProductPreviewDto.class);

        assertThat(response).isNotNull();
        assertThat(response.quantity()).isEqualTo(inventoryDto.quantity());
    }

    @Test
    @DisplayName("Search for products - should return 200 with the found products")
    void getProductsByNameContaining() {
        ProductDto[] response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .param("name", "java")
                .get("/api/product")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ProductDto[].class);

        assertThat(response).hasSize(2);
    }

    @Test
    @DisplayName("Create products - should return 201 and create the new product")
    @ExpectedDataSet(value = "createProduct_expected.yml",
            compareOperation = CompareOperation.CONTAINS, ignoreCols = "id")
    void createProduct() {
        mockServer.when(
                request()
                        .withMethod(HttpMethod.POST.name())
                        .withHeader(Header.header(HttpHeaders.AUTHORIZATION, "Bearer " + MOCK_TOKEN))
                        .withPath("/api/inventory/add-stock")
                // ignore body as product id is randomly generated
        ).respond(
                response().withStatusCode(201)
        );

        CreateProductRequest requestBody = new CreateProductRequest("Test",
                "This is the description fo the product", 29.99, 200);
        String bearerToken = getBearerToken(MOCK_SELLER.get("username"), MOCK_SELLER.get("password"));
        ProductPreviewDto response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .post("/api/product")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(ProductPreviewDto.class);

        assertThat(response).isNotNull();
        assertThat(response.quantity()).isEqualTo(requestBody.quantity());
        assertThat(response.name()).isEqualTo(requestBody.name());
        assertThat(response.description()).isEqualTo(requestBody.description());
    }

    @Test
    @DisplayName("Create products - should return 400 because of invalid request body")
    void createProduct_badRequest() {
        CreateProductRequest requestBody = new CreateProductRequest("", "Too short", 0.0, 0);
        String bearerToken = getBearerToken(MOCK_SELLER.get("username"), MOCK_SELLER.get("password"));

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .post("/api/product")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Create products - should return 403 because only sellers can create products")
    void createProduct_forbidden() {
        CreateProductRequest requestBody = new CreateProductRequest("Test", "This is a description", 32.99, 10);
        String bearerToken = getBearerToken(MOCK_BUYER.get("username"), MOCK_BUYER.get("password"));

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .post("/api/product")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Delete product - should return 204 and delete the product")
    @ExpectedDataSet(value = "deleteProduct_expected.yml")
    void deleteProduct() {
        mockServer.when(
                request()
                        .withMethod(HttpMethod.DELETE.name())
                        .withHeader(Header.header(HttpHeaders.AUTHORIZATION, "Bearer " + MOCK_TOKEN))
                        .withPath("/api/inventory/" + PRODUCT_2.getId().toString())
        ).respond(
                response().withStatusCode(204)
        );

        String bearerToken = getBearerToken(MOCK_SELLER.get("username"), MOCK_SELLER.get("password"));
        given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .delete("/api/product/{productId}", PRODUCT_2.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

    }

    @Test
    @DisplayName("Delete products - should return 404 as product does not exist for this seller")
    void deleteProduct_notFound() {
        String bearerToken = getBearerToken(MOCK_SELLER.get("username"), MOCK_SELLER.get("password"));
        given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .delete("/api/product/{productId}", UUID.randomUUID())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Delete products - should return 403 because buyers can not delete products")
    void deleteProduct_forbidden() {
        String bearerToken = getBearerToken(MOCK_BUYER.get("username"), MOCK_BUYER.get("password"));
        given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .delete("/api/product/{productId}", UUID.randomUUID())
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Purchase product - should return 200 and the info about the bough product")
    void purchaseProduct() throws JsonProcessingException {
        InventoryUpdateRequest updateRequest = new InventoryUpdateRequest(PRODUCT_1.getId(), 20);
        mockServer.when(
                request()
                        .withMethod(HttpMethod.PUT.name())
                        .withHeader(Header.header(HttpHeaders.AUTHORIZATION, "Bearer " + MOCK_TOKEN))
                        .withPath("/api/inventory/purchase")
                        .withBody(objectMapper.writeValueAsString(updateRequest))
        ).respond(
                response().withStatusCode(200)
        );

        String bearerToken = getBearerToken(MOCK_BUYER.get("username"), MOCK_BUYER.get("password"));
        PurchaseRequest purchaseRequest = new PurchaseRequest(PRODUCT_1.getId(),20);
        PurchaseResponse response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(purchaseRequest)
                .post("/api/product/purchase")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(PurchaseResponse.class);

        assertThat(bearerToken).isNotNull();
        assertThat(response.productName()).isEqualTo(PRODUCT_1.getName());
        assertThat(response.price()).isEqualTo(PRODUCT_1.getPrice());
    }

    @Test
    @DisplayName("Purchase product - should return 400 because of invalid request body")
    void purchaseProduct_badRequest(){
        String bearerToken = getBearerToken(MOCK_BUYER.get("username"), MOCK_BUYER.get("password"));
        PurchaseRequest purchaseRequest = new PurchaseRequest(null,0);

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(purchaseRequest)
                .post("/api/product/purchase")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Restock product - should return 200")
    void restockProduct() throws JsonProcessingException {
        InventoryUpdateRequest updateRequest = new InventoryUpdateRequest(PRODUCT_1.getId(), 20);
        mockServer.when(
                request()
                        .withMethod(HttpMethod.PUT.name())
                        .withHeader(Header.header(HttpHeaders.AUTHORIZATION, "Bearer " + MOCK_TOKEN))
                        .withPath("/api/inventory/restock")
                        .withBody(objectMapper.writeValueAsString(updateRequest))
        ).respond(
                response().withStatusCode(200)
        );

        String bearerToken = getBearerToken(MOCK_SELLER.get("username"), MOCK_SELLER.get("password"));
        RestockRequest purchaseRequest = new RestockRequest(PRODUCT_1.getId(),20);
        given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(purchaseRequest)
                .put("/api/product/restock")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Restock product - should return 403 because buyers can not restock")
    void restockProduct_forbidden() {
        String bearerToken = getBearerToken(MOCK_BUYER.get("username"), MOCK_BUYER.get("password"));
        RestockRequest purchaseRequest = new RestockRequest(PRODUCT_1.getId(),20);

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(purchaseRequest)
                .put("/api/product/restock")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Restock product - should return 404 because product does not exist for seller")
    void restockProduct_notFound() {
        String bearerToken = getBearerToken(MOCK_SELLER.get("username"), MOCK_SELLER.get("password"));
        RestockRequest purchaseRequest = new RestockRequest(UUID.randomUUID(),20);

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(purchaseRequest)
                .put("/api/product/restock")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private static String getBearerToken(String username, String password) {
        try (Keycloak keycloakClient = KeycloakBuilder.builder()
                .serverUrl(keycloak.getAuthServerUrl())
                .realm("e-com")
                .clientId("access-token")
                .username(username)
                .password(password)
                .build()) {

            String accessToken = keycloakClient.tokenManager().getAccessTokenString();
            return "Bearer " + accessToken;
        } catch (Exception e) {
            return null;
        }
    }
}
