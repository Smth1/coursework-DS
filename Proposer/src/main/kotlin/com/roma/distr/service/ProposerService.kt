package com.roma.distr.service

import com.roma.distr.grpc.*
import com.roma.distr.proposer.*
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Service
import kotlin.math.floor

@Service
class ProposerService {
    private val proposerId : Int = 0
    val acceptors  = mutableListOf<Int>(6551, 6552, 6553)
    val learners = mutableListOf<Int>(6565)
    private val consensus get() = (floor(acceptors.size / 2f) + 1).toInt()

    private val URL = "localhost"
    private val proposerMutex = Mutex()
    private var roundCount = 0

    suspend fun propose(proposeRequest: ProposeRequest): ProposeResponse {

        proposerMutex.withLock {
            while (!Thread.currentThread().isInterrupted) {
                roundCount ++
                val roundIdentifier = ProposalNumber(roundCount, proposerId)
                var countPromises = 0


                // Phase 1: Prepare
                val prepareResponses = acceptors.map { GlobalScope.launch {
                    val res = sendPrepare(
                        PrepareRequest(roundIdentifier), it)
                    if (res is PrepareResponse.PromiseAccepted) {
                        countPromises++
                    }

                }
                 }.listIterator().forEach { it.join() }

                println(prepareResponses)

//                val roundHighMark = prepareResponses
//                        .filter { it is PrepareResponse.Reject }.map { it as PrepareResponse.Reject }
//                        .maxByOrNull { it.proposalNumber }
//                        ?.proposalNumber?.round
//
//                if (roundHighMark != null) {
//                    if (roundHighMark < consensus) {
//                        roundCount = roundHighMark
//                        continue
//                    }
//                }

                val acceptedValue = proposeRequest.value

                // Phase 2: Accept
                val agreedNodes = acceptors
                        .map { sendAccept(AcceptRequest(roundIdentifier, acceptedValue), it) }
                        .filter { it is AcceptResponse.Accept }
                        .size

                if (agreedNodes >= consensus) {
                    learners.forEach { sendDecide(DecideRequest(acceptedValue), it) }
                    return ProposeResponse(acceptedValue)
                }
            }

            throw InterruptedException()
        }
    }

    private suspend fun sendPrepare(prepareRequest: PrepareRequest, server : Int) : PrepareResponse {

        println("server $server ")


        val managedChannel = ManagedChannelBuilder.forAddress(URL, server)
                .usePlaintext().build()
        val blockingStub = PrepareServiceGrpc.newBlockingStub(managedChannel)

        val prepareRequestGrpc = PrepareRequestGrpc.newBuilder()
                .setId(prepareRequest.proposalNumber.identifier)
                .setRound(prepareRequest.proposalNumber.round)
                .build()
        var prepareResponseGrpc = PrepareResponseGrpc.newBuilder()
                .build()

        try {
            val asyncResponse = GlobalScope.async { blockingStub.sendPrepare(prepareRequestGrpc) }

            prepareResponseGrpc = asyncResponse.await()
        } catch (e : Exception) {
            println(e)

            return PrepareResponse
                    .Reject(prepareRequest.proposalNumber);
        }

        var prepareResponse: PrepareResponse = PrepareResponse
                .Reject(ProposalNumber(
                        prepareResponseGrpc.round,
                        prepareResponseGrpc.id))

        if (prepareResponseGrpc.isAccepted) {
            prepareResponse = PrepareResponse
                    .PromiseAccepted(ProposalNumber(
                            prepareResponseGrpc.round,
                            prepareResponseGrpc.id))
        }
        return prepareResponse
    }

    private fun sendAccept(acceptRequest : AcceptRequest, server: Int) : AcceptResponse {
        val managedChannel = ManagedChannelBuilder.forAddress(URL, server)
                .usePlaintext().build()
        val blockingStub = AcceptServiceGrpc.newBlockingStub(managedChannel)

        val acceptRequestGrpc = AcceptRequestGrpc.newBuilder()
                .setId(acceptRequest.proposalNumber.identifier)
                .setRound(acceptRequest.proposalNumber.round)
                .setAcceptedValue(acceptRequest.value)
                .build()
        val acceptResponseGrpc: AcceptResponseGrpc

        try {
            acceptResponseGrpc = blockingStub.sendAccept(acceptRequestGrpc)

        } catch (e : Exception) {
            println(e)
            return AcceptResponse.Reject;
        }
        var acceptResponse : AcceptResponse = AcceptResponse.Reject

        if (acceptResponseGrpc.isAccepted) {
            acceptResponse = AcceptResponse.Accept
        }

        return acceptResponse
    }

    private fun sendDecide(decideRequest : DecideRequest, server: Int) {
        GlobalScope.launch {
            val managedChannel = ManagedChannelBuilder.forAddress(URL, server)
                    .usePlaintext().build()
            val blockingStub = DecideServiceGrpc.newBlockingStub(managedChannel)

            val decideRequestGrpc = DecideRequestGrpc.newBuilder()
                    .setValue(decideRequest.value)
                    .build()

            blockingStub.sendDecide(decideRequestGrpc)
        }
    }
}
