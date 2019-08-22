package com.loks.predict.service.impl;

import com.loks.predict.dao.PredictorDao;
import com.loks.predict.dto.*;
import com.loks.predict.service.*;
import ml.dmlc.xgboost4j.java.Booster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import static com.loks.predict.util.ResourceUtility.*;

@Component
public class PredictorServiceImpl implements PredictorService {

    @Autowired
    private StateService stateService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private ConstituenciesService constituenciesService;

    @Autowired
    private PoliticalPartyService politicalPartyService;

    @Autowired
    private PredictorDao predictorDao;

    @Value("${resource.news-uri}")
    private String newsUri;

    @Value("${http.use-proxy}")
    private Boolean useProxy;

    @Value("${http.proxy.host}")
    private String proxyHost;

    @Value("${http.proxy.port}")
    private Integer proxyPort;

    /**
     *
     * @param predictionParameters
     * @return
     */
    @Override
    public PredictionVector build(final PredictionParameters predictionParameters) {

        final PoliticalParty politicalParty = (predictionParameters.getPartyId() != null) ?
                    politicalPartyService.getPoliticalParty(predictionParameters.getPartyId()).block() :
                    politicalPartyService.getPoliticalParty("IND").block();

        predictionParameters.setPartyId(politicalParty.getId());
        predictionParameters.setPartyGroupId(politicalParty.getPoints());

        final State state = predictionParameters.getStateId() != null ?
                        stateService.getStateInfo(predictionParameters.getStateId()).block() :
                        stateService.getStateInfo(
                                constituenciesService
                                        .getConstituencyInfo(predictionParameters.getStateId())
                                        .block()
                                        .getStateName()
                        ).block();

        predictionParameters.setStateId(state.getId());
        predictionParameters.setStateSeatShare(getFloatAsDouble(state.getSeatShare()));
        predictionParameters.setStateLiteracyRate(getFloatAsDouble(state.getLiteracyRate()));
        predictionParameters.setNumberOfPhases(state.getNoOfPhases());
        predictionParameters.setDeltaStateVoterTurnout(getFloatAsDouble(state.getDeltaVoterTurnout()));

        predictionParameters.setRecontest(predictionParameters.getRecontest() != null ?
                predictionParameters.getRecontest() : false);

        predictionParameters.setSex(predictionParameters.getSex() != null ?
                predictionParameters.getSex() : false);

        final Integer numberOfMediaItems =
                    (predictionParameters.getNumberOfMediaItems() == null) ?
                            getMediaPopularityScore(predictionParameters.getCandidateName(),
                                    newsUri, useProxy, proxyHost, proxyPort) :
                            predictionParameters.getNumberOfMediaItems();

        predictionParameters.setNumberOfMediaItems(numberOfMediaItems);

        return transform(predictionParameters);
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
    public Integer calculateEarningPoints(final Integer age, final Double earnings){
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
    public List<ConstituencyResult> getConstituencyResults(final Integer constituencyId){
        return constituenciesService.getConstituencyResults(constituencyId);
    }

    public Mono<Booster> getModel(){
        return predictorDao.getModel();
    }
}
