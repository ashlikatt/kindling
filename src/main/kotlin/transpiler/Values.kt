package transpiler

import serializer.DFSerializable
import serializer.serializeString
import serializer.toInner

/**
 * Represents a value in DF, such as Text, Variables, or Items.
 */
sealed interface DFValue : DFSerializable {
    data class Text(val text: String) : DFValue {
        override fun serialize() = """{"id":"txt","data":{"name":"${toInner(text)}"}}"""
        override fun toString() = "\"$text\""
    }
    data class Number(val num: Float): DFValue {
        override fun serialize() = """{"id":"num","data":{"name":"$num"}}"""
        override fun toString() = num.toString()
    }
    data class Location(val x: Float, val y: Float, val z: Float, val pitch: Float, val yaw: Float) : DFValue {
        override fun serialize() = """{"id":"loc","data":{""" +
                """"isBlock":false,""" +
                """"x":$x,""" +
                """"y":$y,""" +
                """"z":$z,""" +
                """"pitch":$pitch,""" +
                """"yaw":$yaw}}"""
        override fun toString() = "Loc[$x, $y, $z, $pitch, $yaw]"
    }
    data class Vector(val x: Float, val y: Float, val z: Float) : DFValue {
        override fun serialize() = """{"id":"vec","data":{"x":$x,"y":$y,"z":$z}}"""
        override fun toString() = "<$x, $y, $z>"
    }
    data class Sound(val type: String, val pitch: Float, val volume: Float) : DFValue {
        override fun serialize() = """{"id":"snd","data":{""" +
                """"sound":${serializeString(type)},""" +
                """"pitch":$pitch,""" +
                """"vol":$volume}}"""
        override fun toString() = "Snd[$type, $pitch, $volume]"
    }
    data class PotionEffect(val type: String, val duration: Int, val level: Int) : DFValue {
        override fun serialize() = """{"id":"pot","data":{""" +
                """"pot":${serializeString(type)},""" +
                """"dur":$duration,""" +
                """"amp":$level}}"""
        override fun toString() = "Pot[$type, $duration, $level]"
    }
    data class Variable(val name: String, val scope: VariableScope) : DFValue {
        override fun serialize() = """{"id":"var","data":{""" +
                """"name":${serializeString(name)},""" +
                """"scope":"${scope.serialize()}"}}"""
        override fun toString() = "$scope[$name]"
    }
    data class GameValue(val type: String, val selector: Selector) : DFValue {
        override fun serialize() = """{"id":"g_val","data":{""" +
                """"type":${serializeString(type)},""" +
                """"target":"${selector.serialize()}"}}"""
        override fun toString() = "Val[$type, $selector]"
    }
    data class Particle(val type: String) : DFValue {
        var amount = 1
        var spreadX = 0f
        var spreadY = 0f
        var motionX: Float? = null
        var motionY: Float? = null
        var motionZ: Float? = null
        var size: Float? = null
        var roll: Float? = null
        var variationSize: Int? = null
        var variationColor: Int? = null
        var variationMotion: Int? = null
        var material: String? = null
        var color: Int? = null // RGB number: R * 256^2 + G * 256 + B

        override fun serialize(): String {
            val settings = mutableListOf<String>()
            if (motionX != null) settings.add(""""x":$motionX""")
            if (motionY != null) settings.add(""""y":$motionY""")
            if (motionZ != null) settings.add(""""z":$motionZ""")
            if (size != null) settings.add(""""size":$size""")
            if (roll != null) settings.add(""""roll":$roll""")
            if (variationSize != null) settings.add(""""sizeVariation":$variationMotion""")
            if (variationColor != null) settings.add(""""colorVariation":$variationMotion""")
            if (variationMotion != null) settings.add(""""motionVariation":$variationMotion""")
            if (material != null) settings.add(""""material":${serializeString(material)}""")
            if (color != null) settings.add(""""rgb":$color""")
            return """{"id":"part","data":{""" +
                        """"particle":${serializeString(type)},""" +
                        """"cluster":{""" +
                            """"amount":$amount,""" +
                            """"horizontal":$spreadX,""" +
                            """"vertical":$spreadY""" +
                        """},"data":{${settings.joinToString(",")}}}}"""
        }
        override fun toString(): String {
            val settings = mutableListOf("amount = $amount", "spreadX = $spreadX", "spreadY = $spreadY")
            if (motionX != null) settings.add("motionX = $motionX")
            if (motionY != null) settings.add("motionY = $motionY")
            if (motionZ != null) settings.add("motionZ = $motionZ")
            if (size != null) settings.add("size = $size")
            if (roll != null) settings.add("rol = $roll")
            if (variationSize != null) settings.add("variationSize = $variationSize")
            if (variationColor != null) settings.add("variationColor = $variationColor")
            if (variationMotion != null) settings.add("variationMotion = $variationMotion")
            if (material != null) settings.add("material = $material")
            if (color != null) settings.add("color = $color")
            return "Par[$type: ${ settings.joinToString(", ") }]"
        }
    }

    data class Tag(val option: String, val tag: String, val block: String, val action: String) : DFValue {
        override fun serialize() = """{"id":"bl_tag","data":{"option":${serializeString(option)},"tag":${serializeString(tag)},"action":${serializeString(action)},"block":${serializeString(block)}}}"""
        override fun toString() = "{$tag = $option}"
    }
    data class Item(val data: String) : DFValue {
        override fun serialize() = "{}" // TODO
        override fun toString() = "{ITEM}"
    }
}