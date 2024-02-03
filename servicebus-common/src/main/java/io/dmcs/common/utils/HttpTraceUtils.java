package io.dmcs.common.utils;


import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpTraceUtils {

    private HttpTraceUtils() {
    }

    public static void traceHttpExchange(TraceData traceData) {

        StringBuilder sb = new StringBuilder("\n\t>> ")
                .append(traceData.methodName).append(" ").append(traceData.uri).append("\n");

        if (traceData.requestHeaders.size() > 0) {
            sb.append("\t>> Headers: ");
            List<String> headers = new ArrayList<>();
            traceData.requestHeaders.forEach((key, value) -> {
                if (!StringUtils.equals(key, "Authorization"))
                    headers.add(key + " = " + value);
            });
            sb.append(headers).append("\n");
        }

        if (traceData.requestBody != null) {
            sb.append("\t>> Body: ");
            traceBody(sb, traceData.getRequestHeaders().get("Content-Type"), traceData.requestBody);
            sb.append("\n");
        }

        if (traceData.responseHeaders.size() > 0) {
            sb.append(String.format("\t<< %s Headers: ", traceData.responseCode));
            List<String> headers = new ArrayList<>();
            traceData.responseHeaders.forEach((key, value) -> {
                if (!StringUtils.equals(key, "Authorization"))
                    headers.add(key + " = " + value);
            });
            sb.append(headers).append("\n");
        }

        if (traceData.responseBody != null && traceData.responseBody.length > 0) {
            sb.append("\t<< Body: ");
            traceBody(sb, traceData.getResponseHeaders().get("Content-Type"), traceData.responseBody);
            sb.append("\n");
        }

        traceData.log.debug(sb.toString()); // NOSONAR
    }

    private static void traceBody(StringBuilder sb, String contentType, byte[] body) {

        if (body == null)
            return;

        if (isBinaryContent(contentType))
            sb.append("<binary>");
        else
            sb.append(new String(body));
    }

    @Getter
    @Builder
    public static class TraceData {
        protected Logger log;
        protected String methodName;
        protected String uri;
        protected String requestContentType;
        protected Map<String, String> requestHeaders;
        protected Throwable error;
        protected byte[] requestBody;
        protected int responseCode;
        protected Map<String, String> responseHeaders;
        protected byte[] responseBody;

    }

    public static boolean isBinaryContent(String contentType) {

        if (StringUtils.isEmpty(contentType))
            return false;

        if (contentType.contains("stream"))
            return true;

        return !contentType.contains("text") && !contentType.contains("json");
    }

}
