package com.roma.distr.grpc

import com.roma.distr.proposer.ProposeRequest
import com.roma.distr.service.ProposerService
import org.lognet.springboot.grpc.GRpcService

@GRpcService
class ProposeServiceGrpcImpl(private val proposerService : ProposerService) : ProposeServiceGrpcKt.ProposeServiceCoroutineImplBase() {
    override suspend fun sendPropose(request: ProposeRequestGrpc): ProposeResponseGrpc {
        val proposeRequest = ProposeRequest(request.value)

        proposerService.propose(proposeRequest)

        return ProposeResponseGrpc.newBuilder()
                .build()
    }
}
