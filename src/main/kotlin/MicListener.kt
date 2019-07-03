import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine

class MicListener(private val channel: SendChannel<Float>) {

    private val format = AudioFormat(44100f, 16, 2, true, true)
    private val targetInfo = DataLine.Info(TargetDataLine::class.java, format)

    private val targetLine = AudioSystem.getLine(targetInfo) as TargetDataLine
    private val targetData = ByteArray(targetLine.bufferSize / 5)
    private val samples = FloatArray(targetLine.bufferSize / 10)

    fun start() = runBlocking {
        targetLine.open(format)
        targetLine.start()

        var numBytesRead: Int
        while (true) {
            // Читаем обязательно в IO корутинах + добавляем доп. точку приостановки корутины
            numBytesRead = withContext(Dispatchers.IO) {
                targetLine.read(targetData, 0, targetData.size)
            }

            if (numBytesRead == -1) break //TODO обыграть ситуацию прерывания прослушки

            send(
                extractStrength(numBytesRead)
            )
        }
    }

    private fun extractStrength(numBytesRead: Int): Float {
        // convert bytes to samples here
        var i = 0
        var s = 0
        while (i < numBytesRead) {
            var sample: Int = 0

            /**
             * reverse these two lines
             * if the format is not big endian
             */
            sample = sample or (targetData[i++].toInt() shl 8)
            sample = sample or (targetData[i++].toInt() and 0xFF)

            // normalize to range of +/-1.0f
            samples[s++] = sample / 32768f
        }

        val rms = samples.fold(0f) { acc, sample -> acc + (sample * sample)}

        return Math.sqrt(rms.toDouble() / samples.size).toFloat()
    }

    private suspend fun send(strength: Float) {
        channel.send(strength)
    }
}