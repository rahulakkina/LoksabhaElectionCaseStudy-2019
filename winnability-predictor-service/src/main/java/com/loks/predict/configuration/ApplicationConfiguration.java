package com.loks.predict.configuration;

import com.loks.predict.util.ResourceUtility;
import com.loks.predict.util.impl.AsyncFileResourceUtility;
import com.loks.predict.util.impl.AsyncHttpResourceUtility;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({"classpath*:context.xml"})
public class ApplicationConfiguration {

    @Value("${http.use-proxy}")
    private Boolean useProxy;

    @Value("${http.proxy.host}")
    private String proxyHost;

    @Value("${http.proxy.port}")
    private Integer proxyPort;

    @Value("${resource.parent-uri}")
    private String parentUrl;

    @Value("${resource.root-dir}")
    private String rootDir;

    @Value("${resource.fetch-online}")
    private Boolean fetchOnline;

    @Bean
    public ResourceUtility getResourceUtility(){
        return fetchOnline ?
                new AsyncHttpResourceUtility(useProxy, proxyHost, proxyPort) : new AsyncFileResourceUtility();
    }

    @Bean
    @Qualifier("parentUrl")
    public String getParentUrl(){
        return fetchOnline ? parentUrl : rootDir;
    }


}
