package com.roma.distr.grpc

import com.roma.distr.learner.DecideRequest
import com.roma.distr.service.LearnerService
import org.lognet.springboot.grpc.GRpcService

@GRpcService
class DecideServiceGrpcImpl(private val learnerService: LearnerService) : DecideServiceGrpcKt.DecideServiceCoroutineImplBase() {
    override suspend fun sendDecide(request: DecideRequestGrpc): DecideResponseGrpc {
        val decideRequest = DecideRequest(request.value)
        learnerService.decide(decideRequest)

        return DecideResponseGrpc.newBuilder().build()
    }
}
