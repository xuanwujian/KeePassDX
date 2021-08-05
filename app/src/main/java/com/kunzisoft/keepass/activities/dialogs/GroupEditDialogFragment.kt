/*
 * Copyright 2019 Jeremy Jamet / Kunzisoft.
 *     
 * This file is part of KeePassDX.
 *
 *  KeePassDX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  KeePassDX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePassDX.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.kunzisoft.keepass.activities.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import com.google.android.material.textfield.TextInputLayout
import com.kunzisoft.keepass.R
import com.kunzisoft.keepass.activities.dialogs.GroupEditDialogFragment.EditGroupDialogAction.CREATION
import com.kunzisoft.keepass.activities.dialogs.GroupEditDialogFragment.EditGroupDialogAction.UPDATE
import com.kunzisoft.keepass.activities.fragments.DatabaseDialogFragment
import com.kunzisoft.keepass.database.element.Database
import com.kunzisoft.keepass.database.element.DateInstant
import com.kunzisoft.keepass.database.element.icon.IconImage
import com.kunzisoft.keepass.model.GroupInfo
import com.kunzisoft.keepass.view.DateTimeEditFieldView
import com.kunzisoft.keepass.viewmodels.GroupEditViewModel
import org.joda.time.DateTime

class GroupEditDialogFragment : DatabaseDialogFragment() {

    private var mEditGroupListener: EditGroupListener? = null
    private val mGroupEditViewModel: GroupEditViewModel by activityViewModels()

    private var populateIconMethod: ((ImageView, IconImage) -> Unit)? = null
    private var mEditGroupDialogAction = EditGroupDialogAction.NONE
    private var mGroupInfo = GroupInfo()

    private lateinit var iconButtonView: ImageView
    private var mIconColor: Int = 0
    private lateinit var nameTextLayoutView: TextInputLayout
    private lateinit var nameTextView: TextView
    private lateinit var notesTextLayoutView: TextInputLayout
    private lateinit var notesTextView: TextView
    private lateinit var expirationView: DateTimeEditFieldView

    enum class EditGroupDialogAction {
        CREATION, UPDATE, NONE;

        companion object {
            fun getActionFromOrdinal(ordinal: Int): EditGroupDialogAction {
                return values()[ordinal]
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mEditGroupListener = context as EditGroupListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                context.toString()
                        + " must implement " + GroupEditDialogFragment::class.java.name
            )
        }
    }

    override fun onDetach() {
        mEditGroupListener = null
        super.onDetach()
    }

    override fun onDatabaseRetrieved(database: Database?) {
        populateIconMethod = { imageView, icon ->
            database?.iconDrawableFactory?.assignDatabaseIcon(imageView, icon, mIconColor)
        }
        populateIconMethod?.invoke(iconButtonView, mGroupInfo.icon)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mGroupEditViewModel.onIconSelected.observe(this) { iconImage ->
            mGroupInfo.icon = iconImage
            populateIconMethod?.invoke(iconButtonView, mGroupInfo.icon)
        }

        mGroupEditViewModel.onDateSelected.observe(this) { viewModelDate ->
            // Save the date
            mGroupInfo.expiryTime = DateInstant(
                DateTime(mGroupInfo.expiryTime.date)
                    .withYear(viewModelDate.year)
                    .withMonthOfYear(viewModelDate.month + 1)
                    .withDayOfMonth(viewModelDate.day)
                    .toDate())
            expirationView.dateTime = mGroupInfo.expiryTime
            if (expirationView.dateTime.type == DateInstant.Type.DATE_TIME) {
                val instantTime = DateInstant(mGroupInfo.expiryTime.date, DateInstant.Type.TIME)
                // Trick to recall selection with time
                mGroupEditViewModel.requestDateTimeSelection(instantTime)
            }
        }

        mGroupEditViewModel.onTimeSelected.observe(this) { viewModelTime ->
            // Save the time
            mGroupInfo.expiryTime = DateInstant(
                DateTime(mGroupInfo.expiryTime.date)
                    .withHourOfDay(viewModelTime.hours)
                    .withMinuteOfHour(viewModelTime.minutes)
                    .toDate(), mGroupInfo.expiryTime.type)
            expirationView.dateTime = mGroupInfo.expiryTime
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let { activity ->
            val root = activity.layoutInflater.inflate(R.layout.fragment_group_edit, null)
            iconButtonView = root.findViewById(R.id.group_edit_icon_button)
            nameTextLayoutView = root.findViewById(R.id.group_edit_name_container)
            nameTextView = root.findViewById(R.id.group_edit_name)
            notesTextLayoutView = root.findViewById(R.id.group_edit_note_container)
            notesTextView = root.findViewById(R.id.group_edit_note)
            expirationView = root.findViewById(R.id.group_edit_expiration)

            // Retrieve the textColor to tint the icon
            val ta = activity.theme.obtainStyledAttributes(intArrayOf(android.R.attr.textColor))
            mIconColor = ta.getColor(0, Color.WHITE)
            ta.recycle()

            if (savedInstanceState != null
                    && savedInstanceState.containsKey(KEY_ACTION_ID)
                    && savedInstanceState.containsKey(KEY_GROUP_INFO)) {
                mEditGroupDialogAction = EditGroupDialogAction.getActionFromOrdinal(savedInstanceState.getInt(KEY_ACTION_ID))
                mGroupInfo = savedInstanceState.getParcelable(KEY_GROUP_INFO) ?: mGroupInfo
            } else {
                arguments?.apply {
                    if (containsKey(KEY_ACTION_ID))
                        mEditGroupDialogAction = EditGroupDialogAction.getActionFromOrdinal(getInt(KEY_ACTION_ID))
                    if (containsKey(KEY_GROUP_INFO)) {
                        mGroupInfo = getParcelable(KEY_GROUP_INFO) ?: mGroupInfo
                    }
                }
            }

            // populate info in views
            populateInfoToViews(mGroupInfo)

            iconButtonView.setOnClickListener { _ ->
                mGroupEditViewModel.requestIconSelection(mGroupInfo.icon)
            }
            expirationView.setOnDateClickListener = { dateInstant ->
                mGroupEditViewModel.requestDateTimeSelection(dateInstant)
            }

            val builder = AlertDialog.Builder(activity)
            builder.setView(root)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel) { _, _ ->
                        retrieveGroupInfoFromViews()
                        mEditGroupListener?.cancelEditGroup(
                                mEditGroupDialogAction,
                                mGroupInfo)
                    }

            return builder.create()
        }
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        // To prevent auto dismiss
        val d = dialog as AlertDialog?
        if (d != null) {
            val positiveButton = d.getButton(Dialog.BUTTON_POSITIVE) as Button
            positiveButton.setOnClickListener {
                retrieveGroupInfoFromViews()
                if (isValid()) {
                    mEditGroupListener?.approveEditGroup(
                            mEditGroupDialogAction,
                            mGroupInfo)
                    d.dismiss()
                }
            }
        }
    }

    private fun populateInfoToViews(groupInfo: GroupInfo) {
        mGroupEditViewModel.selectIcon(groupInfo.icon)
        nameTextView.text = groupInfo.title
        notesTextLayoutView.visibility = if (groupInfo.notes == null) View.GONE else View.VISIBLE
        groupInfo.notes?.let {
            notesTextView.text = it
        }
        expirationView.activation = groupInfo.expires
        expirationView.dateTime = groupInfo.expiryTime
    }

    private fun retrieveGroupInfoFromViews() {
        mGroupInfo.title = nameTextView.text.toString()
        // Only if there
        val newNotes = notesTextView.text.toString()
        if (newNotes.isNotEmpty()) {
            mGroupInfo.notes = newNotes
        }
        mGroupInfo.expires = expirationView.activation
        mGroupInfo.expiryTime = expirationView.dateTime
    }

    override fun onSaveInstanceState(outState: Bundle) {
        retrieveGroupInfoFromViews()
        outState.putInt(KEY_ACTION_ID, mEditGroupDialogAction.ordinal)
        outState.putParcelable(KEY_GROUP_INFO, mGroupInfo)
        super.onSaveInstanceState(outState)
    }

    private fun isValid(): Boolean {
        val error = mEditGroupListener?.isValidGroupName(nameTextView.text.toString()) ?: Error(false, null)
        error.messageId?.let { messageId ->
            nameTextLayoutView.error = getString(messageId)
        } ?: kotlin.run {
            nameTextLayoutView.error = null
        }
        return !error.isError
    }

    data class Error(val isError: Boolean, val messageId: Int?)

    interface EditGroupListener {
        fun isValidGroupName(name: String): Error
        fun approveEditGroup(action: EditGroupDialogAction,
                             groupInfo: GroupInfo)
        fun cancelEditGroup(action: EditGroupDialogAction,
                            groupInfo: GroupInfo)
    }

    companion object {

        const val TAG_CREATE_GROUP = "TAG_CREATE_GROUP"
        const val KEY_ACTION_ID = "KEY_ACTION_ID"
        const val KEY_GROUP_INFO = "KEY_GROUP_INFO"

        fun create(groupInfo: GroupInfo): GroupEditDialogFragment {
            val bundle = Bundle()
            bundle.putInt(KEY_ACTION_ID, CREATION.ordinal)
            bundle.putParcelable(KEY_GROUP_INFO, groupInfo)
            val fragment = GroupEditDialogFragment()
            fragment.arguments = bundle
            return fragment
        }

        fun update(groupInfo: GroupInfo): GroupEditDialogFragment {
            val bundle = Bundle()
            bundle.putInt(KEY_ACTION_ID, UPDATE.ordinal)
            bundle.putParcelable(KEY_GROUP_INFO, groupInfo)
            val fragment = GroupEditDialogFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
