package com.reactiveclient.client;

import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

class DefaultExtendedExchangeStrategies implements ExtendedExchangeStrategies {

    private List<HttpExceptionReader> exceptionReaders;

    private ExchangeStrategies exchangeStrategies;

    DefaultExtendedExchangeStrategies(List<HttpExceptionReader> exceptionReaders) {
        this.exceptionReaders = unmodifiableCopy(exceptionReaders);
        this.exchangeStrategies = ExchangeStrategies.withDefaults();
    }

    private <T> List<T> unmodifiableCopy(List<? extends T> list) {
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    @Override
    public Supplier<Stream<HttpExceptionReader>> exceptionReader() {
        return exceptionReaders::stream;
    }

    @Override
    public Supplier<Stream<HttpMessageReader<?>>> messageReaders() {
        return exchangeStrategies.messageReaders();
    }

    @Override
    public Supplier<Stream<HttpMessageWriter<?>>> messageWriters() {
        return exchangeStrategies.messageWriters();
    }
}
