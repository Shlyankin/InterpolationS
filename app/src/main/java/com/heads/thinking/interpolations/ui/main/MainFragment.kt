package com.heads.thinking.interpolations.ui.main

import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.heads.thinking.interpolations.R
import kotlinx.android.synthetic.main.main_fragment.*
import net.objecthunter.exp4j.ExpressionBuilder
import java.lang.Exception
import java.lang.NumberFormatException

class MainFragment : Fragment(), View.OnClickListener {

    private lateinit var viewModel: MainViewModel
    private var callback : OnFragmentInteractionListener? = null

    override fun onClick(view: View) {
        when(view.id) {
            R.id.infoBtn -> {
                createInfoDialog().show()
            }
            R.id.calculateInterpolationBtn -> {
                if(checkForms())
                        callback?.onCalculateInterpolationClick()
            }
        }
    }

    private fun checkForms() : Boolean {
        try {
            val function = functionET.text.toString()
            val interval = intervalET.text.toString().toDouble()
            val step = stepET.text.toString().toDouble()
            viewModel.expression = ExpressionBuilder(function).variable("x").build()
            return true
        } catch (exc : Exception) {
            Toast.makeText(this.context, exc.message, Toast.LENGTH_LONG).show()
            return false
        }
    }

    companion object {
        fun newInstance() = MainFragment()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        infoBtn.setOnClickListener(this)
        calculateInterpolationBtn.setOnClickListener(this)
        viewModel = ViewModelProviders.of(this.activity!!).get(MainViewModel::class.java)
        if(viewModel.step != null)
            stepET.setText(viewModel.step.toString())
        if(viewModel.function != null)
            functionET.setText(viewModel.function)
        if(viewModel.interval != null)
            intervalET.setText(viewModel.interval.toString())
    }

    override fun onPause() {
        super.onPause()
        viewModel.function = functionET.text.toString()
        if (!intervalET.text.isEmpty())
            viewModel.interval = intervalET.text.toString().toDouble()
        if (!stepET.text.isEmpty())
            viewModel.step = stepET.text.toString().toDouble()
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    interface OnFragmentInteractionListener {
        fun onCalculateInterpolationClick()
    }

    fun setOnFragmentInteractionListener(callback :OnFragmentInteractionListener ) {
        this.callback = callback
    }

    private fun createInfoDialog() : AlertDialog {
        val builder : AlertDialog.Builder = AlertDialog.Builder(this.context)
        builder.setTitle("FAQ")
        builder.setView(this.layoutInflater.inflate(R.layout.info_dialog, null))
                .setPositiveButton("OK", null)
        return builder.create()
    }
}
