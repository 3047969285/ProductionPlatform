package com.example.productionplatform.admin.service;

import com.example.productionplatform.admin.mapper.ProductMapper;
import com.example.productionplatform.admin.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper mapper;

    public List<Product> listAll() {
        return mapper.findAll();
    }

    public int count() {
        return mapper.countAll();
    }

    public boolean add(Product product) {
        product.setCreateTime(LocalDateTime.now());
        return mapper.insert(product) > 0;
    }

    public boolean update(Product product) {
        return mapper.update(product) > 0;
    }

    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
