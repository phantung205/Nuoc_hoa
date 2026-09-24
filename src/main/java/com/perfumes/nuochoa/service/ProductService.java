package com.perfumes.nuochoa.service;

import com.perfumes.nuochoa.dto.ProductRequestDTO;
import com.perfumes.nuochoa.dto.ProductResponseDTO;

import java.util.List;

public interface ProductService {
    List<ProductResponseDTO> getAllProducts();
    List<ProductResponseDTO> getAllActiveProducts();
    ProductResponseDTO getProductById(Long id);
    ProductResponseDTO createProduct(ProductRequestDTO requestDTO);
    ProductRequestDTO getProductRequestDTOById(Long id);
    ProductResponseDTO updateProduct(Long id, ProductRequestDTO requestDTO);
    void deleteProduct(Long id);
}