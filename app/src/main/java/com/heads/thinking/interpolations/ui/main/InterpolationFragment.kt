package com.heads.thinking.interpolations.ui.main

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.heads.thinking.interpolations.R
import com.heads.thinking.interpolations.custom.math.methods.interpolation.*
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_interpolation.*
import net.objecthunter.exp4j.Expression
import java.lang.NumberFormatException
import java.lang.StringBuilder
import kotlin.math.absoluteValue
import android.graphics.DashPathEffect
import android.graphics.Paint
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.Series


class InterpolationFragment : Fragment(), View.OnClickListener {

    private val graphColors = intArrayOf(Color.rgb(34, 139, 34), Color.rgb(255, 0, 0), Color.rgb(255, 20, 147), Color.rgb(255, 69, 0), Color.rgb(0, 0, 139), Color.rgb(0, 128, 128))
    private var graphs1Series : ArrayList <Pair <String, Series<DataPoint>>> = ArrayList()
    private var graphs2Series : ArrayList <Pair <String, Series<DataPoint>>> = ArrayList()

    private var interpolations : ArrayList<Pair<String, ((x : Double) -> Double)>>? = null


    private lateinit var viewModel: MainViewModel
    private var callback: OnFragmentInteractionListener? = null

    private var step : Double? = null
    private var interval : Double? = null
    private var function : String? = null

    private var expression : Expression? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_interpolation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this.activity!!).get(MainViewModel::class.java)
        expression = viewModel.expression
        if (viewModel.arg != null)
            argET.setText(viewModel.arg)
        if (viewModel.interpolationValue != null)
            interpolationValueTV.text = viewModel.interpolationValue
        if(viewModel.interpolationError != null)
            interpolationErrorTV.text = viewModel.interpolationError
        if(viewModel.step != null)
            step = viewModel.step
        if(viewModel.interval != null)
            interval = viewModel.interval
        val tabulation = ArrayList<Pair<Double, Double>>()
        if(interval != null && step != null && expression != null) {
            graph1.legendRenderer.isVisible = true
            graph2.legendRenderer.isVisible = true
            graph1.legendRenderer.width = 300
            graph2.legendRenderer.width = 300
            graph1.legendRenderer.align = LegendRenderer.LegendAlign.TOP;
            graph2.legendRenderer.align = LegendRenderer.LegendAlign.TOP;
            graph1.viewport.isScalable = true
            graph1.viewport.isScrollable = true
            graph2.viewport.isScalable = true
            graph2.viewport.isScrollable = true
            graph1.viewport.setMinX(interval!!)
            graph2.viewport.setMinX(interval!!)
            graph1.viewport.setMaxX(interval!! + step!!*3)
            graph2.viewport.setMaxX(interval!! + step!!*3)
            try {
                for (i in 0 until 4) {
                    val x = interval!! + step!! * i
                    tabulation.add(Pair(x, expression!!.setVariable("x", x).evaluate()))
                }
                object : AsyncTask<ArrayList<Pair<Double, Double>>, Unit, ArrayList<Pair<String, (x: Double) -> Double>>>() {
                    override fun doInBackground(vararg tabulation: ArrayList<Pair<Double, Double>>?): ArrayList<Pair<String, (x: Double) -> Double>>? {
                        val interpolations = ArrayList<Pair<String, (x: Double) -> Double>>()
                        val interpolationsFunction = ArrayList<Pair<String, ((tabulation: ArrayList<Pair<Double, Double>>) -> ((x: Double) -> Double))>>()
                        interpolationsFunction.add(Pair("Lagrange", ::Lagrange))
                        interpolationsFunction.add(Pair("Newton", ::Newton))
                        interpolationsFunction.add(Pair("LinearSpline", ::createLinearSpline))
                        interpolationsFunction.add(Pair("ParabolaSpline", ::createParabolaSpline))
                        interpolationsFunction.add(Pair("CubicSpline", ::createCubicSpline))
                        interpolations.add(Pair("Function", { x: Double -> expression!!.setVariable("x", x).evaluate() }))
                        for (function in interpolationsFunction)
                            interpolations.add(Pair(function.first, function.second(tabulation[0]!!)))
                        return interpolations
                    }

                    override fun onPreExecute() {
                        super.onPreExecute()
                        progressBar.visibility = View.VISIBLE
                    }

                    override fun onPostExecute(result: ArrayList<Pair<String, (x: Double) -> Double>>?) {
                        super.onPostExecute(result)
                        interpolations = result
                        calculateExprBtn.isClickable = true
                        calculateExprBtn.setOnClickListener(this@InterpolationFragment)
                        progressBar.visibility = View.GONE
                        for (function in interpolations!!) {
                            plotGraph(graph1, function.first, function.second, interval!!, interval!! + step!! * 3)
                        }

                        for (function in interpolations!!) {
                            plotErrorGraph(graph2, function.first, { x: Double -> expression!!.setVariable("x", x).evaluate() },
                                    function.second, interval!!, interval!! + step!! * 3)
                        }
                    }
                }.execute(tabulation)
            } catch (exc : ArithmeticException) {
                Toast.makeText(this.context, exc.message.toString() + "\nTry to change your interval", Toast.LENGTH_LONG).show()
            }
            fullscreenBtn1.setOnClickListener(this)
            fullscreenBtn2.setOnClickListener(this)
        } else {
            Toast.makeText(this.context, "Sorry. Cant create interpolation...", Toast.LENGTH_LONG).show()
        }
        calculateExprBtn.isClickable = false
        backBtn.setOnClickListener(this)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onPause() {
        super.onPause()
        viewModel.arg = argET.text.toString()
        viewModel.interpolationError = interpolationErrorTV.text.toString()
        viewModel.interpolationValue = interpolationValueTV.text.toString()
        viewModel.expression = expression
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.calculateExprBtn -> {
                try {
                    val x = argET.text.toString().toDouble()
                    val answer = expression!!.setVariable("x", x).evaluate()
                    var valueText : StringBuilder = StringBuilder("value\n"
                            + (String.format("%.6f", (x).toString().toDouble()) + "\n"))
                    for (function in this.interpolations!!) {
                        valueText.append(String.format("%.6f", (function.second(x)).toString().toDouble()) + "\n")
                    }
                    interpolationValueTV.text = (valueText)
                    var errorText : StringBuilder = StringBuilder("error\n-\n")
                    for (function in this.interpolations!!) {
                        errorText.append(String.format("%.6f", (function.second(x) - answer).absoluteValue.toString().toDouble()) + "\n")
                    }
                    interpolationErrorTV.text = (errorText)
                } catch (exc : NumberFormatException) {
                    Toast.makeText(this.context, exc.message + "\nTry to change your interval", Toast.LENGTH_SHORT).show()
                } catch (exc : ArithmeticException) {
                    Toast.makeText(this.context, exc.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
            R.id.fullscreenBtn1 -> {
                viewModel.fullscreenGraphType = "MainGraph"
                viewModel.graphs1Series = graphs1Series
                callback!!.onFragmentFullscreenGraphClick()
            }
            R.id.fullscreenBtn2 -> {
                viewModel.fullscreenGraphType = "ErrorGraph"
                viewModel.graphs2Series = graphs2Series
                callback!!.onFragmentFullscreenGraphClick()
            }
            R.id.backBtn -> {
                callback?.onFragmentBackButtonClick()
            }
        }
    }

    interface OnFragmentInteractionListener {
        fun onFragmentFullscreenGraphClick()
        fun onFragmentBackButtonClick()
    }

    fun setOnFragmentInteractionListener(callback : OnFragmentInteractionListener) {
        this.callback = callback
    }

    companion object {
        @JvmStatic
        fun newInstance() = InterpolationFragment()
    }

    fun plotGraph(graph : GraphView, graphName : String, function : (x: Double) -> Double, leftInterval : Double, rightInterval : Double) {
        object : AsyncTask<Any, Unit, Array<DataPoint>>() {
            override fun doInBackground(vararg args: Any?): Array<DataPoint> {
                val expr = args[0] as (x: Double) -> Double
                val leftInterval = args[1] as Double
                val rightInterval = args[2] as Double
                val numberOfPoint = 300
                val plotStep = (rightInterval - leftInterval)/numberOfPoint
                return Array<DataPoint>(numberOfPoint, {
                    DataPoint(leftInterval + it * plotStep, expr(leftInterval + it * plotStep))
                })
            }

            override fun onPostExecute(result: Array<DataPoint>?) {
                super.onPostExecute(result)
                val series  = LineGraphSeries(result)
                series.setAnimated(true)
                series.color = graphColors[graphs1Series.size]
                series.title = graphName
                graphs1Series.add(Pair(graphName, series))
                graph.addSeries(series)
            }
        }.execute(function, leftInterval, rightInterval)
    }

    fun getRandomColor() : Int {

        var RED = (Math.random()*255).toInt() % 128
        var GREEN = (Math.random()*255).toInt() % 128
        var BLUE = (Math.random()*255).toInt() % 128

        when((Math.random()*100).toInt() % 3) {
            0 -> {
                RED = (Math.random()*255).toInt() % 255
            }
            1 -> {
                GREEN = (Math.random()*255).toInt() % 255
            }
            3 -> {
                BLUE = (Math.random()*255).toInt() % 255
            }
        }
        return Color.rgb(RED, GREEN, BLUE)
    }

    fun plotErrorGraph(graph : GraphView, graphName : String, function : (x: Double) -> Double, interpolation : (x: Double) -> Double, leftInterval : Double, rightInterval : Double) {
        object : AsyncTask<Any, Unit, Array<DataPoint>>() {
            override fun doInBackground(vararg args: Any?): Array<DataPoint> {
                val function = args[0] as (x: Double) -> Double
                val interpolation = args[1] as (x: Double) -> Double
                val leftInterval = args[2] as Double
                val rightInterval = args[3] as Double
                val numberOfPoint = 300
                val plotStep = (rightInterval - leftInterval)/numberOfPoint
                return Array<DataPoint>(numberOfPoint, {
                    DataPoint(leftInterval + it * plotStep, (interpolation(leftInterval + it * plotStep)
                            - function(leftInterval + it * plotStep)).absoluteValue)
                })
            }

            override fun onPostExecute(result: Array<DataPoint>?) {
                super.onPostExecute(result)
                val series  = LineGraphSeries(result)
                series.setAnimated(true)
                series.color = graphColors[graphs2Series.size]
                series.title = graphName
                graphs2Series.add(Pair(graphName, series))
                graph.addSeries(series)
            }
        }.execute(interpolation, function, leftInterval, rightInterval)
    }
}
