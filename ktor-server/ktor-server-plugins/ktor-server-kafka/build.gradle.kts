description = ""

kotlin.sourceSets {
    jvmMain {
        dependencies {
            api(project(":ktor-server:ktor-server-core"))

            api(libs.kafka.clients)
        }
    }
    jvmTest {
        dependencies {
            api(project(":ktor-server:ktor-server-test-base"))
            api(project(":ktor-server:ktor-server-test-suites"))
            api(project(":ktor-server:ktor-server-core"))

            api(libs.testcontainers.kafka)
            api(libs.testcontainers.junit)
            api(libs.junit)
            api(libs.kotlinx.coroutines.test)

            api(project(":ktor-server:ktor-server-core", configuration = "testOutput"))
        }
    }
}
