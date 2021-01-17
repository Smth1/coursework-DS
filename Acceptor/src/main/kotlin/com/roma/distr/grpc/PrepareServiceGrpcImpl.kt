package com.roma.distr.grpc

import com.roma.distr.acceptor.PrepareRequest
import com.roma.distr.acceptor.PrepareResponse
import com.roma.distr.acceptor.ProposalNumber
import com.roma.distr.service.AcceptorService
import org.lognet.springboot.grpc.GRpcService

@GRpcService
class PrepareServiceGrpcImpl(private val acceptorService: AcceptorService) : PrepareServiceGrpcKt.PrepareServiceCoroutineImplBase() {
    override suspend fun sendPrepare(request: PrepareRequestGrpc): PrepareResponseGrpc {
        val proposalNumber = ProposalNumber(request.round, request.id)
        val prepareRequest = PrepareRequest(proposalNumber)

        val prepareResponse = acceptorService.prepare(prepareRequest)
        var prepareResponseGrpc  : PrepareResponseGrpc
                = PrepareResponseGrpc.newBuilder().build();

        if (prepareResponse is PrepareResponse.PromiseAccepted) {
            prepareResponseGrpc = PrepareResponseGrpc.newBuilder()
                    .setId(prepareResponse.proposalNumber.identifier)
                    .setRound(prepareResponse.proposalNumber.round)
                    .setIsAccepted(true)
                    .build();
        } else if (prepareResponse is PrepareResponse.Reject) {
            prepareResponseGrpc = PrepareResponseGrpc.newBuilder()
                    .setId(prepareResponse.proposalNumber.identifier)
                    .setRound(prepareResponse.proposalNumber.round)
                    .setIsAccepted(true)
                    .build();
        }

        return prepareResponseGrpc;
    }
}
