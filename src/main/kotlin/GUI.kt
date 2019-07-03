import javafx.application.Platform
import javafx.beans.property.SimpleFloatProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.stage.Stage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import tornadofx.*

class MyApp: App(MainView::class) {
    override fun start(stage: Stage) {
        super.start(stage)

        val controller = ScoreController
        controller.startListen()
    }
}

class MainView: View() {
    private val controller = ScoreController

    override val root = vbox {
        label("Strength: ")
        label(controller.strength)
        progressbar(controller.strength) {
            setPrefSize(200.0, 100.0)
        }
        label("+ ")
        label(controller.lastScore)
        label("Scores: ")
        label(controller.scores)
    }
}

object ScoreController: Controller() {
    var strength = SimpleFloatProperty(this, "strength", 0f)
    var scores = SimpleIntegerProperty(this, "scores", 0)
    var lastScore = SimpleIntegerProperty(this, "lastScore", 0)

    private val channel = Channel<Float>()
    private val micListener = MicListener(channel)
    private val calc = ScoreCalculator()

    init {
        // TODO Надо бы еще завершать нормально не main треды
        runAsync {
            micListener.start()
        }
    }

    fun startListen() {
        // TODO постараться разобраться с кодом на пересечениии асинк тредов торнадо и корутинами. Выглядит паршиво
        runAsync {
            runBlocking {
                while (true) {
                    val strength = channel.receive()

                    Platform.runLater{
                        this@ScoreController.strength.set(strength)
                    }

                    val score = calc.fromStrength(strength)

                    score?.let {
                        Platform.runLater{
                            this@ScoreController.lastScore.set(score)
                            this@ScoreController.scores.set(this@ScoreController.scores.get() + score)
                        }
                    }
                }
            }
        }
    }
}

