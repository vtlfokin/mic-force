import kotlin.math.absoluteValue

class ScoreCalculator {
    /**
     * Минимальная задержка между начислениями балов за протяженную "почти без изменений" громкость
     */
    private val delayMs = 300
    /**
     * Дельта, меньше которой громкость считаем "почти без изменений"
     */
    private val unfeelDelta = 50

    private var lastScoreTime = 0L
    private var lastScore = 0

    fun fromStrength(strength: Float): Int? {
        val score = (strength * 100).toInt()
        val finalScore = getScore(score)

        finalScore?.let {
            lastScore = score
            lastScoreTime = getCurTime()
        }

        return finalScore
    }

    /**
     * Если есть сильное изменение уровня относительно предыдущих замеров - добавляем очки немедленно
     * т.к. это будет добавлять "двайва" в процесс.
     * А уровень громкости "почти без изменений" будет давать очки не чаще таймаута
     */
    private fun getScore(score: Int): Int? {
        if ((score - lastScore).absoluteValue > unfeelDelta) return score

        if (isDelayOutdated()) return score

        return null
    }

    private fun getCurTime() = System.currentTimeMillis()

    /**
     * Достаточно ли времени прошло для добавления очков
     */
    private fun isDelayOutdated(): Boolean {
        if (lastScoreTime == 0L) return true

        return (getCurTime() - lastScoreTime) >= delayMs
    }
}