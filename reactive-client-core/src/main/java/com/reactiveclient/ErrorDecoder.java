package com.reactiveclient;

import org.springframework.http.HttpStatus;

import java.io.InputStream;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface ErrorDecoder<T extends RuntimeException>{

    boolean canDecode(HttpStatus httpStatus);

    T decode(HttpStatus httpStatus, InputStream inputStream);


    static <T extends RuntimeException> ErrorDecoder<T> of(Predicate<HttpStatus> statusPredicate,
                           BiFunction<HttpStatus, InputStream, T> errorDecoder) {
        return new ErrorDecoder<T>() {
            @Override
            public boolean canDecode(HttpStatus httpStatus) {
                return statusPredicate.test(httpStatus);
            }

            @Override
            public T decode(HttpStatus httpStatus, InputStream inputStream) {
                return errorDecoder.apply(httpStatus, inputStream);
            }
        };
    }
}
