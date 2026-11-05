package io.github.timur0o31.soa.log;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;

@Provider
public class EndpointFilter implements ContainerRequestFilter {
    @Context
    private ResourceInfo resource;

    @Override
    public void filter(ContainerRequestContext request) {
        var root = resource.getResourceClass().getAnnotation(Path.class);
        var method = resource.getResourceMethod().getAnnotation(Path.class);
        RequestTrace.endpoint((root == null ? "" : root.value()) + (method == null ? "" : method.value()));
    }
}
