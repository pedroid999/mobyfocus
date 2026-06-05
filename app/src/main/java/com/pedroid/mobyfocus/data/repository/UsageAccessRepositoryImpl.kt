package com.pedroid.mobyfocus.data.repository

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Process
import com.pedroid.mobyfocus.domain.model.UsageAccessPermissionStatus
import com.pedroid.mobyfocus.domain.repository.UsageAccessRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Reads Usage Access via [AppOpsManager] — the official, AppOps-gated check for
 * the special `PACKAGE_USAGE_STATS` permission (Constitution Principle I).
 */
class UsageAccessRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : UsageAccessRepository {

    @Suppress("DEPRECATION")
    override fun getPermissionStatus(): UsageAccessPermissionStatus {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        }
        return if (mode == AppOpsManager.MODE_ALLOWED) {
            UsageAccessPermissionStatus.GRANTED
        } else {
            UsageAccessPermissionStatus.NOT_GRANTED
        }
    }
}
