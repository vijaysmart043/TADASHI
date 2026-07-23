package com.vijay.tadashi.core.tools.system

import com.vijay.tadashi.core.tools.ToolRequest
import com.vijay.tadashi.core.tools.ToolResult
import com.vijay.tadashi.core.tools.battery.BatteryInfo
import com.vijay.tadashi.core.tools.battery.BatteryInfoProvider
import com.vijay.tadashi.core.tools.battery.BatteryTool
import com.vijay.tadashi.core.tools.bluetooth.BluetoothChangeResult
import com.vijay.tadashi.core.tools.bluetooth.BluetoothController
import com.vijay.tadashi.core.tools.bluetooth.BluetoothStatusResult
import com.vijay.tadashi.core.tools.bluetooth.BluetoothTool
import com.vijay.tadashi.core.tools.clipboard.ClipboardController
import com.vijay.tadashi.core.tools.clipboard.ClipboardReadResult
import com.vijay.tadashi.core.tools.clipboard.ClipboardTool
import com.vijay.tadashi.core.tools.deviceinfo.DeviceInfo
import com.vijay.tadashi.core.tools.deviceinfo.DeviceInfoProvider
import com.vijay.tadashi.core.tools.deviceinfo.DeviceInfoTool
import com.vijay.tadashi.core.tools.ringer.RingerChangeResult
import com.vijay.tadashi.core.tools.ringer.RingerController
import com.vijay.tadashi.core.tools.ringer.RingerMode
import com.vijay.tadashi.core.tools.ringer.RingerModeTool
import com.vijay.tadashi.core.tools.ringer.RingerStatusResult
import com.vijay.tadashi.core.tools.rotation.RotationChangeResult
import com.vijay.tadashi.core.tools.rotation.RotationController
import com.vijay.tadashi.core.tools.rotation.RotationStatusResult
import com.vijay.tadashi.core.tools.rotation.ScreenRotationTool
import com.vijay.tadashi.core.tools.wifi.WifiChangeResult
import com.vijay.tadashi.core.tools.wifi.WifiController
import com.vijay.tadashi.core.tools.wifi.WifiTool
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemToolsTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun wifi_turnOn() = runBlocking {
        val controller = object : WifiController {
            private var enabled = false

            override fun isEnabled(): Result<Boolean> = Result.success(enabled)

            override fun setEnabled(enabled: Boolean): WifiChangeResult {
                this.enabled = enabled
                return WifiChangeResult(
                    verification = true,
                    enabled = enabled,
                    requiresUserAction = false,
                    message = "Wi-Fi turned on"
                )
            }
        }

        val tool = WifiTool(controller)
        val result = tool.execute(
            ToolRequest(
                toolId = "WIFI",
                arguments = mapOf("action" to "ON"),
                caller = "test",
                timestampMs = 0L
            )
        )

        assertTrue(result is ToolResult.Success)
        val report = json.decodeFromString<ToolExecutionReport>((result as ToolResult.Success).message)
        assertTrue(report.success)
        assertTrue(report.verification)
    }

    @Test
    fun bluetooth_turnOff() = runBlocking {
        val controller = object : BluetoothController {
            private var enabled = true

            override fun status(): BluetoothStatusResult {
                return BluetoothStatusResult(
                    enabled = enabled,
                    requiresPermission = false,
                    message = if (enabled) "Bluetooth is ON" else "Bluetooth is OFF"
                )
            }

            override fun setEnabled(enabled: Boolean): BluetoothChangeResult {
                this.enabled = enabled
                return BluetoothChangeResult(
                    verification = true,
                    enabled = enabled,
                    requiresPermission = false,
                    requiresUserAction = false,
                    message = "Bluetooth turned off"
                )
            }
        }

        val tool = BluetoothTool(controller)
        val result = tool.execute(
            ToolRequest(
                toolId = "BLUETOOTH",
                arguments = mapOf("action" to "OFF"),
                caller = "test",
                timestampMs = 0L
            )
        )

        assertTrue(result is ToolResult.Success)
        val report = json.decodeFromString<ToolExecutionReport>((result as ToolResult.Success).message)
        assertTrue(report.success)
        assertTrue(report.verification)
    }

    @Test
    fun battery_percentage() = runBlocking {
        val provider = object : BatteryInfoProvider {
            override fun getBatteryInfo(): Result<BatteryInfo> {
                return Result.success(
                    BatteryInfo(
                        percentage = 42,
                        isCharging = true,
                        health = "GOOD",
                        temperatureC = 30.5f,
                        powerSave = false
                    )
                )
            }
        }

        val tool = BatteryTool(provider)
        val result = tool.execute(
            ToolRequest(
                toolId = "BATTERY",
                arguments = mapOf("action" to "PERCENTAGE"),
                caller = "test",
                timestampMs = 0L
            )
        )

        assertTrue(result is ToolResult.Success)
        val report = json.decodeFromString<ToolExecutionReport>((result as ToolResult.Success).message)
        assertTrue(report.success)
        assertEquals("Battery is 42%", report.message)
    }

    @Test
    fun ringer_enableSilentMode() = runBlocking {
        val controller = object : RingerController {
            private var mode: RingerMode = RingerMode.NORMAL

            override fun status(): RingerStatusResult {
                return RingerStatusResult(
                    mode = mode,
                    message = "Ringer mode: ${mode.name}"
                )
            }

            override fun setMode(mode: RingerMode): RingerChangeResult {
                this.mode = mode
                return RingerChangeResult(
                    verification = true,
                    mode = mode,
                    requiresUserAction = false,
                    message = "Ringer mode set to ${mode.name}"
                )
            }
        }

        val tool = RingerModeTool(controller)
        val result = tool.execute(
            ToolRequest(
                toolId = "RINGER_MODE",
                arguments = mapOf("action" to "SILENT"),
                caller = "test",
                timestampMs = 0L
            )
        )

        assertTrue(result is ToolResult.Success)
        val report = json.decodeFromString<ToolExecutionReport>((result as ToolResult.Success).message)
        assertTrue(report.success)
        assertTrue(report.verification)
    }

    @Test
    fun clipboard_copyAndRead() = runBlocking {
        val controller = object : ClipboardController {
            private var text: String? = null

            override fun copy(text: String): Result<Unit> {
                this.text = text
                return Result.success(Unit)
            }

            override fun read(): ClipboardReadResult {
                val value = text
                return if (value == null) {
                    ClipboardReadResult(
                        text = null,
                        restricted = false,
                        message = "Clipboard is empty"
                    )
                } else {
                    ClipboardReadResult(
                        text = value,
                        restricted = false,
                        message = "Clipboard read"
                    )
                }
            }

            override fun clear(): Result<Unit> {
                text = null
                return Result.success(Unit)
            }

            override fun share(text: String): Result<Unit> = Result.success(Unit)
        }

        val tool = ClipboardTool(controller)

        val copyResult = tool.execute(
            ToolRequest(
                toolId = "CLIPBOARD",
                arguments = mapOf("action" to "COPY", "text" to "Hello"),
                caller = "test",
                timestampMs = 0L
            )
        )
        assertTrue(copyResult is ToolResult.Success)
        val copyReport = json.decodeFromString<ToolExecutionReport>((copyResult as ToolResult.Success).message)
        assertTrue(copyReport.success)

        val readResult = tool.execute(
            ToolRequest(
                toolId = "CLIPBOARD",
                arguments = mapOf("action" to "READ"),
                caller = "test",
                timestampMs = 0L
            )
        )
        assertTrue(readResult is ToolResult.Success)
        val readReport = json.decodeFromString<ToolExecutionReport>((readResult as ToolResult.Success).message)
        assertEquals("Hello", readReport.message)
    }

    @Test
    fun deviceInfo_show() = runBlocking {
        val provider = object : DeviceInfoProvider {
            override fun getDeviceInfo(): Result<DeviceInfo> {
                return Result.success(
                    DeviceInfo(
                        model = "Pixel",
                        manufacturer = "Google",
                        androidVersion = "16",
                        apiLevel = 36,
                        totalRamBytes = 8L * 1024L * 1024L * 1024L,
                        totalStorageBytes = 128L * 1024L * 1024L * 1024L,
                        cpuAbis = listOf("arm64-v8a"),
                        screenResolution = "1080x2400"
                    )
                )
            }
        }

        val tool = DeviceInfoTool(provider)
        val result = tool.execute(
            ToolRequest(
                toolId = "DEVICE_INFO",
                arguments = emptyMap(),
                caller = "test",
                timestampMs = 0L
            )
        )

        assertTrue(result is ToolResult.Success)
        val report = json.decodeFromString<ToolExecutionReport>((result as ToolResult.Success).message)
        assertTrue(report.message.contains("Model: Pixel"))
        assertTrue(report.success)
    }

    @Test
    fun screenRotation_enableDisable() = runBlocking {
        val controller = object : RotationController {
            private var enabled = false

            override fun status(): RotationStatusResult {
                return RotationStatusResult(
                    enabled = enabled,
                    message = if (enabled) "Auto-rotate is ON" else "Auto-rotate is OFF"
                )
            }

            override fun setAutoRotateEnabled(enabled: Boolean): RotationChangeResult {
                this.enabled = enabled
                return RotationChangeResult(
                    verification = true,
                    enabled = enabled,
                    requiresUserAction = false,
                    message = if (enabled) "Auto-rotate enabled" else "Auto-rotate disabled"
                )
            }
        }

        val tool = ScreenRotationTool(controller)

        val enableResult = tool.execute(
            ToolRequest(
                toolId = "SCREEN_ROTATION",
                arguments = mapOf("action" to "ENABLE"),
                caller = "test",
                timestampMs = 0L
            )
        )
        assertTrue(enableResult is ToolResult.Success)
        val enableReport = json.decodeFromString<ToolExecutionReport>((enableResult as ToolResult.Success).message)
        assertTrue(enableReport.success)
        assertEquals("Auto-rotate enabled", enableReport.message)

        val disableResult = tool.execute(
            ToolRequest(
                toolId = "SCREEN_ROTATION",
                arguments = mapOf("action" to "DISABLE"),
                caller = "test",
                timestampMs = 0L
            )
        )
        assertTrue(disableResult is ToolResult.Success)
        val disableReport = json.decodeFromString<ToolExecutionReport>((disableResult as ToolResult.Success).message)
        assertTrue(disableReport.success)
        assertEquals("Auto-rotate disabled", disableReport.message)
    }
}

