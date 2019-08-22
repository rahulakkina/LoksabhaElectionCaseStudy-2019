package com.loks.predict.service;

import com.loks.predict.dto.Constituency;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ConstituenciesService {

    Flux<Constituency> getConstituenciesInfo();

    Flux<Constituency> getConstituenciesByState(final String stateName);

    Mono<Constituency> getConstituencyInfo(final Integer id);
}
