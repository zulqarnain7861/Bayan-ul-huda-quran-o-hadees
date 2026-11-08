package com.example.data.repository

/**
 * Lightweight, zero-dependency, thread-safe JSON parser used by
 * QuranTajweedEngineRepository, IslamQaRepository, and ClassicalLibraryAndBiographiesRepository
 * for parsing bundled JSON/ZIP/GZIP assets on both Android and JVM unit tests.
 */
internal object MiniJsonParser {

  @Suppress("UNCHECKED_CAST")
  fun parseObject(json: String): Map<String, Any?> {
    val value = ParserState(json).parseValue()
    return (value as? Map<String, Any?>) ?: emptyMap()
  }

  @Suppress("UNCHECKED_CAST")
  fun parseArray(json: String): List<Any?> {
    val value = ParserState(json).parseValue()
    return (value as? List<Any?>) ?: emptyList()
  }

  fun getString(map: Map<String, Any?>?, key: String, default: String = ""): String {
    if (map == null) return default
    val v = map[key] ?: return default
    return when (v) {
      is String -> v
      else -> v.toString()
    }
  }

  fun getInt(map: Map<String, Any?>?, key: String, default: Int = 0): Int {
    if (map == null) return default
    val v = map[key] ?: return default
    return when (v) {
      is Number -> v.toInt()
      is String -> v.trim().toIntOrNull() ?: default
      else -> default
    }
  }

  fun getDouble(map: Map<String, Any?>?, key: String, default: Double = 0.0): Double {
    if (map == null) return default
    val v = map[key] ?: return default
    return when (v) {
      is Number -> v.toDouble()
      is String -> v.trim().toDoubleOrNull() ?: default
      else -> default
    }
  }

  fun getBoolean(map: Map<String, Any?>?, key: String, default: Boolean = false): Boolean {
    if (map == null) return default
    val v = map[key] ?: return default
    return when (v) {
      is Boolean -> v
      is String -> v.equals("true", ignoreCase = true)
      else -> default
    }
  }

  @Suppress("UNCHECKED_CAST")
  fun getObject(map: Map<String, Any?>?, key: String): Map<String, Any?> {
    if (map == null) return emptyMap()
    return (map[key] as? Map<String, Any?>) ?: emptyMap()
  }

  @Suppress("UNCHECKED_CAST")
  fun getArray(map: Map<String, Any?>?, key: String): List<Any?> {
    if (map == null) return emptyList()
    return (map[key] as? List<Any?>) ?: emptyList()
  }

  fun getStringList(map: Map<String, Any?>?, key: String): List<String> {
    if (map == null) return emptyList()
    val arr = map[key] as? List<*> ?: return emptyList()
    return arr.mapNotNull { it?.toString() }
  }

  private class ParserState(private val s: String) {
    private var pos = 0

    fun parseValue(): Any? {
      skipWs()
      if (pos >= s.length) return null
      return when (val ch = s[pos]) {
        '{' -> parseObj()
        '[' -> parseArr()
        '"' -> parseStr()
        't', 'f' -> parseBool()
        'n' -> {
          pos = (pos + 4).coerceAtMost(s.length)
          null
        }
        else -> if (ch == '-' || ch.isDigit()) parseNum() else {
          pos++
          null
        }
      }
    }

    private fun parseObj(): Map<String, Any?> {
      val map = LinkedHashMap<String, Any?>()
      pos++ // '{'
      skipWs()
      if (pos < s.length && s[pos] == '}') {
        pos++
        return map
      }
      while (pos < s.length) {
        skipWs()
        if (pos >= s.length || s[pos] == '}') {
          if (pos < s.length) pos++
          break
        }
        val key = if (s[pos] == '"') parseStr() else {
          pos++
          continue
        }
        skipWs()
        if (pos < s.length && s[pos] == ':') pos++
        val value = parseValue()
        map[key] = value
        skipWs()
        if (pos < s.length && s[pos] == ',') {
          pos++
        } else if (pos < s.length && s[pos] == '}') {
          pos++
          break
        } else {
          break
        }
      }
      return map
    }

    private fun parseArr(): List<Any?> {
      val list = ArrayList<Any?>()
      pos++ // '['
      skipWs()
      if (pos < s.length && s[pos] == ']') {
        pos++
        return list
      }
      while (pos < s.length) {
        skipWs()
        if (pos < s.length && s[pos] == ']') {
          pos++
          break
        }
        list.add(parseValue())
        skipWs()
        if (pos < s.length && s[pos] == ',') {
          pos++
        } else if (pos < s.length && s[pos] == ']') {
          pos++
          break
        } else {
          break
        }
      }
      return list
    }

    private fun parseStr(): String {
      pos++ // '"'
      val sb = StringBuilder()
      while (pos < s.length) {
        val ch = s[pos++]
        if (ch == '"') break
        if (ch == '\\' && pos < s.length) {
          when (val esc = s[pos++]) {
            '"', '\\', '/' -> sb.append(esc)
            'b' -> sb.append('\b')
            'f' -> sb.append('\u000C')
            'n' -> sb.append('\n')
            'r' -> sb.append('\r')
            't' -> sb.append('\t')
            'u' -> {
              if (pos + 4 <= s.length) {
                val hex = s.substring(pos, pos + 4)
                val code = hex.toIntOrNull(16)
                if (code != null) {
                  sb.append(code.toChar())
                }
                pos += 4
              }
            }
            else -> sb.append(esc)
          }
        } else {
          sb.append(ch)
        }
      }
      return sb.toString()
    }

    private fun parseBool(): Boolean {
      return if (s.startsWith("true", pos)) {
        pos += 4
        true
      } else {
        pos = (pos + 5).coerceAtMost(s.length)
        false
      }
    }

    private fun parseNum(): Number {
      val start = pos
      while (pos < s.length && (s[pos].isDigit() || s[pos] in "-+.eE")) {
        pos++
      }
      val raw = s.substring(start, pos)
      return if ('.' in raw || 'e' in raw || 'E' in raw) {
        raw.toDoubleOrNull() ?: 0.0
      } else {
        raw.toLongOrNull() ?: 0L
      }
    }

    private fun skipWs() {
      while (pos < s.length && s[pos].isWhitespace()) {
        pos++
      }
    }
  }
}
