package com.loks.predict.service;

import com.loks.predict.dto.ConstituencyResult;
import com.loks.predict.dto.PredictionParameters;
import com.loks.predict.dto.PredictionVector;
import ml.dmlc.xgboost4j.java.Booster;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PredictorService {
    <T> T find(final Integer id, final Class<T> cl);
    PredictionVector build(final PredictionParameters predictionParameters);
    List<ConstituencyResult> getConstituencyResults(final Integer constituencyId);
    Mono<Booster> getModel();
}
