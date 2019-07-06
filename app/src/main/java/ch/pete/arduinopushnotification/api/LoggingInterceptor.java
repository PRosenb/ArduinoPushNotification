package ch.pete.arduinopushnotification.api;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Buffer;
import timber.log.Timber;

import java.io.IOException;
import java.net.URLDecoder;

/**
 * Prints request and response URL and data to console.
 * Created by peterrosenberg on 14/06/2017.
 */
public class LoggingInterceptor implements Interceptor {
    private static final boolean LOG_REQUEST = true;
    private static final boolean LOG_RESPONSE = true;
    private static final boolean LOG_RESPONSE_HEADERS = false;

    public static String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "Could not convert body to string";
        }
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long t1 = System.currentTimeMillis();
        if (LOG_REQUEST) {
            StringBuilder requestLog = new StringBuilder();
            requestLog.append("Sending request\n");
            requestLog.append(request.url());
            if (chain.connection() != null) {
                requestLog.append("\n on ");
                requestLog.append(chain.connection());
            }
            requestLog.append(request.headers());

            if (request.method().compareToIgnoreCase("post") == 0) {
                requestLog.append("\n");
                requestLog.append(bodyToString(request));
            }
            Timber.d(requestLog.toString());
        }
        okhttp3.Response response = chain.proceed(request);
        long t2 = System.currentTimeMillis();
        String bodyString = response.body().string();

        if (LOG_RESPONSE) {
            StringBuilder responseLog = new StringBuilder();
            responseLog.append("Received response for\n");
            responseLog.append(URLDecoder.decode(response.request().url().toString()));
            responseLog.append("\nin ");
            responseLog.append(t2 - t1);
            responseLog.append("ms");
            if (LOG_RESPONSE_HEADERS) {
                responseLog.append(response.headers());
            }
            responseLog.append("\n");
            responseLog.append(bodyString);

            Timber.d(responseLog.toString());
        }

        return response.newBuilder()
                .body(ResponseBody.create(response.body().contentType(), bodyString))
                .build();
    }
}
