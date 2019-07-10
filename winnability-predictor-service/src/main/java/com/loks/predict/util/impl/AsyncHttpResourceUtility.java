package com.loks.predict.util.impl;


import com.google.common.base.Function;
import com.loks.predict.util.ResourceUtility;
import org.asynchttpclient.*;
import org.asynchttpclient.proxy.ProxyServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Map;



public class AsyncHttpResourceUtility implements ResourceUtility {

    private final Integer proxyPort;

    private final String proxyHost;

    private final Boolean useProxy;

    public AsyncHttpResourceUtility(final Boolean useProxy, final String proxyHost, final Integer proxyPort) {
        this.proxyPort = proxyPort;
        this.proxyHost = proxyHost;
        this.useProxy = useProxy;
    }

    public <T> Mono<T> getData(final String url, final Function<ByteArrayOutputStream, T> function){
        final AsyncHttpClient client =
                useProxy ?
                        (Dsl.asyncHttpClient(new DefaultAsyncHttpClientConfig.Builder()
                                .setProxyServer(new ProxyServer
                                        .Builder(proxyHost, proxyPort)).build()))
                        : Dsl.asyncHttpClient();

        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final WritableByteChannel wbc = Channels.newChannel(stream);
        return mono(
                client.prepareGet(url).execute(new AsyncCompletionHandler<T>() {

                    @Override
                    public State onBodyPartReceived(final HttpResponseBodyPart bodyPart) throws Exception {
                        wbc.write(bodyPart.getBodyByteBuffer());
                        return State.CONTINUE;
                    }

                    @Override
                    public T onCompleted(final Response response) throws Exception {
                        return function.apply(stream);
                    }
                }));
    }

    public <T> Mono<T> getData(final String url, final Map<String, String> params, final Function<ByteArrayOutputStream, T> function){
        final AsyncHttpClient client =
                useProxy ?
                        (Dsl.asyncHttpClient(new DefaultAsyncHttpClientConfig.Builder()
                                .setProxyServer(new ProxyServer
                                        .Builder(proxyHost, proxyPort)).build()))
                        : Dsl.asyncHttpClient();

        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final WritableByteChannel wbc = Channels.newChannel(stream);


        BoundRequestBuilder boundRequestBuilder = client.prepareGet(url);

        for(Map.Entry<String, String> entry : params.entrySet()){
            boundRequestBuilder = boundRequestBuilder.addQueryParam(entry.getKey(), entry.getValue());
        }

        return mono(
                boundRequestBuilder.execute(new AsyncCompletionHandler<T>() {

                    @Override
                    public State onBodyPartReceived(final HttpResponseBodyPart bodyPart) throws Exception {
                        wbc.write(bodyPart.getBodyByteBuffer());
                        return State.CONTINUE;
                    }

                    @Override
                    public T onCompleted(final Response response) throws Exception {
                        return function.apply(stream);
                    }
                }));
    }

}
