package com.example.productservice.controller;

import com.example.productservice.dto.product.CreateProductRequest;
import com.example.productservice.dto.product.ProductDto;
import com.example.productservice.dto.product.ProductPreviewDto;
import com.example.productservice.dto.product.PurchaseRequest;
import com.example.productservice.dto.product.PurchaseResponse;
import com.example.productservice.dto.product.RestockRequest;
import com.example.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductPreviewDto> previewProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(productService.previewProduct(productId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDto>> getProductsByNameContaining(@RequestParam("name") String name) {
        return ResponseEntity.ok(productService.getAllByName(name));
    }

    @PostMapping
    public ResponseEntity<ProductPreviewDto> createProduct(@RequestBody @Valid CreateProductRequest createProductRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(createProductRequest));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID productId) {
        productService.delete(productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/purchase")
    public ResponseEntity<PurchaseResponse> purchaseProduct(@RequestBody @Valid PurchaseRequest purchaseRequest) {
        return ResponseEntity.ok(productService.buyProduct(purchaseRequest));
    }

    @PutMapping("/restock")
    public ResponseEntity<Void> restock(@RequestBody @Valid RestockRequest restockRequest) {
        productService.restock(restockRequest);
        return ResponseEntity.ok().build();
    }
}
