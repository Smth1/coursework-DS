package com.roma.distr.proposer

sealed class AcceptResponse {
    object Accept: AcceptResponse()
    object Reject: AcceptResponse()
}
