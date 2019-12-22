/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.tool.calculator

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentCalculatorBinding
import jp.toastkid.yobidashi.libs.clip.Clipboard

/**
 * TODO clean up code.
 * @author toastkidjp
 */
class CalculatorDialogFragment : DialogFragment() {

    private var binding: FragmentCalculatorBinding? = null

    private var totalValue: Double = 0.0

    private var displayText = ""

    private var lastOperator = Operator.NONE

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = context ?: return super.onCreateDialog(savedInstanceState)
        val inflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_calculator,
                null,
                false
        )

        binding?.fragment = this

        return AlertDialog.Builder(context)
                .setTitle(R.string.title_calculator)
                .setView(binding?.root)
                .create()
    }

    fun plus() {
        val currentValue = extractCurrentValue()
        totalValue += currentValue
        commitInput("${if (isInputEmpty()) "" else "+"} $currentValue")
        lastOperator = Operator.PLUS
    }

    fun minus() {
        val currentValue = extractCurrentValue()
        totalValue -= currentValue
        commitInput("${if (isInputEmpty()) "" else "-"} $currentValue")
        lastOperator = Operator.MINUS
    }

    fun multiply() {
        val currentValue = extractCurrentValue()
        totalValue *= currentValue
        commitInput("${if (isInputEmpty()) "" else "*"} $currentValue")
        lastOperator = Operator.MULTIPLY
    }

    fun divide() {
        val currentValue = extractCurrentValue()
        totalValue /= currentValue
        commitInput("${if (isInputEmpty()) "" else "/"} $currentValue")
        lastOperator = Operator.DIVIDE
    }

    fun power() {
        val currentTotal = totalValue
        totalValue *= currentTotal
        commitInput("${if (isInputEmpty()) "" else "*"} $currentTotal")
        lastOperator = Operator.POWER
    }

    fun clip() {
        Clipboard.clip(requireContext(), totalValue.toString())
    }

    fun clear() {
        totalValue = 0.0
        binding?.display?.text = ""
        displayText = ""
        lastOperator = Operator.NONE
        setEmpty()
    }

    fun equal() {
        when (lastOperator) {
            Operator.PLUS -> plus()
            Operator.MINUS -> minus()
            Operator.MULTIPLY -> multiply()
            Operator.DIVIDE -> divide()
            Operator.POWER -> power()
            Operator.NONE -> Unit
        }
        lastOperator = Operator.NONE
        binding?.input?.setText(totalValue.toString())
    }

    private fun isInputEmpty() = binding?.display?.text?.isEmpty() == true

    private fun commitInput(additionalText: String) {
        displayText += additionalText
        binding?.display?.text = displayText
        setEmpty()
    }

    private fun setEmpty() {
        binding?.input?.setText("")
    }

    private fun extractCurrentValue(): Double {
        val toString = binding?.input?.text?.toString()
        if (toString.isNullOrBlank()) {
            return 0.0
        }
        return toString.toDouble()
    }

}