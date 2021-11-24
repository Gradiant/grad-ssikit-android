package id.walt.model

//ANDROID PORT
import com.beust.klaxon.*
//ANDROID PORT
import id.walt.vclib.model.Proof

data class DidEbsi (
    @Json(name = "@context")
    //ANDROID PORT
    val context: EbsiContext,
    //ANDROID PORT
    override val id: String,
    @Json(serializeNull = false) val verificationMethod: List<VerificationMethod>? = null,
    @Json(serializeNull = false) val authentication: List<String>? = null,
    @Json(serializeNull = false) var assertionMethod: List<String>? = null,
    @Json(serializeNull = false) val capabilityDelegation: List<String>? = null,
    @Json(serializeNull = false) val capabilityInvocation: List<String>? = null,
    @Json(serializeNull = false) val keyAgreement: List<String>? = null,
    @Json(serializeNull = false) val serviceEndpoint: List<VerificationMethod>? = null,
    @Json(serializeNull = false) var proof: Proof? = null,
) : BaseDid()

//ANDROID PORT
open class EbsiContext

class EbsiContextStr(val value: String): EbsiContext()

class EbsiContextList(val value: List<String>): EbsiContext()

class ContextConverter: Converter {
    override fun canConvert(cls: Class<*>): Boolean {
        return cls == EbsiContext::class.java
    }

    override fun fromJson(jv: JsonValue): Any? {
        val context = jv.inside
        return when (context) {
            is JsonArray<*> -> {
                EbsiContextList(context.value as List<String>)
            }
            is String -> {
                EbsiContextStr(context)
            }
            else -> {
                "Not allowed context format"
            }
        }
    }

    override fun toJson(value: Any): String {
        val ebsiContext = value as EbsiContext
        return if (ebsiContext is EbsiContextList) ebsiContext.value.joinToString("\", \"", "[\"", "\"]", ) else "\"${(ebsiContext as EbsiContextStr).value}\""
    }
}
//ANDROID PORT
