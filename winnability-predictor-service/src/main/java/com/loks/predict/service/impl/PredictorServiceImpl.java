package com.loks.predict.service.impl;

import com.loks.predict.dao.PredictorDao;
import com.loks.predict.dto.*;
import com.loks.predict.service.*;
import ml.dmlc.xgboost4j.java.Booster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.tablesaw.api.Table;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class PredictorServiceImpl implements PredictorService {

    private static final Logger logger = LoggerFactory.getLogger(PredictorServiceImpl.class);

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


    @Override
    public <T> T find(final Integer id, final Class<T> cl) {
        if(State.class == cl){
            return (T)stateService.getStatesInfo()
                    .filter(state -> state.getId() == id)
                    .last()
                    .block();
        }

        if(Constituency.class == cl){
            return (T)constituenciesService.getConstituenciesInfo()
                    .filter(constituency -> constituency.getId() == id)
                    .last()
                    .block();
        }

        if(PoliticalParty.class == cl){
            return (T)politicalPartyService
                    .getPoliticalParty(id)
                    .block();
        }

        if(Candidate.class == cl){
            return (T)candidateService.getContestantByCandidateId(id)
                    .block();
        }


        return null;
    }

    @Override
    public PredictionVector build(final PredictionParameters predictionParameters) {
        final State state = find(predictionParameters.getStateId(), State.class);

        predictionParameters.setStateSeatShare((double)state.getSeatShare());
        predictionParameters.setStateLiteracyRate((double)state.getLiteracyRate());
        predictionParameters.setNumberOfPhases(state.getNoOfPhases());
        predictionParameters.setDeltaStateVoterTurnout((double)state.getDeltaVoterTurnout());

        final PoliticalParty politicalParty = find(predictionParameters.getPartyId(), PoliticalParty.class);

        predictionParameters.setPartyGroupId(politicalParty.getPoints());

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

        final Table candidateAnalysed = predictorDao.getDatasets().get("candidate-analysed").block();

        final List<ConstituencyResult> candidates = StreamSupport.stream(candidateAnalysed.spliterator(), false)
                .filter(row -> constituencyId.compareTo(row.getInt("CONSTITUENCY_INDEX")) == 0)
                .map(row -> new ConstituencyResult(row.getInt("CANDIDATE_ID"),
                        row.getString("CANDIDATE_NAME"),
                        row.getDouble("VOTING_PERCENTAGE")))
                .collect(Collectors.toList());

        return candidates;
    }

    public Mono<Booster> getModel(){
        return predictorDao.getModel();
    }
}
