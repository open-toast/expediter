package com.toasttab.expediter.parser

class SignatureParser private constructor(private val signature: String) {
    private var idx = 0

    private fun nextType(internal: Boolean): TypeSignature {
        var primitive = true
        var dimensions = 0
        var c = signature[idx]
        while (c == '[') {
            dimensions++
            c = signature[++idx]
        }

        val name = if (dimensions > 0 || !internal) {
            if (c == 'L') {
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
        } else {
            signature.substring(idx)
        }

        return TypeSignature(name, dimensions, primitive)
    }

    private fun parseMethod(): MethodSignature {
        if (signature[idx++] != '(') {
            error("error parsing $signature, expected to start with '('")
        }

        val args = mutableListOf<TypeSignature>()

        while (signature[idx] != ')') {
            args.add(nextType(false))
        }

        idx++

        return MethodSignature(nextType(false), args)
    }

    companion object {
        /**
         * Parses a standard method descriptor, e.g.
         *
         * (Ljava/lang/Object;)V for void fun(Object)
         */
        fun parseMethod(method: String) = SignatureParser(method).parseMethod()

        /**
         * Parses a standard type descriptor, as it appears in a method descriptor, e.g.
         *
         * L/java/lang/Object; for Object
         * [L/java/lang/Object; for Object[]
         */
        fun parseType(type: String) = SignatureParser(type).nextType(false)

        /**
         * Parses a internal type descriptor, as reported by ASM for method owners, instanceof, etc; e.g.
         *
         * [L/java/lang/Object; for Object[]
         *
         * but just
         *
         * java/lang/Object for Object
         */
        fun parseInternalType(type: String) = SignatureParser(type).nextType(true)
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
    val dimensions: Int,
    val primitive: Boolean
) {
    fun isArray() = dimensions > 0
    fun referencedTypes() = if (primitive) emptySet() else setOf(scalarName)

    fun scalarSignature() = TypeSignature(scalarName, 0, primitive)
    val name get() = scalarName + "[]".repeat(dimensions)
}
