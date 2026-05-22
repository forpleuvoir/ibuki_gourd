package moe.forpleuvoir.ibukigourd.mixin.client;

//@Mixin(GuiTextRenderState.class)
//@Implements(@Interface(iface = GuiTextRenderStateExtensions.class, prefix = "ibukigourd$"))
//public class GuiTextRenderStateMixin {
//    @Unique
//    private float ibukigourd$x;
//
//    @Unique
//    private boolean ibukigourd$xInitialized = false;
//
//    @Unique
//    private float ibukigourd$y;
//
//    @Unique
//    private boolean ibukigourd$yInitialized = false;
//
//
//    @Unique
//    public float ibukigourd$getXF() {
//        return ibukigourd$x;
//    }
//
//    @Unique
//    public void ibukigourd$setXF(float x) {
//        ibukigourd$x = x;
//        ibukigourd$xInitialized = true;
//    }
//
//    @Unique
//    public float ibukigourd$getYF() {
//        return ibukigourd$y;
//    }
//
//    @Unique
//    public void ibukigourd$setYF(float y) {
//        ibukigourd$y = y;
//        ibukigourd$yInitialized = true;
//    }
//
//    @ModifyArgs(
//            method = "ensurePrepared",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/gui/Font;prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;"
//            )
//    )
//    private void ensurePrepared(Args args) {
//        if (ibukigourd$xInitialized) {
//            args.set(1, ibukigourd$x);
//        }
//        if (ibukigourd$yInitialized) {
//            args.set(2, ibukigourd$y);
//        }
//    }
//
//}
