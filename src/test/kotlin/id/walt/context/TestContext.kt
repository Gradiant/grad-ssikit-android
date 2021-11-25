package id.walt.context

//ANDROID PORT
import id.walt.servicematrix.utils.AndroidUtils
//ANDROID PORT
import id.walt.services.context.Context
import id.walt.services.hkvstore.FileSystemHKVStore
import id.walt.services.hkvstore.FilesystemStoreConfig
import id.walt.services.keystore.HKVKeyStoreService
import id.walt.services.vcstore.HKVVcStoreService

val TEST_CONTEXT_DATA_ROOT = "build/testdata/context"

class TestContext(val username: String): Context {
  override val keyStore = HKVKeyStoreService()
  override val vcStore = HKVVcStoreService()
  //ANDROID PORT
  override val hkvStore = FileSystemHKVStore()
  //ANDROID PORT
}
