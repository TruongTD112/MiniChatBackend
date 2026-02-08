package miniapp.com.vn.minichatbackend.security;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Wrapper that reads and caches the request body once, so it can be logged
 * before the filter chain and still be read by the controller.
 */
public class CachedBodyRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    public CachedBodyRequestWrapper(HttpServletRequest request, int maxBodySize) throws IOException {
        super(request);
        this.cachedBody = readBody(request, maxBodySize);
    }

    private static byte[] readBody(HttpServletRequest request, int maxBodySize) throws IOException {
        int contentLength = request.getContentLength();
        try (InputStream in = request.getInputStream()) {
            if (contentLength == 0) {
                return new byte[0];
            }
            if (contentLength > 0) {
                if (contentLength > maxBodySize) {
                    throw new IllegalStateException("Request body too large: " + contentLength + " (max " + maxBodySize + ")");
                }
                byte[] buffer = new byte[contentLength];
                int total = 0;
                while (total < contentLength) {
                    int n = in.read(buffer, total, contentLength - total);
                    if (n <= 0) break;
                    total += n;
                }
                return total == contentLength ? buffer : Arrays.copyOf(buffer, total);
            }
            // Chunked or unknown length: read up to maxBodySize
            ByteArrayOutputStream out = new ByteArrayOutputStream(Math.min(8192, maxBodySize));
            byte[] buf = new byte[8192];
            int len;
            while (out.size() < maxBodySize && (len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            return out.toByteArray();
        }
    }

    public byte[] getContentAsByteArray() {
        return cachedBody;
    }

    @Override
    public int getContentLength() {
        return cachedBody.length;
    }

    @Override
    public long getContentLengthLong() {
        return cachedBody.length;
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedBodyInputStream(cachedBody);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cachedBody), StandardCharsets.UTF_8));
    }

    private static final class CachedBodyInputStream extends ServletInputStream {
        private final ByteArrayInputStream delegate;

        CachedBodyInputStream(byte[] body) {
            this.delegate = new ByteArrayInputStream(body);
        }

        @Override
        public boolean isFinished() {
            return delegate.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public int read() {
            return delegate.read();
        }

        @Override
        public int read(byte[] b, int off, int len) {
            return delegate.read(b, off, len);
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException("Async read not supported");
        }
    }
}
