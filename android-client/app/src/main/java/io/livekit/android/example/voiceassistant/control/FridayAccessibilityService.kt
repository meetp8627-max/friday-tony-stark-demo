package io.livekit.android.example.voiceassistant.control

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Lets FRIDAY act on the phone: open apps, tap things, type text, scroll,
 * navigate, and read what's on screen. Requires the user to manually enable
 * it once under Settings > Accessibility — Android does not allow this to be
 * granted silently, by design, since it's a powerful permission.
 *
 * A single static [instance] is kept so the RPC handler (registered in
 * VoiceAssistantViewModel) can reach this service without a bound-service
 * connection, since accessibility services are started by the OS, not by us.
 */
class FridayAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "FRIDAY Accessibility Service connected")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance === this) instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No continuous event handling needed — actions are performed on
        // demand from performAction() below, driven by RPC calls from FRIDAY.
    }

    override fun onInterrupt() {}

    /**
     * Executes one control_phone action. Returns a short plain-English
     * result string that gets sent straight back to FRIDAY's tool call.
     */
    fun performAction(action: String, target: String): String {
        return try {
            when (action) {
                "open_app" -> openApp(target)
                "click" -> clickByText(target)
                "type_text" -> typeText(target)
                "scroll_down" -> scroll(forward = true)
                "scroll_up" -> scroll(forward = false)
                "go_back" -> {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    "Went back."
                }
                "go_home" -> {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    "Went to the home screen."
                }
                "read_screen" -> readScreen()
                else -> "Unknown action: $action"
            }
        } catch (e: Exception) {
            Log.e(TAG, "performAction($action, $target) failed", e)
            "Action failed: ${e.message}"
        }
    }

    private fun openApp(appName: String): String {
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
        val match = apps.firstOrNull {
            pm.getApplicationLabel(it).toString().contains(appName, ignoreCase = true)
        } ?: return "Couldn't find an app matching \"$appName\"."

        val launchIntent = pm.getLaunchIntentForPackage(match.packageName)
            ?: return "Found \"$appName\" but it has no launchable activity."
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(launchIntent)
        return "Opened ${pm.getApplicationLabel(match)}."
    }

    private fun clickByText(text: String): String {
        val root = rootInActiveWindow ?: return "Can't read the screen right now."
        val node = findNodeByText(root, text)
            ?: return "Couldn't find anything on screen matching \"$text\"."
        val clickable = findClickableAncestor(node) ?: node
        val ok = clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        return if (ok) "Tapped \"$text\"." else "Found \"$text\" but couldn't tap it."
    }

    private fun typeText(text: String): String {
        val root = rootInActiveWindow ?: return "Can't read the screen right now."
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?: return "No text field is currently focused."
        val args = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text,
            )
        }
        val ok = focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        return if (ok) "Typed the text in." else "Couldn't type into the focused field."
    }

    private fun scroll(forward: Boolean): String {
        val root = rootInActiveWindow ?: return "Can't read the screen right now."
        val scrollable = findScrollable(root)
            ?: return "Nothing scrollable found on screen."
        val action = if (forward) {
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        } else {
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        }
        val ok = scrollable.performAction(action)
        return if (ok) "Scrolled." else "Couldn't scroll."
    }

    /** Collects visible text into a short summary FRIDAY can reason about. */
    private fun readScreen(): String {
        val root = rootInActiveWindow ?: return "Can't read the screen right now."
        val texts = LinkedHashSet<String>()
        collectText(root, texts, limit = 60)
        if (texts.isEmpty()) return "The screen has no readable text right now."
        return texts.joinToString(" | ")
    }

    private fun collectText(node: AccessibilityNodeInfo, out: MutableSet<String>, limit: Int) {
        if (out.size >= limit) return
        val t = node.text?.toString()?.trim()
        if (!t.isNullOrEmpty()) out.add(t)
        val d = node.contentDescription?.toString()?.trim()
        if (!d.isNullOrEmpty()) out.add(d)
        for (i in 0 until node.childCount) {
            if (out.size >= limit) return
            node.getChild(i)?.let { collectText(it, out, limit) }
        }
    }

    private fun findNodeByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val nodeText = node.text?.toString() ?: node.contentDescription?.toString()
        if (nodeText != null && nodeText.contains(text, ignoreCase = true)) return node
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findNodeByText(child, text)?.let { return it }
            }
        }
        return null
    }

    private fun findClickableAncestor(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node
        while (current != null) {
            if (current.isClickable) return current
            current = current.parent
        }
        return null
    }

    private fun findScrollable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findScrollable(child)?.let { return it }
            }
        }
        return null
    }

    companion object {
        private const val TAG = "FridayAccessibility"

        @Volatile
        var instance: FridayAccessibilityService? = null
            private set

        val isEnabled: Boolean get() = instance != null
    }
}
