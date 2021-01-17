package com.roma.distr.service

import com.roma.distr.acceptor.*
import com.roma.distr.grpc.AcceptRequestGrpc
import com.roma.distr.grpc.AcceptResponseGrpc
import com.roma.distr.grpc.AcceptServiceGrpc
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AcceptorService {
    private var acceptedResponse: Int? = null
    private var currentProposalNumber: ProposalNumber? = null
    private val acceptorMutex = Mutex()
    private val logger = LoggerFactory.getLogger("Acceptor")

    private val URL = "localhost"

    suspend fun prepare(prepareRequest: PrepareRequest): PrepareResponse {
        if (currentProposalNumber == null || prepareRequest.proposalNumber > currentProposalNumber) {
            logger.info("Prepare request accepted")

            return PrepareResponse.PromiseAccepted(prepareRequest.proposalNumber)
        } else {
            logger.info("Prepare request rejected")

            return PrepareResponse.Reject(currentProposalNumber!!)
        }

    }

    suspend fun accept(acceptRequest: AcceptRequest): AcceptResponse {
        return acceptorMutex.withLock {
            if (acceptRequest.proposalNumber >= currentProposalNumber) {
                currentProposalNumber = acceptRequest.proposalNumber
                acceptedResponse = acceptRequest.value

                AcceptResponse.Accept
            } else {
                AcceptResponse.Reject
            }
        }
    }
}
