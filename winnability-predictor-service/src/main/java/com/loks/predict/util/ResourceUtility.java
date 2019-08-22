package com.loks.predict.util;

import com.google.common.base.Function;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.XGBoost;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.asynchttpclient.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.xml.sax.InputSource;
import reactor.core.publisher.Mono;
import tech.tablesaw.api.Table;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
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

    static Double getFloatAsDouble(float value) {
        return Double.valueOf(Float.valueOf(value).toString()).doubleValue();
    }

    static Integer getMediaPopularityScore(final String searchQ, final String newsUri,
                                          final Boolean useProxy, final String proxyHost, final Integer proxyPort){

        final Long startTime = System.currentTimeMillis();

        try {
            final URI uri = new URIBuilder().setPath(newsUri)
                    .addParameter("q", String.format("\"%s\"", searchQ))
                    .addParameter("hl", "en-SG")
                    .addParameter("gl", "SG")
                    .addParameter("ceid", "SG:en").build();

            Request request = Request.Get(uri).setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            if(useProxy) {
                request = request.viaProxy(new HttpHost(proxyHost, proxyPort));
            }

            final String feedContent = request.execute().returnContent().asString();

            if (StringUtils.isNotBlank(feedContent)) {
                final SyndFeed feed = new SyndFeedInput().build(new InputSource(new StringReader(feedContent)));

                return (feed != null && !CollectionUtils.isEmpty(feed.getEntries())) ? feed.getEntries().size() : 0;
            }

        }catch(IOException | URISyntaxException | FeedException ie){
            logger.error(ie.getMessage(), ie);
        }finally{
            logger.info(String.format("Performed news search for the Candidate : '%s' - completed in %d ms",
                    searchQ, (System.currentTimeMillis() - startTime)));
        }

        return 0;
    }
}
