package com.base.iot.core.ui.recycler

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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

        val bgColor = if (isDark) 0xFF161B26.toInt() else 0xFFF1F5F9.toInt()
        val nameColor = if (isDark) 0xFFF8FAFC.toInt() else 0xFF0F172A.toInt()
        val ipColor = if (isDark) 0xFF94A3B8.toInt() else 0xFF64748B.toInt()
        val protocolColor = if (isDark) 0xFF00E5FF.toInt() else 0xFF0284C7.toInt()

        holder.rootView.setBackgroundColor(bgColor)
        holder.tvName.text = item.name
        holder.tvName.setTextColor(nameColor)

        holder.tvProtocol.text = item.protocol
        holder.tvProtocol.setTextColor(protocolColor)
        holder.tvProtocol.setBackgroundColor(if (isDark) 0x2200E5FF else 0x1A0284C7)

        holder.tvStatus.text = "● ${item.status}"
        holder.tvStatus.setTextColor(
            if (item.status == "ONLINE") {
                if (isDark) 0xFF10B981.toInt() else 0xFF059669.toInt()
            } else {
                if (isDark) 0xFFEF4444.toInt() else 0xFFDC2626.toInt()
            }
        )

        holder.tvIp.text = "${item.ipAddress} (${item.deviceId})"
        holder.tvIp.setTextColor(ipColor)
    }
}
