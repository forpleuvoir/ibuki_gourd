package com.forpleuvoir.ibukigourd.config

import com.forpleuvoir.nebula.config.Config
import net.minecraft.text.MutableText
import net.minecraft.text.TranslatableTextContent

fun Config<*, *>.displayName(modId: String): MutableText = MutableText.of(TranslatableTextContent("$modId.config.$key"))

fun Config<*, *>.displayDesc(modId: String): MutableText =
	MutableText.of(TranslatableTextContent("$modId.config.$key.desc"))