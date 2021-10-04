//ANDROID PORT
package id.walt.crypto

import fr.acinq.secp256k1.Secp256k1
import java.security.MessageDigest
import java.security.interfaces.ECPublicKey
import java.util.*


object AndroidECDSAVerifier {

    fun verify(signature: ByteArray, payload: ByteArray, key: ECPublicKey): Boolean {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(payload)
        val digest = md.digest()
        val keyBytes = Arrays.copyOfRange(key.encoded, 23,  key.encoded.size)
        return Secp256k1.verify(signature, digest, keyBytes)
    }
}
//ANDROID PORT