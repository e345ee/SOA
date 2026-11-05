package io.github.timur0o31.soa.log;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@WebFilter("/api/v1/*")
public class RequestTrace implements Filter {
    public static final String HEADER = "X-Request-ID";
    private static final Logger LOG = Logger.getLogger("soa.requests");
    private static final ThreadLocal<RequestTrace> CURRENT = new ThreadLocal<>();
    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}");
    private String id;
    private String endpoint = "unmatched";
    private int sqlCount;
    private long sqlMicros;

    public static String requestId() {
        var trace = CURRENT.get();
        return trace == null ? UUID.randomUUID().toString() : trace.id;
    }

    static void endpoint(String path) {
        var trace = CURRENT.get();
        if (trace != null) trace.endpoint = path;
    }

    public static void sql(long elapsed, boolean success, String queryId) {
        var trace = CURRENT.get();
        if (trace == null) return;
        trace.sqlCount++;
        trace.sqlMicros += elapsed;
        trace.log("sql", Json.createObjectBuilder().add("sequence", trace.sqlCount)
                .add("queryId", queryId).add("durationUs", elapsed).add("success", success));
    }

    public static void downstream(String operation, int status, long start) {
        var trace = CURRENT.get();
        if (trace != null) trace.log("downstream", Json.createObjectBuilder().add("target", "ticket")
                .add("operation", operation).add("status", status).add("durationMs", elapsed(start)));
    }

    public static void failure(Throwable exception) {
        var trace = CURRENT.get();
        if (trace == null) return;
        var frames = Json.createArrayBuilder();
        java.util.Arrays.stream(exception.getStackTrace()).limit(12).forEach(frame -> frames.add(frame.toString()));
        trace.log("error", Json.createObjectBuilder().add("exception", exception.getClass().getName())
                .add("cause", exception.getCause() == null ? "" : exception.getCause().getClass().getName())
                .add("frames", frames));
    }

    private void log(String event, JsonObjectBuilder fields) {
        LOG.info(fields.add("event", event).add("requestId", id).add("path", endpoint).build().toString());
    }

    private static long elapsed(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        var http = (HttpServletRequest) request;
        var result = (HttpServletResponse) response;
        var trace = new RequestTrace();
        String incoming = http.getHeader(HEADER);
        trace.id = incoming != null && UUID_PATTERN.matcher(incoming).matches() ? incoming : UUID.randomUUID().toString();
        CURRENT.set(trace);
        long start = System.nanoTime();
        int status = 500;
        try {
            result.setHeader(HEADER, trace.id);
            chain.doFilter(request, response);
            status = result.getStatus();
        } finally {
            try {
                trace.log("http", Json.createObjectBuilder().add("method", http.getMethod())
                        .add("status", status).add("durationMs", elapsed(start))
                        .add("sqlCount", trace.sqlCount).add("sqlMs", trace.sqlMicros / 1000.0));
            } finally {
                CURRENT.remove();
            }
        }
    }
}
