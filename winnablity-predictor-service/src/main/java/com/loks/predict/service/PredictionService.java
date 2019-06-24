package com.loks.predict.service;

import com.loks.predict.dto.PredictionParameters;
import com.loks.predict.dto.PredictionResponse;
import reactor.core.publisher.Mono;

public interface PredictionService {
    Mono<PredictionResponse> predict(final PredictionParameters predictionParameters);
}
