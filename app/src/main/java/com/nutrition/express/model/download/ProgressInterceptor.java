package com.nutrition.express.model.download;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ProgressInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response originalResponse = chain.proceed(request);
        if (request.tag() instanceof ProgressResponseBody.ProgressListener) {
            return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body(),
                            (ProgressResponseBody.ProgressListener) request.tag()))
                    .build();
        }
        return originalResponse;
    }
}
