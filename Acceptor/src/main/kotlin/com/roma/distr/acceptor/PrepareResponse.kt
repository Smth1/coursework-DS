package com.roma.distr.acceptor

sealed class PrepareResponse {
    data class PromiseAccepted(val proposalNumber: ProposalNumber): PrepareResponse()
    data class Reject(val proposalNumber: ProposalNumber): PrepareResponse()
}
