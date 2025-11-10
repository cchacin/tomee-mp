package io.microprofile.tutorial.store.product;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

@RequestScoped
public class ProductRepository {

    private static final Logger LOGGER = Logger.getLogger(ProductRepository.class.getName());

    @PersistenceContext(unitName = "catalogPU")
    private EntityManager entityManager;

    public List<Product> findAllProducts() {
        LOGGER.fine("JPA Repository: Finding all products");
        TypedQuery<Product> query =
                entityManager.createNamedQuery("Product.findAll", Product.class);
        return query.getResultList();
    }

    public Product findProductById(Long id) {
        LOGGER.fine("JPA Repository: Finding product with ID: " + id);
        if (id == null) {
            return null;
        }
        return entityManager.find(Product.class, id);
    }

    public Product createProduct(Product product) {
        LOGGER.info("JPA Repository: Creating new product: " + product);
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        // Ensure ID is null for new products
        product.setId(null);

        entityManager.persist(product);
        entityManager.flush(); // Force the insert to get the generated ID

        LOGGER.info("JPA Repository: Created product with ID: " + product.getId());
        return product;
    }

    public Product updateProduct(Product product) {
        LOGGER.info("JPA Repository: Updating product: " + product);
        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException("Product and ID cannot be null for update");
        }

        Product existingProduct = entityManager.find(Product.class, product.getId());
        if (existingProduct == null) {
            LOGGER.warning("JPA Repository: Product not found for update: " + product.getId());
            return null;
        }

        // Update fields
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());

        Product updatedProduct = entityManager.merge(existingProduct);
        entityManager.flush();

        LOGGER.info("JPA Repository: Updated product with ID: " + updatedProduct.getId());
        return updatedProduct;
    }

    public boolean deleteProduct(Long id) {
        LOGGER.info("JPA Repository: Deleting product with ID: " + id);
        if (id == null) {
            return false;
        }

        Product product = entityManager.find(Product.class, id);
        if (product != null) {
            entityManager.remove(product);
            entityManager.flush();
            LOGGER.info("JPA Repository: Deleted product with ID: " + id);
            return true;
        }

        LOGGER.warning("JPA Repository: Product not found for deletion: " + id);
        return false;
    }

    public List<Product> searchProducts(
            String name, String description, Double minPrice, Double maxPrice) {
        LOGGER.info("JPA Repository: Searching for products with criteria");

        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE 1=1");

        if (name != null && !name.trim().isEmpty()) {
            jpql.append(" AND LOWER(p.name) LIKE :namePattern");
        }

        if (description != null && !description.trim().isEmpty()) {
            jpql.append(" AND LOWER(p.description) LIKE :descriptionPattern");
        }

        if (minPrice != null) {
            jpql.append(" AND p.price >= :minPrice");
        }

        if (maxPrice != null) {
            jpql.append(" AND p.price <= :maxPrice");
        }

        TypedQuery<Product> query = entityManager.createQuery(jpql.toString(), Product.class);

        // Set parameters only if they are provided
        if (name != null && !name.trim().isEmpty()) {
            query.setParameter("namePattern", "%" + name.toLowerCase() + "%");
        }

        if (description != null && !description.trim().isEmpty()) {
            query.setParameter("descriptionPattern", "%" + description.toLowerCase() + "%");
        }

        if (minPrice != null) {
            query.setParameter("minPrice", BigDecimal.valueOf(minPrice));
        }

        if (maxPrice != null) {
            query.setParameter("maxPrice", BigDecimal.valueOf(maxPrice));
        }

        List<Product> results = query.getResultList();
        LOGGER.info("JPA Repository: Found " + results.size() + " products matching criteria");

        return results;
    }
}
