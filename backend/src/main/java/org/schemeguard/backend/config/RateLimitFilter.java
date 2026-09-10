package org.schemeguard.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final UploadProperties properties;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    public RateLimitFilter(UploadProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || request.getRequestURI().startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String clientKey = request.getRemoteAddr();
        boolean chunkRequest = request.getRequestURI()
                .matches(".*/api/uploads/[^/]+/parts/\\d+$");
        int limit = chunkRequest
                ? properties.getChunkUploadsPerMinute()
                : properties.getApiRequestsPerMinute();
        String key = clientKey + (chunkRequest ? ":chunks" : ":api");

        if (!isAllowed(key, limit)) {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"message\":\"Too many requests. Try again later.\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(String key, int limit) {
        long currentWindow = System.currentTimeMillis() / 60_000;
        Counter counter = counters.compute(key, (ignored, previous) -> {
            if (previous == null || previous.window != currentWindow) {
                return new Counter(currentWindow);
            }
            return previous;
        });
        counter.requests.incrementAndGet();
        if (counters.size() > 10_000) {
            counters.entrySet().removeIf(entry -> entry.getValue().window < currentWindow - 1);
        }
        return counter.requests.get() <= limit;
    }

    private static final class Counter {
        private final long window;
        private final AtomicInteger requests = new AtomicInteger();

        private Counter(long window) {
            this.window = window;
        }
    }
}
