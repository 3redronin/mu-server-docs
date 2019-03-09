package io.muserver.docs.handlers;

import io.muserver.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ViewCacheHandler implements MuHandler {
    private static final Logger log = LoggerFactory.getLogger(ViewCacheHandler.class);
    private final Date lastModifiedDate = new Date((System.currentTimeMillis() / 1000) * 1000);
    private final String lastModified = Mutils.toHttpDate(lastModifiedDate);

    private final List<String> paths;

    public ViewCacheHandler(String... paths) {
        this.paths = Arrays.asList(paths);
    }

    @Override
    public boolean handle(MuRequest request, MuResponse response) {

        if (paths.contains(request.relativePath())) {
            response.headers().set(HeaderNames.CACHE_CONTROL, "must-revalidate, max-age=600");
            String imsv = request.headers().get(HeaderNames.IF_MODIFIED_SINCE);
            if (imsv != null) {
                try {
                    long since = Mutils.fromHttpDate(imsv).getTime() / 1000;
                    long lastModifiedSeconds = lastModifiedDate.getTime() / 1000;
                    if (lastModifiedSeconds <= since) {
                        response.status(304);
                        return true;
                    }
                } catch (DateTimeParseException ignored) {
                    log.info("Invalid " + HeaderNames.IF_MODIFIED_SINCE + " value, so ignored: " + imsv);
                }
            }
            response.headers().set(HeaderNames.LAST_MODIFIED, lastModified);
        }
        return false;
    }
}
