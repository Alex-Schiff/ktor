/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.kafka

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.apache.kafka.clients.consumer.*
import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
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
            KafkaProducer<String, String>(
                mapOf(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers,
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.qualifiedName,
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.qualifiedName
                )
            )
        val kafkaConsumer =
            KafkaConsumer<String, String>(
                mapOf(
                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers,
                    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.qualifiedName,
                    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.qualifiedName,
                    ConsumerConfig.GROUP_ID_CONFIG to "test-group"
                )
            )
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
