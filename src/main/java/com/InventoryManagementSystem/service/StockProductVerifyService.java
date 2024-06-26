package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.dto.ProductDTO;
import com.InventoryManagementSystem.model.Product;
import com.InventoryManagementSystem.service.CrudProductService;
import com.InventoryManagementSystem.service.ShoppingCartService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@AllArgsConstructor
public class StockProductVerifyService {

    private final CrudProductService crudProductService;
    private final ShoppingCartService shoppingCartService;

    /**
     * Apply discount to products if current time is within a specific time interval of the day.
     *
     * @param  days              the number of days to consider for near expiry products
     * @param  discountPercentage the percentage of discount to apply to the products
     */
    public void applyDiscountToProducts(int days, int discountPercentage) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startInterval = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0);
        LocalDateTime endInterval = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 1);

        if(now.isAfter(startInterval) && now.isBefore(endInterval)) {
            List<ProductDTO> nearExpiryProductsFromCrud = getNearExpiryProductsFromCrud(days);

            nearExpiryProductsFromCrud.stream()
                    .map(item -> {
                        Product product = new Product(item);
                        product.setPriceInCents(product.getPriceInCents() - (product.getPriceInCents() * discountPercentage / 100));
                        return product;
                    }).forEach(item -> {
                        ProductDTO updatedProductDTO = new ProductDTO(item);
                        crudProductService.updateProduct(updatedProductDTO);
                    });
        }
    }

    /**
     * Apply a discount to the products in the shopping cart if the current time is within a specific interval.
     *
     * @param  days              the number of days to consider for near expiry products
     * @param  discountPercentage the percentage of discount to apply
     */
    public void applyDiscountToCart(int days, int discountPercentage) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startInterval = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0);
        LocalDateTime endInterval = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 1);

        if(now.isAfter(startInterval) && now.isBefore(endInterval)) {
            List<ProductDTO> nearExpiryProductsFromCart = getNearExpiryProductsFromShoppingCart(days);

            nearExpiryProductsFromCart.stream()
                    .map(item -> {
                        Product product = new Product(item);
                        product.setPriceInCents(product.getPriceInCents() - (product.getPriceInCents() * discountPercentage / 100));
                        return product;
                    }).forEach(item -> {
                        ProductDTO updatedProductDTO = new ProductDTO(item);
                        crudProductService.updateProduct(updatedProductDTO);
                    });
        }

    }
    private List<ProductDTO> getNearExpiryProductsFromShoppingCart(int days) {
        return shoppingCartService.getShoppingCart()
                .stream()
                .filter(item -> item.expiryDate().isBefore(LocalDate.now().plusDays(days)))
                .toList();
    }

    private List<ProductDTO> getNearExpiryProductsFromCrud(int days) {
        return crudProductService.getAllProducts()
                .stream()
                .filter(item -> item.expiryDate().isBefore(LocalDate.now().plusDays(days)))
                .toList();
    }

    private List<ProductDTO> getExpiredProductsFromShoppingCart() {
        return shoppingCartService.getShoppingCart()
                .stream()
                .filter(item -> item.expiryDate().isBefore(LocalDate.now()))
                .toList();
    }

    private List<ProductDTO> getExpiredProductsFromCrud() {
        return crudProductService.getAllProducts()
                .stream()
                .filter(item -> item.expiryDate().isBefore(LocalDate.now()))
                .toList();
    }
}
