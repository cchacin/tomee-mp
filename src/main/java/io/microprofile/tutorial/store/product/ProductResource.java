package io.microprofile.tutorial.store.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/products")
@ApplicationScoped
public class ProductResource {
    @Inject ProductRepository productRepository;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response createProduct(Product product) {
        System.out.println("Creating product");
        productRepository.createProduct(product);
        return Response.status(Response.Status.CREATED).entity("New product created").build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateProduct(Product product) {
        // Update an existing product
        Response response;
        System.out.println("Updating product");
        Product updatedProduct = productRepository.updateProduct(product);

        if (updatedProduct != null) {
            response = Response.status(Response.Status.OK).entity("Product updated").build();
        } else {
            response =
                    Response.status(Response.Status.NOT_FOUND).entity("Product not found").build();
        }
        return response;
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response deleteProduct(@PathParam("id") Long id) {
        // Delete a product
        Response response;
        System.out.println("Deleting product with id: " + id);
        Product product = productRepository.findProductById(id);
        if (product != null) {
            productRepository.deleteProduct(product.getId());
            response = Response.status(Response.Status.OK).entity("Product deleted").build();
        } else {
            response =
                    Response.status(Response.Status.NOT_FOUND).entity("Product not found").build();
        }
        return response;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Product> getProducts() {
        // Return a list of products
        return productRepository.findAllProducts();
    }
}
