package com.roma.distr.acceptor

sealed class AcceptResponse {
    object Accept: AcceptResponse()
    object Reject: AcceptResponse()
}
