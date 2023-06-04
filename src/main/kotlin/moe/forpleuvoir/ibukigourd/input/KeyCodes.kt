@file:Suppress("unused")

package moe.forpleuvoir.ibukigourd.input

import net.minecraft.client.util.InputUtil

interface KeyCode {

	val code: Int

	val keyName: String
		get() = InputUtil.fromKeyCode(code, 0).localizedText.string


	companion object {

		private val keyMap: Map<Int, KeyCode> by lazy {
			buildMap {
				this[UNKNOWN.code] = UNKNOWN
				for (mouseCode in Mouse.values()) this[mouseCode.code] = mouseCode
				for (keyCode in Keyboard.values()) this[keyCode.code] = keyCode
			}
		}

		@JvmStatic
		val UNKNOWN: KeyCode = object : KeyCode {
			override val code: Int = 0
		}

		@JvmStatic
		fun fromCode(code: Int): KeyCode = keyMap[code] ?: UNKNOWN

	}

}

enum class Mouse(override val code: Int) : KeyCode {
	LEFT(0),
	RIGHT(1),
	MIDDLE(2),
	BUTTON_4(3),
	BUTTON_5(4),
	BUTTON_6(5),
	BUTTON_7(6),
	BUTTON_8(7);
}

enum class Keyboard(override val code: Int) : KeyCode {
	KEY_0(48),
	KEY_1(49),
	KEY_2(50),
	KEY_3(51),
	KEY_4(52),
	KEY_5(53),
	KEY_6(54),
	KEY_7(55),
	KEY_8(56),
	KEY_9(57),
	A(65),
	B(66),
	C(67),
	D(68),
	E(69),
	F(70),
	G(71),
	H(72),
	I(73),
	J(74),
	K(75),
	L(76),
	M(77),
	N(78),
	O(79),
	P(80),
	Q(81),
	R(82),
	S(83),
	T(84),
	U(85),
	V(86),
	W(87),
	X(88),
	Y(89),
	Z(90),
	F1(290),
	F2(291),
	F3(292),
	F4(293),
	F5(294),
	F6(295),
	F7(296),
	F8(297),
	F9(298),
	F10(299),
	F11(300),
	F12(301),
	F13(302),
	F14(303),
	F15(304),
	F16(305),
	F17(306),
	F18(307),
	F19(308),
	F20(309),
	F21(310),
	F22(311),
	F23(312),
	F24(313),
	F25(314),
	NUM_LOCK(282),
	KP_0(320),
	KP_1(321),
	KP_2(322),
	KP_3(323),
	KP_4(324),
	KP_5(325),
	KP_6(326),
	KP_7(327),
	KP_8(328),
	KP_9(329),
	KP_DECIMAL(330),
	KP_ENTER(335),
	KP_EQUAL(336),
	DOWN(264),
	LEFT(263),
	RIGHT(262),
	UP(265),
	KP_ADD(334),
	APOSTROPHE(39),
	BACKSLASH(92),
	COMMA(44),
	EQUAL(61),
	GRAVE_ACCENT(96),
	LEFT_BRACKET(91),
	MINUS(45),
	KP_MULTIPLY(332),
	PERIOD(46),
	RIGHT_BRACKET(93),
	SEMICOLON(59),
	SLASH(47),
	SPACE(32),
	TAB(258),
	LEFT_ALT(342),
	LEFT_CONTROL(341),
	LEFT_SHIFT(340),
	LEFT_SUPER(343),
	RIGHT_ALT(346),
	RIGHT_CONTROL(345),
	RIGHT_SHIFT(344),
	RIGHT_SUPER(347),
	ENTER(257),
	ESCAPE(256),
	BACKSPACE(259),
	DELETE(261),
	END(269),
	HOME(268),
	INSERT(260),
	PAGE_DOWN(267),
	PAGE_UP(266),
	CAPS_LOCK(280),
	PAUSE(284),
	SCROLL_LOCK(281),
	PRINT_SCREEN(283);

}
