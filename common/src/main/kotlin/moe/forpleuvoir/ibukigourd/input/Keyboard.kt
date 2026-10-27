package moe.forpleuvoir.ibukigourd.input

import com.mojang.blaze3d.platform.InputConstants
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.plainText

/**
 * 键盘按键。`code` 为 [InputConstants.Type.KEYBOARD] 的取值（26.3 起为 SDL scancode），
 * 与 `KeyEvent.key()`、`InputConstants.getKey(KeyEvent)` 的数值同一命名空间。
 */
enum class Keyboard(override val code: Int) : KeyCode {
    UNKNOWN(0) {
        override val translationKey: String = "key.keyboard.unknown"
    },
    KEY_0(39),
    KEY_1(30),
    KEY_2(31),
    KEY_3(32),
    KEY_4(33),
    KEY_5(34),
    KEY_6(35),
    KEY_7(36),
    KEY_8(37),
    KEY_9(38),
    A(4),
    B(5),
    C(6),
    D(7),
    E(8),
    F(9),
    G(10),
    H(11),
    I(12),
    J(13),
    K(14),
    L(15),
    M(16),
    N(17),
    O(18),
    P(19),
    Q(20),
    R(21),
    S(22),
    T(23),
    U(24),
    V(25),
    W(26),
    X(27),
    Y(28),
    Z(29),
    F1(58),
    F2(59),
    F3(60),
    F4(61),
    F5(62),
    F6(63),
    F7(64),
    F8(65),
    F9(66),
    F10(67),
    F11(68),
    F12(69),
    F13(104),
    F14(105),
    F15(106),
    F16(107),
    F17(108),
    F18(109),
    F19(110),
    F20(111),
    F21(112),
    F22(113),
    F23(114),
    F24(115),
    NUM_LOCK(83),
    KP_0(98),
    KP_1(89),
    KP_2(90),
    KP_3(91),
    KP_4(92),
    KP_5(93),
    KP_6(94),
    KP_7(95),
    KP_8(96),
    KP_9(97),
    KP_DECIMAL(220),
    KP_ENTER(88),
    KP_EQUAL(103),
    DOWN(81),
    LEFT(80),
    RIGHT(79),
    UP(82),
    KP_ADD(87),
    APOSTROPHE(52),
    BACKSLASH(49),
    COMMA(54),
    EQUAL(46),
    GRAVE_ACCENT(53),
    LEFT_BRACKET(47),
    MINUS(45),
    KP_MULTIPLY(85),
    PERIOD(55),
    RIGHT_BRACKET(48),
    SEMICOLON(51),
    SLASH(56),
    SPACE(44),
    TAB(43),
    LEFT_ALT(226),
    LEFT_CONTROL(224),
    LEFT_SHIFT(225),
    LEFT_SUPER(227),
    RIGHT_ALT(230),
    RIGHT_CONTROL(228),
    RIGHT_SHIFT(229),
    RIGHT_SUPER(231),
    ENTER(40),
    ESCAPE(41),
    BACKSPACE(42),
    DELETE(76),
    END(77),
    HOME(74),
    INSERT(73),
    PAGE_DOWN(78),
    PAGE_UP(75),
    CAPS_LOCK(57),
    PAUSE(72),
    SCROLL_LOCK(71),
    PRINT_SCREEN(70);

    companion object {

        internal val keyMap: Map<Int, Keyboard> by lazy { entries.associateBy { it.code } }

        @JvmStatic
        fun fromCode(code: Int): Keyboard = keyMap[code] ?: UNKNOWN

        fun isToggle(keyCode: KeyCode): Boolean {
            return keyCode == ENTER || keyCode == SPACE || keyCode == KP_ENTER
        }

        fun isToggle(keyCode: Int): Boolean {
            return isToggle(fromCode(keyCode))
        }
    }

    private val _keyNameText by lazy { InputConstants.Type.KEYBOARD.getOrCreate(code).displayName }

    override val keyNameText: MutableText
        get() = _keyNameText.copy()

    override val keyName: String by lazy { _keyNameText.plainText }

    override val translationKey: String by lazy { InputConstants.Type.KEYBOARD.getOrCreate(code).name }
}
