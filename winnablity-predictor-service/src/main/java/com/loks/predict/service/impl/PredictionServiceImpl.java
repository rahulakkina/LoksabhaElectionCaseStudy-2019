package com.loks.predict.service.impl;

import com.google.common.collect.Lists;
import com.loks.predict.dao.PredictorDao;
import com.loks.predict.dto.ConstituencyResult;
import com.loks.predict.dto.PredictionParameters;
import com.loks.predict.dto.PredictionResponse;
import com.loks.predict.dto.PredictionVector;
import com.loks.predict.service.PredictionService;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.tablesaw.api.Table;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class PredictionServiceImpl implements PredictionService {

    private static final Logger logger = LoggerFactory.getLogger(PredictionServiceImpl.class);

    private final PredictorDao predictorDao;

    @Autowired
    public PredictionServiceImpl(final PredictorDao predictorDao) {
        this.predictorDao = predictorDao;
    }

    /**
     *
     * @param predictionParameters
     * @return
     */
    @Override
    public Mono<PredictionResponse> predict(final PredictionParameters predictionParameters) {
        final PredictionVector predictionVector = transform(predictionParameters);
        final Mono<Booster> model = predictorDao.getModel();
        return model.flatMap(new Function<Booster, Mono<PredictionResponse>>() {
            @Override
            public Mono<PredictionResponse> apply(final Booster booster) {
                final PredictionResponse result = new PredictionResponse();
                try {

                    final Long startTime = System.currentTimeMillis();

                    final float prediction[][] = booster.predict(
                            new DMatrix(predictionVector.getVector(), 1, predictionVector.vectorSize()));

                    final Double score = Precision.round(new BigDecimal(prediction[0][0]).doubleValue(), 4);

                    result.setScore(score);

                    final List<ConstituencyResult> rankings = getConstituencyResults(predictionParameters.getConstituencyId());

                    rankings.add(new ConstituencyResult(predictionParameters.getCandidateName(), score, true));

                    Collections.sort(rankings, (cr1,cr2) -> cr2.getVotingPercentage().compareTo(cr1.getVotingPercentage()));

                    result.setRankings(rankings);

                    logger.info(String.format(
                            "Constituency Result prediction for the Candidate : '%s' - completed in %d ms",
                            predictionParameters.getCandidateName(), (System.currentTimeMillis() - startTime)));

                } catch (final XGBoostError xgBoostError) {
                    logger.error(xgBoostError.getMessage(), xgBoostError);
                }

               return Mono.just(result);
            }
        });
    }

    /**
     *
     * @param predictionParameters
     * @return
     */
    protected PredictionVector transform(final PredictionParameters predictionParameters){
        final PredictionVector predictionVector = new PredictionVector();

        predictionVector.setAgeGroupId(predictorDao.getAgeGroupMappings().get(predictionParameters.getAge()));
        predictionVector.setConstituencyId(predictionParameters.getConstituencyId());
        predictionVector.setStateId(predictionParameters.getStateId());
        predictionVector.setPartyId(predictionParameters.getPartyId());
        predictionVector.setNumberOfPendingCriminalCases(predictionParameters.getNumberOfPendingCriminalCases());
        predictionVector.setEarningPoints(calculateEarningPoints(predictionParameters.getAge(),
                (predictionParameters.getEarnedIncome() - predictionParameters.getLiabilities())));
        predictionVector.setStateLiteracyRate(predictionParameters.getStateLiteracyRate());
        predictionVector.setStateSeatShare(predictionParameters.getStateSeatShare());
        predictionVector.setPartyGroupId(predictionParameters.getPartyGroupId());
        predictionVector.setEducationGroupId(predictionParameters.getEducationGroupId());
        predictionVector.setDeltaStateVoterTurnout(predictionParameters.getDeltaStateVoterTurnout());
        predictionVector.setNumberOfPhases(predictionParameters.getNumberOfPhases());
        predictionVector.setRecontest(predictionParameters.getRecontest());
        predictionVector.setSex(predictionParameters.getSex());
        predictionVector.setNumberOfMediaItems(predictionParameters.getNumberOfMediaItems());

        return predictionVector;
    }

    /**
     *
     * @param age
     * @param earnings
     * @return
     */
    protected Integer calculateEarningPoints(final Integer age, final Double earnings){
        final Integer ageGroup = predictorDao.getAgeGroupMappings().get(age);

        if(ageGroup == null){
            return 0;
        }

        final Double ageGroupAverageEarnings = predictorDao.getAgeGroupedEarningsMappings().get(ageGroup);

        if(ageGroupAverageEarnings == null){
            return 0;
        }

        return (earnings >= (ageGroupAverageEarnings - 0.5 * ageGroupAverageEarnings)
                && earnings <= (ageGroupAverageEarnings + 0.5 * ageGroupAverageEarnings)) ? 1 : 0;

    }

    /**
     *
     * @param constituencyId
     * @return
     */
    protected List<ConstituencyResult> getConstituencyResults(final Integer constituencyId){

        final Table candidateAnalysed = predictorDao.getDatasets().get("candidate-analysed").block();

        final List<ConstituencyResult> candidates = StreamSupport.stream(candidateAnalysed.spliterator(), false)
                .filter(row -> constituencyId.compareTo(row.getInt("CONSTITUENCY_INDEX")) == 0)
                .map(row -> new ConstituencyResult(row.getString("CANDIDATE_NAME"),
                        Precision.round(row.getDouble("VOTING_PERCENTAGE"), 4)))
                .collect(Collectors.toList());

        return candidates;
    }

}
