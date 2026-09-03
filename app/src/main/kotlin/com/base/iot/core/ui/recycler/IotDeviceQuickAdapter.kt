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
class IotDeviceQuickAdapter : BaseQuickAdapter<IotDeviceItem, IotDeviceQuickAdapter.VH>() {

    class VH(
        val rootView: View,
        val tvName: TextView,
        val tvProtocol: TextView,
        val tvStatus: TextView,
        val tvIp: TextView
    ) : RecyclerView.ViewHolder(rootView)

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        // 使用代码构建布局或 XML 均可，此处代码构建免去额外 XML 开销并保持 100% 独立可运行
        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            setBackgroundColor(0xFF1E2640.toInt()) // 深色卡片背景
        }

        val row1 = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
        }

        val tvName = TextView(context).apply {
            textSize = 15f
            setTextColor(0xFFFFFFFF.toInt())
            layoutParams = android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvProtocol = TextView(context).apply {
            textSize = 12f
            setPadding(16, 4, 16, 4)
            setTextColor(0xFF00E5FF.toInt())
            setBackgroundColor(0x2200E5FF)
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
            setTextColor(0xFF00E676.toInt())
            layoutParams = android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvIp = TextView(context).apply {
            textSize = 12f
            setTextColor(0xFF94A3B8.toInt())
        }

        row2.addView(tvStatus)
        row2.addView(tvIp)

        layout.addView(row1)
        layout.addView(row2)

        return VH(layout, tvName, tvProtocol, tvStatus, tvIp)
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: IotDeviceItem?) {
        if (item == null) return
        holder.tvName.text = item.name
        holder.tvProtocol.text = item.protocol
        holder.tvStatus.text = "● ${item.status}"
        holder.tvStatus.setTextColor(
            if (item.status == "ONLINE") 0xFF00E676.toInt() else 0xFFFF5252.toInt()
        )
        holder.tvIp.text = "${item.ipAddress} (${item.deviceId})"
    }
}
