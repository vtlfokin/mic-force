import tornadofx.launch
import javax.sound.sampled.*
import kotlinx.coroutines.*

fun main(args: Array<String>) = runBlocking {

    launch(Dispatchers.IO) {
        val format = AudioFormat(44100f, 16, 2, true, true)

        val targetInfo = DataLine.Info(TargetDataLine::class.java, format)

        try {
            val targetLine = AudioSystem.getLine(targetInfo) as TargetDataLine
            targetLine.open(format)
            targetLine.start()

            var numBytesRead: Int
            val targetData = ByteArray(targetLine.bufferSize / 5)
            val samples = FloatArray(targetLine.bufferSize / 10)

            while (true) {
                numBytesRead = withContext(Dispatchers.IO) {
                    targetLine.read(targetData, 0, targetData.size)
                }

                if (numBytesRead == -1) break

                straightMeter(targetData, samples, numBytesRead)
            }
        } catch (e: Exception) {
            System.err.println(e)
        }
    }

    println("next")

    launch<MyApp>(args)
}

fun straightMeter(buf: ByteArray, samples: FloatArray, numBytesRead: Int) {
    // convert bytes to samples here
    var i = 0
    var s = 0
    while (i < numBytesRead) {
        var sample: Int = 0

        sample = sample or (buf[i++].toInt() shl 8) //  if the format is big endian)
        sample = sample or (buf[i++].toInt() and 0xFF) // (reverse these two lines

        // normalize to range of +/-1.0f
        samples[s++] = sample / 32768f
    }

    var rms = 0f
//    var peak = 0f
    for (sample in samples) {
//        val abs = Math.abs(sample)
//        if (abs > peak) {
//            peak = abs
//        }

        rms += sample * sample
    }

    rms = Math.sqrt(rms.toDouble() / samples.size).toFloat()

    updateGUI(rms)
}

fun updateGUI(rms: Float) {

    val normalizedRms = if (rms < 0.001) 0f else rms
    MyController.updateStrength(normalizedRms)
}