/*
 * Copyright 2019 Jeremy Jamet / Kunzisoft.
 *     
 * This file is part of KeePass DX.
 *
 *  KeePass DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  KeePass DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePass DX.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.kunzisoft.keepass.settings

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.kunzisoft.keepass.R
import com.kunzisoft.keepass.database.SortNodeEnum
import com.kunzisoft.keepass.timeout.TimeoutHelper
import java.util.*

object PreferencesUtil {

    private const val NO_BACKUP_PREFERENCE_FILE_NAME = "nobackup"

    fun getNoBackupSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(
                NO_BACKUP_PREFERENCE_FILE_NAME,
                Context.MODE_PRIVATE)
    }

    fun deleteAllValuesFromNoBackupPreferences(context: Context) {
        val prefsNoBackup = getNoBackupSharedPreferences(context)
        val sharedPreferencesEditor = prefsNoBackup.edit()
        sharedPreferencesEditor.clear()
        sharedPreferencesEditor.apply()
    }

    fun omitBackup(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.omitbackup_key),
                context.resources.getBoolean(R.bool.omitbackup_default))
    }

    fun showUsernamesListEntries(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.list_entries_show_username_key),
                context.resources.getBoolean(R.bool.list_entries_show_username_default))
    }

    /**
     * Retrieve the text size in SP, verify the integrity of the size stored in preference
     */
    fun getListTextSize(context: Context): Float {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val defaultSizeString = context.getString(R.string.list_size_default)
        var listSize = prefs.getString(context.getString(R.string.list_size_key), defaultSizeString)
        if (!listOf(*context.resources.getStringArray(R.array.list_size_values)).contains(listSize))
            listSize = defaultSizeString
        return java.lang.Float.parseFloat(listSize)
    }

    fun getDefaultPasswordLength(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(context.getString(R.string.password_length_key),
                Integer.parseInt(context.getString(R.string.default_password_length)))
    }

    fun getDefaultPasswordCharacters(context: Context): Set<String>? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getStringSet(context.getString(R.string.list_password_generator_options_key),
                HashSet(listOf(*context.resources
                                .getStringArray(R.array.list_password_generator_options_default_values))))
    }

    fun isClipboardNotificationsEnable(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.clipboard_notifications_key),
                context.resources.getBoolean(R.bool.clipboard_notifications_default))
    }

    /**
     * Save current time, can be retrieve with `getTimeSaved()`
     */
    fun saveCurrentTime(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
            putLong(context.getString(R.string.timeout_backup_key), System.currentTimeMillis())
            apply()
        }
    }

    /**
     * Time previously saved in milliseconds (commonly used to compare with current time and check timeout)
     */
    fun getTimeSaved(context: Context): Long {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getLong(context.getString(R.string.timeout_backup_key),
                TimeoutHelper.NEVER)
    }

    /**
     * App timeout selected in milliseconds
     */
    fun getAppTimeout(context: Context): Long {
        return try {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            java.lang.Long.parseLong(prefs.getString(context.getString(R.string.app_timeout_key),
                    context.getString(R.string.clipboard_timeout_default)))
        } catch (e: NumberFormatException) {
            TimeoutHelper.DEFAULT_TIMEOUT
        }
    }

    fun isLockDatabaseWhenScreenShutOffEnable(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.lock_database_screen_off_key),
                context.resources.getBoolean(R.bool.lock_database_screen_off_default))
    }

    fun isLockDatabaseWhenBackButtonOnRootClicked(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.lock_database_back_root_key),
                context.resources.getBoolean(R.bool.lock_database_back_root_default))
    }

    fun isFingerprintEnable(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.fingerprint_enable_key),
                context.resources.getBoolean(R.bool.fingerprint_enable_default))
    }

    fun isFullFilePathEnable(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.full_file_path_enable_key),
                context.resources.getBoolean(R.bool.full_file_path_enable_default))
    }

    fun getListSort(context: Context): SortNodeEnum {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.getString(context.getString(R.string.sort_node_key),
                SortNodeEnum.TITLE.name)?.let {
            return SortNodeEnum.valueOf(it)
        }
        return SortNodeEnum.DB
    }

    fun getGroupsBeforeSort(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.sort_group_before_key),
                context.resources.getBoolean(R.bool.sort_group_before_default))
    }

    fun getAscendingSort(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.sort_ascending_key),
                context.resources.getBoolean(R.bool.sort_ascending_default))
    }

    fun getRecycleBinBottomSort(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.sort_recycle_bin_bottom_key),
                context.resources.getBoolean(R.bool.sort_recycle_bin_bottom_default))
    }

    fun isPasswordMask(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.maskpass_key),
                context.resources.getBoolean(R.bool.maskpass_default))
    }

    fun fieldFontIsInVisibility(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.monospace_font_fields_enable_key),
                context.resources.getBoolean(R.bool.monospace_font_fields_enable_default))
    }

    fun autoOpenSelectedFile(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.auto_open_file_uri_key),
                context.resources.getBoolean(R.bool.auto_open_file_uri_default))
    }

    fun isFirstTimeAskAllowCopyPasswordAndProtectedFields(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.allow_copy_password_first_time_key),
                context.resources.getBoolean(R.bool.allow_copy_password_first_time_default))
    }

    fun allowCopyPasswordAndProtectedFields(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.allow_copy_password_key),
                context.resources.getBoolean(R.bool.allow_copy_password_default))
    }

    fun setAllowCopyPasswordAndProtectedFields(context: Context, allowCopy: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit()
                .putBoolean(context.getString(R.string.allow_copy_password_first_time_key), false)
                .putBoolean(context.getString(R.string.allow_copy_password_key), allowCopy)
                .apply()
    }

    fun getIconPackSelectedId(context: Context): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(
                context.getString(R.string.setting_icon_pack_choose_key),
                context.getString(R.string.setting_icon_pack_choose_default))
    }

    fun emptyPasswordAllowed(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.allow_no_password_key),
                context.resources.getBoolean(R.bool.allow_no_password_default))
    }

    fun enableReadOnlyDatabase(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.enable_read_only_key),
                context.resources.getBoolean(R.bool.enable_read_only_default))
    }

    fun enableKeyboardNotificationEntry(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.keyboard_notification_entry_key),
                context.resources.getBoolean(R.bool.keyboard_notification_entry_default))
    }

    fun enableKeyboardVibration(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.keyboard_key_vibrate_key),
                context.resources.getBoolean(R.bool.keyboard_key_vibrate_default))
    }

    fun enableKeyboardSound(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context.getString(R.string.keyboard_key_sound_key),
                context.resources.getBoolean(R.bool.keyboard_key_sound_default))
    }
}
