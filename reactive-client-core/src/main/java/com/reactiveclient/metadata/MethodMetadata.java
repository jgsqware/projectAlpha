package com.reactiveclient.metadata;

import com.reactiveclient.metadata.request.RequestHeader;
import com.reactiveclient.metadata.request.RequestHeaders;
import com.reactiveclient.metadata.request.ReactiveRequestTemplate;
import lombok.Getter;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Getter
public class MethodMetadata {
    private Method targetMethod;
    private ResolvableType responseType;
    private ResolvableType bodyType;
    private ReactiveRequestTemplate reactiveRequestTemplate;

    private MethodMetadata(Builder builder) {
        targetMethod = builder.targetMethod;
        responseType = builder.returnType;
        bodyType = builder.bodyType;
        reactiveRequestTemplate = new ReactiveRequestTemplate(
                builder.uriBuilder,
                builder.httpMethod,
                new RequestHeaders(builder.headers, builder.headerIndexToName),
                builder.bodyIndex,
                builder.variableIndexToName);
    }

    public static Builder newBuilder(URI baseUri) {
        return new Builder(baseUri.getScheme(), baseUri.getAuthority());
    }

    public static Builder newBuilder(MethodMetadata other) {
        return new Builder(other);
    }

    public static class Builder {
        private UriBuilder uriBuilder;
        private MultiValueMap<Integer, String> variableIndexToName;
        private Map<String, RequestHeader> headers;
        private Map<Integer, String> headerIndexToName;
        private HttpMethod httpMethod;
        private Method targetMethod;
        private Integer bodyIndex;
        private ResolvableType returnType;
        private ResolvableType bodyType;

        private Builder() {
            variableIndexToName = new LinkedMultiValueMap<>();
            headers = new HashMap<>();
            headerIndexToName = new HashMap<>();
        }

        public Builder(String scheme, String authority) {
            this();
            if (scheme != null && authority != null) {
                uriBuilder = new DefaultUriBuilderFactory(scheme + "://" + authority).builder();
            } else {
                uriBuilder = new DefaultUriBuilderFactory().builder();
            }
        }

        public Builder(MethodMetadata other) {
            this();
            uriBuilder = new DefaultUriBuilderFactory(other.getReactiveRequestTemplate().getUriBuilder().build().toString()).builder();
            variableIndexToName.putAll(other.getReactiveRequestTemplate().getVariableIndexToName());
            headers.putAll(other.getReactiveRequestTemplate().getRequestHeaders().getHeaders());
            headerIndexToName.putAll(other.getReactiveRequestTemplate().getRequestHeaders().getIndexToName());
            httpMethod = other.getReactiveRequestTemplate().getHttpMethod();
            targetMethod = other.getTargetMethod();
        }

        public Builder addPath(String path) {
            uriBuilder.path(path);
            return this;
        }

        public Builder addPathIndex(Integer index, String pathVariable) {
            this.variableIndexToName.add(index, pathVariable);
            return this;
        }

        public Builder addHeader(String name, String value) {
            headers.put(name, new RequestHeader.BasicRequestHeader(name, value));
            return this;
        }

        public Builder addHeader(Integer index, String name) {
            headers.put(name, new RequestHeader.DynamicRequestHeader(name));
            headerIndexToName.put(index, name);
            return this;
        }

        public Builder addParameter(Integer index, String name) {
            variableIndexToName.add(index, name);
            uriBuilder.query(name + "={" + name + "}");
            return this;
        }

        public Builder httpMethod(HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder body(Integer bodyIndex, Type bodyType) {
            if (this.bodyType != null && this.bodyIndex != null) {
                throw new IllegalArgumentException();
            }

            this.bodyIndex = bodyIndex;
            this.bodyType = ResolvableType.forType(bodyType);
            return this;
        }

        public Builder targetMethod(Method targetMethod) {
            this.targetMethod = targetMethod;
            this.returnType = ResolvableType.forMethodReturnType(targetMethod);
            return this;
        }

        public MethodMetadata build() {
            return new MethodMetadata(this);
        }
    }
}
