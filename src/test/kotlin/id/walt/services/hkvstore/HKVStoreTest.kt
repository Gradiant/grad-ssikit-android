package id.walt.services.hkvstore

import id.walt.servicematrix.ServiceRegistry
import id.walt.services.context.WaltContext
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

//ANDROID PORT
/*class HKVStoreTest : StringSpec({

    fun hkvTest() {
        val hkvStore = WaltContext.hkvStore
        val testData = "test data"

        println("Adding items...")
        hkvStore.put(HKVKey("parent", "child", "leaf"), testData)
        hkvStore.put(HKVKey("parent", "child2", "leaf2"), testData)

        println("Retrieving items...")
        hkvStore.listChildKeys(HKVKey("parent"), true) shouldHaveSize 2
        hkvStore.listChildKeys(HKVKey("parent", "child")) shouldHaveSize 1

        println("Retrieving data...")
        hkvStore.getAsString(HKVKey("parent", "child", "leaf")) shouldBe testData

        println("Deleting items...")
        hkvStore.delete(HKVKey("parent"), true)
        hkvStore.listChildKeys(HKVKey("parent"), true) shouldHaveSize 0
    }

    "InMemoryStore test" {
        ServiceRegistry.registerService<HKVStoreService>(InMemoryHKVStore())
        hkvTest()
    }
    "FileSystemStore test" {
        ServiceRegistry.registerService<HKVStoreService>(FileSystemHKVStore("./fsStore.conf"))
        hkvTest()
    }
})*/
//ANDROID PORT
