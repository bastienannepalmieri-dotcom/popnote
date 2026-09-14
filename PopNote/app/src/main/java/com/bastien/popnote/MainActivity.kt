package com.bastien.popnote

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bastien.popnote.accessibility.AppWatcherService
import com.bastien.popnote.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AppListAdapter(
            onSelectionChanged = { app, selected ->
                NotesRepository.setAppSelected(this, app.packageName, selected)
                updateAppInList(app.packageName) { it.copy(isSelected = selected) }
            },
            onRowClicked = { app ->
                val intent = Intent(this, NoteEditActivity::class.java)
                intent.putExtra(NoteEditActivity.EXTRA_PACKAGE, app.packageName)
                intent.putExtra(NoteEditActivity.EXTRA_LABEL, app.label)
                startActivity(intent)
            }
        )

        binding.recyclerApps.layoutManager = LinearLayoutManager(this)
        binding.recyclerApps.adapter = adapter
        binding.recyclerApps.setHasFixedSize(true)

        binding.btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.btnOverlay.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                )
            } else {
                Toast.makeText(this, "Déjà autorisé", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateButtonStates()
        loadApps()
    }

    /** Charge la liste des apps hors du thread principal pour ne jamais bloquer l'UI. */
    private fun loadApps() {
        lifecycleScope.launch {
            val list = withContext(Dispatchers.Default) { loadInstalledApps() }
            adapter.submitList(list)
        }
    }

    private fun updateAppInList(packageName: String, transform: (AppInfo) -> AppInfo) {
        val current = adapter.currentList.toMutableList()
        val index = current.indexOfFirst { it.packageName == packageName }
        if (index != -1) {
            current[index] = transform(current[index])
            adapter.submitList(current)
        }
    }

    private fun updateButtonStates() {
        val accessibilityOn = isAccessibilityServiceEnabled()
        val overlayOn = Settings.canDrawOverlays(this)
        binding.btnAccessibility.text = getString(R.string.enable_accessibility) +
            "  (" + getString(if (accessibilityOn) R.string.status_enabled else R.string.status_disabled) + ")"
        binding.btnOverlay.text = getString(R.string.enable_overlay) +
            "  (" + getString(if (overlayOn) R.string.status_enabled else R.string.status_disabled) + ")"
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponent = ComponentName(this, AppWatcherService::class.java)
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            val componentName = ComponentName.unflattenFromString(splitter.next())
            if (componentName != null && componentName == expectedComponent) {
                return true
            }
        }
        return false
    }

    private fun loadInstalledApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        val selected = NotesRepository.getSelectedApps(this)

        return resolved
            .distinctBy { it.activityInfo.packageName }
            .filter { it.activityInfo.packageName != packageName } // exclure PopNote elle-même
            .map { resolveInfo ->
                val pkg = resolveInfo.activityInfo.packageName
                AppInfo(
                    packageName = pkg,
                    label = resolveInfo.loadLabel(pm).toString(),
                    icon = IconCache.getOrLoad(pkg) { resolveInfo.loadIcon(pm) },
                    isSelected = selected.contains(pkg),
                    hasNote = NotesRepository.hasNote(this, pkg)
                )
            }
            .sortedBy { it.label.lowercase() }
    }
}
