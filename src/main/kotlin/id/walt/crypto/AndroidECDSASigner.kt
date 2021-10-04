//ANDROID PORT
package id.walt.crypto

import com.nimbusds.jose.util.Base64URL
import fr.acinq.secp256k1.Secp256k1
import java.security.MessageDigest
import java.security.interfaces.ECPrivateKey
import java.util.*


object AndroidECDSASigner {

    fun sign(key: ECPrivateKey, payload: String?): Base64URL {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(payload?.toByteArray())
        val digest = md.digest()
        var keyBytes = key.s.toByteArray()
        if (keyBytes.size == 33) {
            keyBytes = Arrays.copyOfRange(keyBytes, 1, keyBytes.size)
        }
        val signature = Secp256k1.sign(digest, keyBytes)
        return Base64URL.encode(signature)
    }
}
//ANDROID PORT