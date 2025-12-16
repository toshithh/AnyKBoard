package com.toshith.anykboard

import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent
import java.lang.reflect.Field
import java.util.*

class KeyboardExecutor {

    private val robot = Robot()
    private val clipboard = Toolkit.getDefaultToolkit().systemClipboard

    private val isMac =
        System.getProperty("os.name")
            .lowercase(Locale.getDefault())
            .contains("mac")

    /* -------------------------------------------------------
     *  KEY COMBOS (CTRL+ALT+DEL etc.)
     * ------------------------------------------------------- */

    private fun getKeyCode(key: String): Int {
        val clazz: Class<*> = KeyEvent::class.java
        val field: Field = clazz.getField(key)
        return field.getInt(null)
    }

    private fun keyPressRecursive(keys: List<String>, index: Int = 0) {
        if (index >= keys.size) return

        val raw = keys[index]
        val vkName =
            if (raw == "windows" && isMac)
                "VK_META"
            else
                "VK_" + raw.uppercase()

        val code = getKeyCode(vkName)

        robot.keyPress(code)
        keyPressRecursive(keys, index + 1)
        robot.keyRelease(code)
    }

    fun pressCombo(combo: String) {
        try {
            keyPressRecursive(combo.split("+"))
        } catch (e: Exception) {
            System.err.println("Combo failed [$combo]: $e")
        }
    }

    /* -------------------------------------------------------
     *  BULK TYPE (best-effort, layout dependent)
     * ------------------------------------------------------- */

    fun bulkType(text: String) {
        try {
            for (ch in text) {
                when (ch) {
                    '\n' -> tap(KeyEvent.VK_ENTER)
                    '\t' -> tap(KeyEvent.VK_TAB)
                    ' '  -> tap(KeyEvent.VK_SPACE)
                    else -> typeChar(ch)
                }
            }
        } catch (e: Exception) {
            System.err.println("Bulk type failed: $e")
        }
    }

    private fun typeChar(ch: Char) {
        val upper = ch.uppercaseChar()
        val needsShift = ch.isUpperCase() || shiftChars.contains(ch)

        val vkName = "VK_$upper"
        val code = try {
            getKeyCode(vkName)
        } catch (_: Exception) {
            return // unsupported character
        }

        if (needsShift) robot.keyPress(KeyEvent.VK_SHIFT)
        robot.keyPress(code)
        robot.keyRelease(code)
        if (needsShift) robot.keyRelease(KeyEvent.VK_SHIFT)
    }

    private fun tap(code: Int) {
        robot.keyPress(code)
        robot.keyRelease(code)
    }

    private val shiftChars = "~!@#$%^&*()_+{}|:\"<>?"

    /* -------------------------------------------------------
     *  BULK PASTE (exact text, Unicode / emoji safe)
     * ------------------------------------------------------- */

    fun bulkPaste(text: String) {
        try {
            clipboard.setContents(StringSelection(text), null)
            robot.delay(20)

            if (isMac) {
                robot.keyPress(KeyEvent.VK_META)
                robot.keyPress(KeyEvent.VK_V)
                robot.keyRelease(KeyEvent.VK_V)
                robot.keyRelease(KeyEvent.VK_META)
            } else {
                robot.keyPress(KeyEvent.VK_CONTROL)
                robot.keyPress(KeyEvent.VK_V)
                robot.keyRelease(KeyEvent.VK_V)
                robot.keyRelease(KeyEvent.VK_CONTROL)
            }
        } catch (e: Exception) {
            System.err.println("Bulk paste failed: $e")
        }
    }
}
