package bruhcollective.itaysonlab.microapp.guard.core

import androidx.annotation.FloatRange
import bruhcollective.itaysonlab.jetisteam.proto.GuardData
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.nio.ByteBuffer
import java.time.Clock
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import kotlin.time.Duration.Companion.seconds

class GuardInstance(
    private val clock: Clock,
    private val configuration: GuardData
) {
    companion object {
        private const val Algorithm = "HmacSHA1"
        private const val Digits = 5
        private val Period = 30.seconds.inWholeMilliseconds
        private val Alphabet = "23456789BCDFGHJKMNPQRTVWXY".toCharArray()
    }

    val code = flow {
        do {
            emit(generateCode())
            delay(1000L)
        } while (currentCoroutineContext().isActive)
    }

    val revocationCode get() = configuration.revocation_code
    val username get() = configuration.account_name

    private val secretKey = SecretKeySpec(configuration.shared_secret.toByteArray(), Algorithm)
    private val digest = Mac.getInstance(Algorithm).also { it.init(secretKey) }

    private fun generateCode(): CodeModel {
        val currentTime = clock.millis()

        val progress = ((Period - ((currentTime) % Period)) / Period.toFloat()).coerceIn(0f..1f)
        val localDigest = digest.doFinal(ByteBuffer.allocate(8).putLong(currentTime / Period).array())

        val offset = (localDigest.last() and 0xf).toInt()
        val code = localDigest.copyOfRange(offset, offset + 4)
        code[0] = (0x7f and code[0].toInt()).toByte()

        return CodeModel(buildString {
            var remainingCodeInt = ByteBuffer.wrap(code).int
            repeat(Digits) {
                append(Alphabet[remainingCodeInt % Alphabet.size])
                remainingCodeInt /= 26
            }
        } to progress)
    }

    @JvmInline
    value class CodeModel(private val packed: Pair<String, Float>) {
        val code: String get() = packed.first

        @get:FloatRange(from = 0.0, to = 1.0)
        val progressRemaining: Float get() = packed.second
    }
}