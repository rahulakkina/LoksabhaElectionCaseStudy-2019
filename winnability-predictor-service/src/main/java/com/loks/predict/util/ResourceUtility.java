package com.loks.predict.util;

import com.google.common.base.Function;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.XGBoost;
import org.asynchttpclient.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import tech.tablesaw.api.Table;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;


public interface ResourceUtility {

    Logger logger = LoggerFactory.getLogger(ResourceUtility.class);

    <T> Mono<T> getData(final String url, final Function<ByteArrayOutputStream, T> function);

    <T> Mono<T> getData(final String url, final Map<String, String> params, final Function<ByteArrayOutputStream, T> function);

    default <T> Mono<T> mono(final ListenableFuture<T> listenableFuture){
        return Mono.fromFuture(listenableFuture.toCompletableFuture());
    }

    default Function<ByteArrayOutputStream, Table> getTableFunction(){
        return new Function<ByteArrayOutputStream, Table>(){
            @Nullable
            @Override
            public Table apply(@Nullable final ByteArrayOutputStream byteArrayOutputStream) {
                try {
                    return Table.read().csv(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                }catch(Exception e){
                    logger.error(e.getMessage(), e);
                }
                return Table.create();
            }
        };
    }

    default Function<ByteArrayOutputStream, Booster> getBoosterFunction(){
        return new Function<ByteArrayOutputStream, Booster>(){
            @Nullable
            @Override
            public Booster apply(@Nullable final ByteArrayOutputStream byteArrayOutputStream) {
                try {
                    return XGBoost.loadModel(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                }catch(Exception e){
                    logger.error(e.getMessage(), e);
                }
                return null;
            }
        };
    }
}
