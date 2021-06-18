package com.loks.predict.service.impl;

import com.loks.predict.dto.ConstituencyResult;
import com.loks.predict.dto.PredictionParameters;
import com.loks.predict.dto.PredictionResponse;
import com.loks.predict.dto.PredictionVector;
import com.loks.predict.service.PredictionService;
import com.loks.predict.service.PredictorService;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


import java.math.BigDecimal;
import java.util.List;


@Component
public class PredictionServiceImpl implements PredictionService {

    private static final Logger logger = LoggerFactory.getLogger(PredictionServiceImpl.class);

    private final PredictorService predictorService;

    @Autowired
    public PredictionServiceImpl(final PredictorService predictorService) {
        this.predictorService = predictorService;
    }

    /**
     *
     * @param predictionParameters
     * @return
     */
    @Override
    public Mono<PredictionResponse> predict(
            final PredictionParameters predictionParameters) {

        final PredictionVector predictionVector = predictorService.build(predictionParameters);
        final Mono<Booster> model = predictorService.getModel();

        return model
                .flatMap(
                         booster -> {
                            final PredictionResponse result = new PredictionResponse();
                            try {

                                final Long startTime = System.currentTimeMillis();

                                final float[][] prediction = booster.predict(
                                        new DMatrix(predictionVector.getVector(), 1, predictionVector.vectorSize()));

                                final Double score = BigDecimal.valueOf(prediction[0][0]).doubleValue();

                                result.setScore(score);

                                final List<ConstituencyResult> rankings =
                                        predictorService.getConstituencyResults(
                                                predictionParameters.getConstituencyId());

                                rankings.add(
                                        new ConstituencyResult(-1,
                                                predictionParameters.getCandidateName(), score, true));

                                rankings.sort((cr1, cr2) ->
                                        cr2.getVotingPercentage().compareTo(cr1.getVotingPercentage()));

                                result.setRankings(rankings);

                                logger.info(String.format(
                                        "Constituency Result prediction for the Candidate : '%s' - completed in %d ms",
                                        predictionParameters.toString(), (System.currentTimeMillis() - startTime)));

                            } catch (final XGBoostError xgBoostError) {
                                logger.error(xgBoostError.getMessage(), xgBoostError);
                            }

                           return Mono.just(result);
                        });
    }



}
