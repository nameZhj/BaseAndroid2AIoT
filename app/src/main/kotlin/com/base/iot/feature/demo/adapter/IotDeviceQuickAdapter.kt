// [DEMO_FILE] Reference demo only. Auto-purge on formal development.
package com.base.iot.feature.demo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.graphics.toArgb
import androidx.recyclerview.widget.RecyclerView
import com.base.iot.R
import com.base.iot.core.ui.theme.DarkAppColors
import com.base.iot.core.ui.theme.LightAppColors
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * 物联网设备数据模型（供 BRVAH 列表渲染）
 */
data class IotDeviceItem(
    val deviceId: String,
    val name: String,
    val protocol: String,
    val status: String,
    val ipAddress: String,
    val lastSeen: Long = System.currentTimeMillis()
)

/**
 * 基于 GitHub 24k+ Star 最流行 RecyclerView 框架 BRVAH 4 (BaseRecyclerViewAdapterHelper4)
 * 极简打造的企业级物联网设备列表适配器。
 */
class IotDeviceQuickAdapter(
    var isDark: Boolean = true
) : BaseQuickAdapter<IotDeviceItem, IotDeviceQuickAdapter.VH>() {

    class VH(
        val rootView: View,
        val tvName: TextView,
        val tvProtocol: TextView,
        val tvStatus: TextView,
        val tvIp: TextView
    ) : RecyclerView.ViewHolder(rootView)

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }

        val row1 = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
        }

        val tvName = TextView(context).apply {
            textSize = 15f
            layoutParams = android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvProtocol = TextView(context).apply {
            textSize = 12f
            setPadding(16, 4, 16, 4)
        }

        row1.addView(tvName)
        row1.addView(tvProtocol)

        val row2 = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
            }
        }

        val tvStatus = TextView(context).apply {
            textSize = 13f
            layoutParams = android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvIp = TextView(context).apply {
            textSize = 12f
        }

        row2.addView(tvStatus)
        row2.addView(tvIp)

        layout.addView(row1)
        layout.addView(row2)

        return VH(layout, tvName, tvProtocol, tvStatus, tvIp)
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: IotDeviceItem?) {
        if (item == null) return

        val palette = if (isDark) DarkAppColors else LightAppColors
        val bgColor = palette.surface.toArgb()
        val nameColor = palette.textPrimary.toArgb()
        val ipColor = palette.textSecondary.toArgb()
        val protocolColor = palette.accentCyan.toArgb()
        val protocolBgColor = palette.accentCyan.copy(alpha = 0.15f).toArgb()
        val context = holder.rootView.context
        val statusText = if (item.status == "ONLINE") context.getString(R.string.status_online) else context.getString(R.string.status_offline)
        val statusColor = if (item.status == "ONLINE") palette.accentGreen.toArgb() else palette.accentRed.toArgb()

        holder.rootView.setBackgroundColor(bgColor)
        holder.tvName.text = item.name
        holder.tvName.setTextColor(nameColor)

        holder.tvProtocol.text = item.protocol
        holder.tvProtocol.setTextColor(protocolColor)
        holder.tvProtocol.setBackgroundColor(protocolBgColor)

        holder.tvStatus.text = context.getString(R.string.device_status_dot_format, statusText)
        holder.tvStatus.setTextColor(statusColor)

        holder.tvIp.text = context.getString(R.string.device_ip_desc_format, item.ipAddress, item.deviceId)
        holder.tvIp.setTextColor(ipColor)
    }
}
