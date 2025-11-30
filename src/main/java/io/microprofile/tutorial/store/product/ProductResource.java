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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/products")
@ApplicationScoped
@Tag(name = "Product Resource", description = "CRUD operations for products")
public class ProductResource {
    @Inject ProductRepository productRepository;

    @Inject
    @ConfigProperty(name = "product.maintenance.mode", defaultValue = "false")
    private boolean maintenanceMode;

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
    @Operation(
            summary = "List all products",
            description = "Retrieves a list of all available products")
    @APIResponses(
            value = {
                @APIResponse(
                        responseCode = "200",
                        description = "Successful, list of products found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = Product.class))),
                @APIResponse(
                        responseCode = "400",
                        description = "Unsuccessful, no products found",
                        content = @Content(mediaType = "application/json"))
            })
    public Response getProducts() {
        // If in maintenance mode, return Service Unavailable status
        if (maintenanceMode) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(
                            "The product catalog service is currently in maintenance mode. Please try again later.")
                    .build();

            // If products found, return products and OK status
        }
        var products = productRepository.findAllProducts();

        if (products != null && !products.isEmpty()) {
            return Response.status(Response.Status.OK).entity(products).build();
            // If products not found, return Not Found status and message
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("No products found").build();
        }
    }
}
