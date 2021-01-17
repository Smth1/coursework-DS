package com.roma.distr

import com.roma.distr.grpc.ProposeRequestGrpc
import com.roma.distr.grpc.ProposeServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

fun main() {
    val URL = "localhost"
    val server = 6554
    val managedChannel = ManagedChannelBuilder.forAddress(URL, server)
            .usePlaintext().build()
    val blockingStub = ProposeServiceGrpc.newBlockingStub(managedChannel)


    val proposeRequestGrpc = ProposeRequestGrpc.newBuilder()
            .setValue(Random.nextInt())
            .build()
    runBlocking {
        val asyncResult = GlobalScope.async {
            blockingStub.sendPropose(proposeRequestGrpc)
        }

        asyncResult.await()
    }
}