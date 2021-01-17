package com.roma.distr.proposer

sealed class PrepareResponse {
    data class PromiseAccepted(val proposalNumber: ProposalNumber): PrepareResponse()
    data class Reject(val proposalNumber: ProposalNumber): PrepareResponse()
}
