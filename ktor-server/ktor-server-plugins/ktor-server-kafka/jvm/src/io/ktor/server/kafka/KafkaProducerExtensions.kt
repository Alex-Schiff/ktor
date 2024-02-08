/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.kafka

import kotlinx.coroutines.*
import org.apache.kafka.clients.producer.*
import java.util.concurrent.*

/** Wraps future in a coroutine */
public suspend fun <K, V> KafkaProducer<K, V>.sendAsync(record: ProducerRecord<K, V>): RecordMetadata {
    val future = this.send(record)
    return handleFuture(future)
}

public suspend fun <K, V> KafkaProducer<K, V>.sendAsync(
    record: ProducerRecord<K, V>,
    callback: (metadata: RecordMetadata, exception: Exception) -> Unit
): RecordMetadata {
    val future = this.send(record, callback)
    return handleFuture(future)
}

private suspend fun handleFuture(future: Future<RecordMetadata>): RecordMetadata = withContext(Dispatchers.IO) {
    coroutineScope {
        val cancellableJob = launch {
            suspendCancellableCoroutine { _ ->
                future.cancel(true)
            }
        }

        runInterruptible {
            future.get().also {
                cancellableJob.cancel()
            }
        }
    }
}

