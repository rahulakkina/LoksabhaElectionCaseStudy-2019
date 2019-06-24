package com.loks.predict.service;

import com.loks.predict.dto.Education;
import reactor.core.publisher.Flux;

public interface EducationInfoService {

    Flux<Education> getEducationInfo();

}
