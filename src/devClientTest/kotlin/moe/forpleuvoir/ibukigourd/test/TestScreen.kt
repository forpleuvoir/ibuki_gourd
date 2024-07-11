//package moe.forpleuvoir.ibukigourd.test
//
//import moe.forpleuvoir.ibukigourd.gui.base.extensions.moveToBottom
//import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
//import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
//import moe.forpleuvoir.ibukigourd.gui.widget.text.TextInput
//import moe.forpleuvoir.ibukigourd.test.TestInitialization.text
//import moe.forpleuvoir.ibukigourd.text.Literal
//import net.minecraft.client.gui.widget.ButtonWidget
//
//class TestScreen : IGScreen(Literal("test")) {
//    override fun init() {
//        val text = TextInput(40, 70, 120, 20).also {
//            it.text = text
//            it.onTextChanged = { str ->
//                text = str
//            }
//            it.suggestion = { str ->
//                "我去"
//            }
//            it.hintText = Literal("快灌注我")
//            addDrawableChild(it)
//        }
//        Button(40, 40, 120, 20, Literal("测试按钮1测试按钮1测试按钮1测试按钮1测试按钮1"))
//            .press {
//                println("测试按钮按下")
//            }.release {
//                println("测试按钮释放")
//            }.longPress(20) {
//                println("测试按钮长按")
//            }.let {
////                it.tooltip = Tooltip.of(Literal("这是工具提示\n这是第二行"))
//                addDrawableChild(it)
//            }
//
//        ButtonWidget.builder(Literal("测试按钮2")) {
//            println("测试按钮2")
//        }.dimensions(180, 40, 40, 20).build().let {
//            it.moveToBottom(text,5)
//            addDrawableChild(it)
//        }
//
//    }
//
//}