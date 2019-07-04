package com.loks.predict.router;

import com.loks.predict.dto.*;
import com.loks.predict.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/loksabhaElections")
public class LoksabhaElectionsInfoRouter {

    private final StateService stateService;

    private final ConstituenciesService constituenciesService;

    private final PoliticalPartyService politicalPartyService;

    private final EducationInfoService educationInfoService;

    private final PredictionService predictionService;

    private final CandidateService candidateService;

    @Autowired
    public LoksabhaElectionsInfoRouter(final CandidateService candidateService,
                                       final StateService stateService,
                                       final ConstituenciesService constituenciesService,
                                       final PoliticalPartyService politicalPartyService,
                                       final EducationInfoService educationInfoService,
                                       final PredictionService predictionService) {
        this.candidateService = candidateService;
        this.stateService = stateService;
        this.constituenciesService = constituenciesService;
        this.politicalPartyService = politicalPartyService;
        this.educationInfoService = educationInfoService;
        this.predictionService = predictionService;
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value ="/statesInfoReact", method = RequestMethod.GET, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<State> getStateInfoR(){
        return stateService.getStatesInfo();
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value ="/statesInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Flux<State> getStateInfo(){
        return stateService.getStatesInfo();
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value ="/constituenciesInfoReact", method = RequestMethod.GET, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Constituency> getConstituenciesInfoR(){
        return constituenciesService.getConstituenciesInfo();
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value ="/constituenciesInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Flux<Constituency> getConstituenciesInfo(){
        return constituenciesService.getConstituenciesInfo();
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value ="/constituenciesByStateReact", method = RequestMethod.GET, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Constituency> getConstituenciesInfoR(@RequestParam("state") final String stateName){
        return constituenciesService.getConstituenciesByState(stateName);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value ="/constituenciesByState", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Flux<Constituency> getConstituenciesInfo(@RequestParam("state") final String stateName){
        return constituenciesService.getConstituenciesByState(stateName);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value ="/politicalPartiesInfoReact", method = RequestMethod.GET, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PoliticalParty> getPoliticalPartiesInfoR(){
        return politicalPartyService.getPoliticalPartiesInfo();
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value ="/politicalPartiesInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Flux<PoliticalParty> getPoliticalPartiesInfo(){
        return politicalPartyService.getPoliticalPartiesInfo();
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value ="/educationInfoReact", method = RequestMethod.GET, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Education> getEducationInfoR(){
        return educationInfoService.getEducationInfo();
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value ="/educationInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Flux<Education> getEducationInfo(){
        return educationInfoService.getEducationInfo();
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value ="/predict", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Mono<PredictionResponse> predict(@RequestBody final PredictionParameters predictionParameters){
        return predictionService.predict(predictionParameters);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value ="/candidatesByKeyReact", method = RequestMethod.GET, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Candidate> getCandidatesByKeyWordR(@RequestParam("keyword") final String keyWord){
        return candidateService.getContestantByKeyWord(keyWord);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value ="/candidatesByKey", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Flux<Candidate> getCandidatesByKeyWord(@RequestParam("keyword") final String keyWord){
        return candidateService.getContestantByKeyWord(keyWord);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value ="/candidatesByIdReact", method = RequestMethod.GET, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<Candidate> getCandidateByCandidateIdR(@RequestParam("candidateId") final Integer candidateId){
        return candidateService.getContestantByCandidateId(candidateId);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value ="/candidatesById", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Mono<Candidate> getCandidateByCandidateId(@RequestParam("candidateId") final Integer candidateId){
        return candidateService.getContestantByCandidateId(candidateId);
    }
}
