package com.example.productservice.service;

import com.example.productservice.client.InventoryClient;
import com.example.productservice.dto.inventory.InventoryDto;
import com.example.productservice.dto.inventory.InventoryUpdateRequest;
import com.example.productservice.dto.product.CreateProductRequest;
import com.example.productservice.dto.product.ProductDto;
import com.example.productservice.dto.product.ProductPreviewDto;
import com.example.productservice.dto.product.PurchaseRequest;
import com.example.productservice.dto.product.PurchaseResponse;
import com.example.productservice.dto.product.RestockRequest;
import com.example.productservice.exception.ResourceNotFoundException;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;

    @Transactional
    public ProductPreviewDto create(CreateProductRequest createProductRequest) {
        Product product = Product.builder()
                .sellerId(getLoggedInUsersId())
                .name(createProductRequest.name())
                .description(createProductRequest.description())
                .price(createProductRequest.price())
                .build();
        productRepository.save(product);
        inventoryClient.addNewItemToInventory(new InventoryUpdateRequest(product.getId(), createProductRequest.quantity()));

        log.info("New product with id {} saved successfully", product.getId());
        return new ProductPreviewDto(product.getId(), product.getName(),
                product.getDescription(), product.getPrice(), createProductRequest.quantity());
    }

    public PurchaseResponse buyProduct(PurchaseRequest purchaseRequest) {
        Product product = findById(purchaseRequest.productId());
        InventoryUpdateRequest request = new InventoryUpdateRequest(product.getId(), purchaseRequest.quantity());
        inventoryClient.updateInventoryForPurchase(request);

        log.info("Processed purchase for {} units of product with id {}", purchaseRequest.quantity(), product.getId());
        return new PurchaseResponse(product.getName(), product.getPrice(),
                purchaseRequest.quantity(), Timestamp.from(Instant.now()));
    }

    public void restock(RestockRequest restockRequest) {
        Product product = findSellersProduct(restockRequest.productId(), getLoggedInUsersId());
        inventoryClient.updateInventoryForRestock(new InventoryUpdateRequest(product.getId(), restockRequest.quantity()));
        log.info("Product with id {} restocked with {} units", product.getId(), restockRequest.quantity());
    }

    public List<ProductDto> getAll() {
        return productRepository.findAll().stream()
                .map(product -> new ProductDto(product.getId(), product.getName(), product.getPrice()))
                .toList();
    }

    public ProductPreviewDto previewProduct(UUID id) {
        Product product = findById(id);
        InventoryDto response = inventoryClient.getInventoryForProduct(product.getId());

        return new ProductPreviewDto(product.getId(), product.getName(),
                product.getDescription(), product.getPrice(), response.quantity());
    }


    public List<ProductDto> getAllByName(String name) {
        return productRepository.findByNameContainsIgnoreCase(name).stream()
                .map(product -> new ProductDto(product.getId(), product.getName(), product.getPrice()))
                .toList();
    }

    @Transactional
    public void delete(UUID id) {
        productRepository.delete(findSellersProduct(id, getLoggedInUsersId()));
        inventoryClient.removeProductFromInventory(id);
        log.info("Product with id {} removed", id);
    }

    private Product findById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
    }

    private Product findSellersProduct(UUID productId, UUID sellerId) {
        return productRepository.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
    }

    private UUID getLoggedInUsersId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new AuthenticationCredentialsNotFoundException("Security context was not set");
        }

        return UUID.fromString(authentication.getName()); // seller's uuid is saved as 'name' in the security context
    }
}
