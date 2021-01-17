package com.roma.distr.service

import com.roma.distr.learner.DecideRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class LearnerService {
    var decidedValue: Int? = null
    val logger = LoggerFactory.getLogger("Learner")

    suspend fun decide(decideRequest: DecideRequest) {
        decidedValue = decideRequest.value
        logger.info(decidedValue.toString())
    }
}
