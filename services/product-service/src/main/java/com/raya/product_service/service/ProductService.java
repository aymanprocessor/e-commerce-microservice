package com.raya.product_service.service;

import com.raya.product_service.model.Product;
import com.raya.product_service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }


    public Product getProductById(Long id){
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("product not found with id: " + id));
    }

    public Product createProduct(Product product){

        product.setId(null);
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product product){
        Product existingProduct = getProductById(id);

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setCategory(product.getCategory());

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Long id){
        Product existingProduct = getProductById(id);
        productRepository.delete(existingProduct);


    }

}
