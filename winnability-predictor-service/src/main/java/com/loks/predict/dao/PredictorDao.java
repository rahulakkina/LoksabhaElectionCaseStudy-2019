package com.loks.predict.dao;

import ml.dmlc.xgboost4j.java.Booster;
import reactor.core.publisher.Mono;
import tech.tablesaw.api.Table;

import java.util.Map;

public interface PredictorDao {
    Map<String, Mono<Table>> getDatasets();
    Mono<Booster> getModel();
    Map<Integer, Integer> getAgeGroupMappings();
    Map<Integer, Double> getAgeGroupedEarningsMappings();
}
