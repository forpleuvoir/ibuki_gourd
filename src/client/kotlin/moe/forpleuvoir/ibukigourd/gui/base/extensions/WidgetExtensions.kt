package moe.forpleuvoir.ibukigourd.gui.base.extensions


//fun IGWidget.moveToTop(target: IGWidget, margin: Int, alignment: (Orientation) -> HorizontalAlignment = HorizontalAlignment::Center) {
//    alignment(Orientation.Horizontal).align(target.as, this.asBox).let { v2f ->
//        translateTo(v2f.x().toInt(), target.top - margin - this.height)
//    }
//}
//
//fun IGWidget.moveToBottom(target: IGWidget, margin: Int, alignment: (Orientation) -> HorizontalAlignment = HorizontalAlignment::Center) {
//    alignment(Orientation.Horizontal).align(target.asBox, this.asBox).let { v2f ->
//        translateTo(v2f.x().toInt(), target.bottom + margin)
//    }
//}
//
//fun IGWidget.moveToLeft(target: IGWidget, margin: Int, alignment: (Orientation) -> HorizontalAlignment = HorizontalAlignment::Center) {
//    alignment(Orientation.Vertical).align(target.asBox, this.asBox).let { v2f ->
//        translateTo(target.left - margin - this.width, v2f.y().toInt())
//    }
//}
//
//fun IGWidget.moveToRight(target: IGWidget, margin: Int, alignment: (Orientation) -> HorizontalAlignment = HorizontalAlignment::Center) {
//    alignment(Orientation.Vertical).align(target.asBox, this.asBox).let { v2f ->
//        translateTo(target.right + margin, v2f.y().toInt())
//    }
//}