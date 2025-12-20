package io.microprofile.tutorial.store.product;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@ApplicationScoped
public class ProductServiceLivenessCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory =
                runtime.maxMemory(); // Maximum amount of memory the JVM will attempt to use
        long allocatedMemory = runtime.totalMemory(); // Total memory currently allocated to the JVM
        long freeMemory = runtime.freeMemory(); // Amount of free memory within the allocated memory
        long usedMemory = allocatedMemory - freeMemory; // Actual memory used
        long availableMemory = maxMemory - usedMemory; // Total available memory

        long threshold = 100 * 1024 * 1024; // threshold: 100MB

        // Including diagnostic data in the response
        HealthCheckResponseBuilder responseBuilder =
                HealthCheckResponse.named("systemResourcesLiveness")
                        .withData("FreeMemory", freeMemory)
                        .withData("MaxMemory", maxMemory)
                        .withData("AllocatedMemory", allocatedMemory)
                        .withData("UsedMemory", usedMemory)
                        .withData("AvailableMemory", availableMemory);

        if (availableMemory > threshold) {
            // The system is considered live
            responseBuilder = responseBuilder.up();
        } else {
            // The system is not live.
            responseBuilder = responseBuilder.down();
        }

        return responseBuilder.build();
    }
}
