package com.roma.distr.grpc

import com.roma.distr.acceptor.AcceptRequest
import com.roma.distr.acceptor.AcceptResponse
import com.roma.distr.acceptor.ProposalNumber
import com.roma.distr.service.AcceptorService
import org.lognet.springboot.grpc.GRpcService

@GRpcService
class AcceptServiceGrpcImpl(private val acceptorService: AcceptorService) :
        AcceptServiceGrpcKt.AcceptServiceCoroutineImplBase() {
    override suspend fun sendAccept(request: AcceptRequestGrpc): AcceptResponseGrpc {
        val acceptRequest = AcceptRequest(
                ProposalNumber(request.round, request.id), request.acceptedValue)
        val acceptResponse = acceptorService.accept(acceptRequest)

        if (acceptResponse is AcceptResponse.Accept) {
            val acceptResponseGrpc = AcceptResponseGrpc.newBuilder()
                    .setIsAccepted(true)
                    .build()

            return acceptResponseGrpc
        } else {
            val acceptResponseGrpc = AcceptResponseGrpc.newBuilder()
                    .setIsAccepted(false)
                    .build()
            return acceptResponseGrpc
        }
    }
}
