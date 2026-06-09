package com.v2ray.ang.ui

import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.v2ray.ang.contracts.BaseAdapterListener
import com.v2ray.ang.databinding.ItemRecyclerSubSettingBinding
import com.v2ray.ang.helper.ItemTouchHelperAdapter
import com.v2ray.ang.helper.ItemTouchHelperViewHolder
import com.v2ray.ang.util.Utils
import com.v2ray.ang.viewmodel.SubscriptionsViewModel

class SubSettingRecyclerAdapter(
    private val viewModel: SubscriptionsViewModel,
    private val adapterListener: BaseAdapterListener?
) : RecyclerView.Adapter<SubSettingRecyclerAdapter.MainViewHolder>(), ItemTouchHelperAdapter {

    override fun getItemCount() = viewModel.getAll().size

override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
    val subscriptions = viewModel.getAll()
    val subId = subscriptions[position].guid
    val subItem = subscriptions[position].subscription
    holder.itemSubSettingBinding.tvName.text = subItem.remarks
    holder.itemSubSettingBinding.tvUrl.text = subItem.url
    holder.itemSubSettingBinding.chkEnable.isChecked = subItem.enabled
    holder.itemSubSettingBinding.tvLastUpdated.text = Utils.formatTimestamp(subItem.lastUpdated)
    holder.itemView.setBackgroundColor(Color.TRANSPARENT)

    // Для постоянной группы: скрываем URL и блокируем редактирование, но оставляем удаление
    if (subItem.isPermanent) {
        // Скрываем URL и связанные элементы
        holder.itemSubSettingBinding.layoutUrl.visibility = View.GONE
        holder.itemSubSettingBinding.layoutShare.visibility = View.GONE
        holder.itemSubSettingBinding.layoutLastUpdated.visibility = View.GONE
        holder.itemSubSettingBinding.chkEnable.visibility = View.GONE
        
        // Кнопка удаления - ВИДИМА!
        holder.itemSubSettingBinding.layoutRemove.visibility = View.VISIBLE
        
        // Редактирование - запрещено
        holder.itemSubSettingBinding.layoutEdit.isEnabled = false
        holder.itemSubSettingBinding.layoutEdit.alpha = 0.5f
        
        // Убираем возможность нажать на саму строку для редактирования
        holder.itemSubSettingBinding.layoutEdit.setOnClickListener(null)
    } else {
        // Обычные группы - всё как обычно
        holder.itemSubSettingBinding.layoutUrl.visibility = View.VISIBLE
        holder.itemSubSettingBinding.layoutShare.visibility = View.VISIBLE
        holder.itemSubSettingBinding.layoutLastUpdated.visibility = View.VISIBLE
        holder.itemSubSettingBinding.chkEnable.visibility = View.VISIBLE
        holder.itemSubSettingBinding.layoutRemove.visibility = View.VISIBLE
        holder.itemSubSettingBinding.layoutEdit.isEnabled = true
        holder.itemSubSettingBinding.layoutEdit.alpha = 1f
        
        holder.itemSubSettingBinding.layoutEdit.setOnClickListener {
            adapterListener?.onEdit(subId, position)
        }
    }

    // Кнопка удаления работает для всех (включая постоянные)
    holder.itemSubSettingBinding.layoutRemove.setOnClickListener {
        adapterListener?.onRemove(subId, position)
    }

    // Остальной код для URL и т.д. (только для непостоянных)
    if (!subItem.isPermanent && TextUtils.isEmpty(subItem.url)) {
        holder.itemSubSettingBinding.layoutUrl.visibility = View.GONE
        holder.itemSubSettingBinding.layoutShare.visibility = View.INVISIBLE
        holder.itemSubSettingBinding.chkEnable.visibility = View.INVISIBLE
        holder.itemSubSettingBinding.layoutLastUpdated.visibility = View.INVISIBLE
    } else if (!subItem.isPermanent) {
        holder.itemSubSettingBinding.layoutShare.setOnClickListener {
            adapterListener?.onShare(subItem.url)
        }
    }
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            ItemRecyclerSubSettingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    class MainViewHolder(val itemSubSettingBinding: ItemRecyclerSubSettingBinding) :
        BaseViewHolder(itemSubSettingBinding.root), ItemTouchHelperViewHolder

    open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        fun onItemClear() {
            itemView.setBackgroundColor(0)
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        viewModel.swap(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemMoveCompleted() {
        adapterListener?.onRefreshData()
    }

    override fun onItemDismiss(position: Int) {
    }
}
