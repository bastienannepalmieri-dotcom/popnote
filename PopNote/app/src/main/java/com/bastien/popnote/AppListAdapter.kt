package com.bastien.popnote

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bastien.popnote.databinding.ItemAppBinding

/**
 * ListAdapter + DiffUtil : seules les lignes réellement modifiées sont
 * redessinées (au lieu de tout le RecyclerView à chaque changement).
 */
class AppListAdapter(
    private val onSelectionChanged: (AppInfo, Boolean) -> Unit,
    private val onRowClicked: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppListAdapter.AppViewHolder>(DIFF_CALLBACK) {

    inner class AppViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = getItem(position)
        val b = holder.binding

        b.imgIcon.setImageDrawable(app.icon)
        b.tvAppName.text = app.label
        b.tvNotePreview.text = if (app.hasNote) "📝 Note enregistrée" else "Aucune note"

        // Éviter que le listener se redéclenche lors du bind (recyclage de vue)
        b.checkSelected.setOnCheckedChangeListener(null)
        b.checkSelected.isChecked = app.isSelected
        b.checkSelected.setOnCheckedChangeListener { _, isChecked ->
            onSelectionChanged(app, isChecked)
        }

        b.root.setOnClickListener { onRowClicked(app) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AppInfo>() {
            override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo) =
                oldItem.packageName == newItem.packageName

            override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo) =
                oldItem == newItem
        }
    }
}
