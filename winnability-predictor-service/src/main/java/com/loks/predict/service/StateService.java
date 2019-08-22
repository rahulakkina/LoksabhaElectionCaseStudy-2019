package com.loks.predict.service;

import com.loks.predict.dto.State;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StateService {
    Flux<State> getStatesInfo();
    Mono<State> getStateInfo(final Integer id);
}
