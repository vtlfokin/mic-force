import javafx.application.Platform
import javafx.beans.property.SimpleFloatProperty
import javafx.scene.paint.Color
import tornadofx.*

fun main(args: Array<String>) {
    launch<MyApp>(args)
}

class MyApp: App(MyView::class)

class MyView: View() {
    val controller = MyController

    override val root = vbox {
        label("strength: ")
        label(controller.strength)
        progressbar(controller.strength) {
            setPrefSize(200.0, 100.0)
        }
    }
}

object MyController: Controller() {
    var strength = SimpleFloatProperty(this, "strength", 0f)

    fun updateStrength(newStrength: Float) {
        Platform.runLater {
            strength.set(newStrength)
        }
    }
}