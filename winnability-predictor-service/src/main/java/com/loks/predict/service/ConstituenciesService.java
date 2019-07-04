package com.loks.predict.service;

import com.loks.predict.dto.Constituency;
import reactor.core.publisher.Flux;

public interface ConstituenciesService {

    Flux<Constituency> getConstituenciesInfo();

    Flux<Constituency> getConstituenciesByState(final String stateName);

}
