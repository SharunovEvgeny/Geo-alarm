package com.bignerdranch.android.geo_alarm.Maps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bignerdranch.android.geo_alarm.R
import com.bignerdranch.android.geo_alarm.databinding.MapsDialogFragmentBinding


class MapsDialogFragment(private val address: String) : DialogFragment(R.layout.maps_dialog_fragment) {

    private var _binding: MapsDialogFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MapsDialogFragmentBinding.inflate(inflater, container, false)
        initViews()
        return binding.root
    }

    private fun initViews() {
        binding.address.text = address
        binding.positive.setOnClickListener {
            val boolList = mutableListOf(
                binding.pn.isChecked,
                binding.vt.isChecked,
                binding.sr.isChecked,
                binding.cht.isChecked,
                binding.pt.isChecked,
                binding.sb.isChecked,
                binding.vs.isChecked,
            )
            (activity as MapsActivity?)?.onPositiveDialogButton(boolList)
            dialog?.dismiss()
        }
        binding.negative.setOnClickListener {
            dialog?.dismiss()
        }
    }

}