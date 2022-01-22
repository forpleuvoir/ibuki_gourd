package forpleuvoir.ibuki_gourd.mixin;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 项目名 ibuki_gourd
 * <p>
 * 包名 forpleuvoir.ibuki_gourd.mixin
 * <p>
 * 文件名 MouseAccessor
 * <p>
 * 创建时间 2022/1/17 17:01
 *
 * @author forpleuvoir
 */
@Mixin(Mouse.class)
public interface MouseAccessor {

    @Invoker("onMouseButton")
    void invokerOnMouseButton(long window, int button, int action, int mods);

    @Invoker("onMouseScroll")
    void invokerOnMouseScroll(long window, double horizontal, double vertical);
}
