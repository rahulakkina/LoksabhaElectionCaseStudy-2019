package com.loks.predict.configuration;

import com.loks.predict.util.ResourceUtility;
import com.loks.predict.util.impl.AsyncFileResourceUtility;
import com.loks.predict.util.impl.AsyncHttpResourceUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({"classpath*:context.xml"})
public class ApplicationConfiguration {

    @Value("${resource.parent-uri}")
    private String parentUrl;

    @Value("${resource.root-dir}")
    private String rootDir;

    @Value("${resource.fetch-online}")
    private Boolean fetchOnline;

    @Autowired
    @Qualifier("asyncHttpResourceUtility")
    private ResourceUtility asyncHttpResourceUtility;

    @Autowired
    @Qualifier("asyncFileResourceUtility")
    private ResourceUtility asyncFileResourceUtility;

    @Bean
    @Qualifier("resourceUtility")
    public ResourceUtility getResourceUtility(){
        return fetchOnline ?
                asyncHttpResourceUtility : asyncFileResourceUtility;
    }

    @Bean
    @Qualifier("parentUrl")
    public String getParentUrl(){
        return fetchOnline ? parentUrl : rootDir;
    }


}
