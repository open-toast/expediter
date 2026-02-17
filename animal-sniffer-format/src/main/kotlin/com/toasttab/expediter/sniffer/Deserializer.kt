/*
 * Copyright (c) 2026 Toast Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.toasttab.expediter.sniffer

import java.io.DataInputStream
import java.io.InputStream

private const val MAGIC = 0xACED.toShort()
private const val NEW_OBJECT = 0x73.toByte()
private const val CLASS_DESC = 0x72.toByte()
private const val BLOCKDATA = 0x77.toByte()
private const val END_BLOCKDATA = 0x78.toByte()
private const val NULL = 0x70.toByte()

private const val NEW_STRING = 0x74.toByte()
private const val NEW_ARRAY = 0x75.toByte()
private const val REFERENCE = 0x71.toByte()

private const val FIRST_HANDLE = 0x7e0000

private class FieldDesc(
    val name: String,
    val type: String
)
private class ClassDesc(
    val name: String,
    val fields: List<FieldDesc>,
    val superClass: ClassDesc?
)

private class Handles {
    private val handles = mutableListOf<Any?>()

    operator fun get(i: Int) = handles[i]

    fun create(): Handle {
        handles.add(null)
        return Handle(handles.size - 1)
    }
    inner class Handle(
        private val index: Int
    ) {
        fun <T> assign(obj: T): T {
            handles[index] = obj
            return obj
        }
    }
}

class Deserializer(
    stream: InputStream
) {
    private val data = DataInputStream(stream)
    private val handles = Handles()

    fun deserialize() = data.use {
        readObjects()
    }

    private fun readString(assign: Boolean = false): String {
        val str = String(data.readNBytes(data.readShort().toInt()))
        if (assign) {
            handles.create().assign(str)
        }
        return str
    }

    private fun readHandle() = data.readInt() - FIRST_HANDLE

    private fun readStringOrRef(): String? = when (val b = data.readByte()) {
        NULL -> null
        NEW_STRING -> readString(assign = true)
        REFERENCE -> handles[readHandle()] as String
        else -> error("expect new string, reference, or null; got $b")
    }

    private fun readClassDesc(): ClassDesc? {
        return when (val b = data.readByte()) {
            NULL -> null
            CLASS_DESC -> { // classdesc
                val handle = handles.create()

                val name = readString()
                data.readLong() // SerialVersionUuid
                data.readByte() // Flags

                val fieldCount = data.readShort().toInt()
                val fields = ArrayList<FieldDesc>(fieldCount)

                for (i in 0 until fieldCount) {
                    data.readByte() // type code

                    fields.add(FieldDesc(readString(), readStringOrRef()!!))
                }

                expectNext(END_BLOCKDATA, "expect end of block data")

                handle.assign(ClassDesc(name, fields, readClassDesc()))
            }
            REFERENCE -> {
                handles[readHandle()] as ClassDesc
            }
            else -> error("expect class descriptor, reference, or null; got $b")
        }
    }

    private fun expectNext(expected: Byte, message: String) {
        val b = data.readByte()
        if (b != expected) {
            error("$message, got $b")
        }
    }

    internal fun readStringSet(): Set<String> {
        expectNext(NEW_OBJECT, "expected new object")
        val setClass = readClassDesc() ?: error("set class descriptor cannot be null")

        if (setClass.name != "java.util.HashSet" && setClass.name != "java.util.LinkedHashSet") {
            error("unexpected set class ${setClass.name}")
        }
        val handle = handles.create()

        expectNext(BLOCKDATA, "expected block data")
        data.readByte()

        val capacity = data.readInt()
        val loadFactor = data.readFloat()
        val size = data.readInt()

        val set = HashSet<String>(capacity, loadFactor)

        for (i in 0 until size) {
            set.add(readStringOrRef()!!)
        }

        expectNext(END_BLOCKDATA, "expected end of block data")

        if (set.size != size) {
            error("set size must be $size but found ${set.size} elements")
        }

        return handle.assign(set)
    }

    internal fun readStringArray(): List<String> {
        expectNext(NEW_ARRAY, "expected new array")
        readClassDesc()

        val handle = handles.create()
        val array = mutableListOf<String>()

        val len = data.readInt()

        for (i in 0 until len) {
            array.add(readStringOrRef()!!)
        }

        return handle.assign(array)
    }

    private fun readObj(): AnimalSnifferTypeDescriptor {
        return handles.create().assign(
            AnimalSnifferTypeDescriptor(
                readStringOrRef()!!,
                readStringSet(),
                readStringOrRef(),
                readStringArray()
            )
        )
    }

    internal fun readStreamHeader() {
        val magic = data.readShort()
        if (magic != MAGIC) {
            error("wrong magic bytes $magic, does not look like a serialized stream")
        }
        data.readShort() // version
    }

    private fun readObjects(): List<AnimalSnifferTypeDescriptor> {
        val list = mutableListOf<AnimalSnifferTypeDescriptor>()
        readStreamHeader()

        var b = data.readByte()

        while (b == NEW_OBJECT) {
            val cls = readClassDesc()!!

            if (cls.name != "org.codehaus.mojo.animal_sniffer.Clazz") {
                error("unexpected class ${cls.name}")
            }

            list.add(readObj())

            b = data.readByte()
        }

        if (b != NULL) {
            error("expected null at the end, got $b")
        }

        return list
    }
}
