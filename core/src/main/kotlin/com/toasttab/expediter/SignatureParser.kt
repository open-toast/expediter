package com.toasttab.expediter

class SignatureParser private constructor(private val signature: String) {
    private var idx = 0

    private fun nextType(): TypeSignature {
        var primitive = true
        var array = false
        var c = signature[idx]
        while (c == '[') {
            array = true
            c = signature[++idx]
        }
        val name = if (c == 'L') {
            primitive = false
            val next = signature.indexOf(';', startIndex = idx + 1)
            if (next < 0) {
                error("error parsing type from $signature, cannot find ';' after index $idx")
            }
            val start = idx + 1
            idx = next + 1
            signature.substring(start, next)
        } else {
            signature.substring(idx, ++idx)
        }

        return TypeSignature(name, array, primitive)
    }

    private fun parseMethod(): MethodSignature {
        if (signature[idx++] != '(') {
            error("error parsing $signature, expected to start with '('")
        }

        val args = mutableListOf<TypeSignature>()

        while (signature[idx] != ')') {
            args.add(nextType())
        }

        idx++

        return MethodSignature(nextType(), args)
    }

    companion object {
        fun parseMethod(method: String) = SignatureParser(method).parseMethod()

        fun parseType(field: String) = SignatureParser(field).nextType()
    }
}

class MethodSignature(
    val returnType: TypeSignature,
    val argumentTypes: List<TypeSignature>
) {
    fun referencedTypes() = (argumentTypes + returnType).filter { !it.primitive }.map { it.scalarName }
}

class TypeSignature(
    val scalarName: String,
    val array: Boolean,
    val primitive: Boolean
) {
    fun referencedTypes() = if (primitive) emptySet() else setOf(scalarName)
}
