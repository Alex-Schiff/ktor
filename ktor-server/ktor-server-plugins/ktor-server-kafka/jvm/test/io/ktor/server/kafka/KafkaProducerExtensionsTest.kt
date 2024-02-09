/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.kafka

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.apache.kafka.clients.consumer.*
import org.apache.kafka.clients.producer.*
import org.testcontainers.containers.*
import org.testcontainers.junit.jupiter.*
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.*
import java.time.*
import kotlin.test.*
import kotlin.test.Test

private const val TOPIC = "test-topic"

@Testcontainers
class KafkaProducerExtensionsTest {
    @Container
    private val kafka: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))

    @Test
    fun sendAsyncWithoutCallbackShouldProduceRecord() = runTest {
        val kafkaProducer =
            KafkaProducer<String, String>(mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers,
            ))
        val kafkaConsumer =
            KafkaConsumer<String, String>(mapOf(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers))
        val data = "test"

        kafkaProducer.sendAsync(ProducerRecord<String, String>(TOPIC, data))

        val records = async {
            kafkaConsumer.subscribe(listOf(TOPIC))
            return@async kafkaConsumer.poll(Duration.ofSeconds(10))
        }.await()
        kafkaConsumer.unsubscribe()
        
        assertEquals(data, records.records(TOPIC).toList()[0].value())
    }
}
