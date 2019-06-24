package com.loks.predict.service;

import com.loks.predict.dto.State;
import reactor.core.publisher.Flux;

public interface StateService {
    public Flux<State> getStatesInfo();
}
