package com.heads.thinking.interpolations

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.heads.thinking.interpolations.ui.main.MainViewModel
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.Series
import kotlinx.android.synthetic.main.fragment_image_viewer.*


class ImageViewerFragment : Fragment(), View.OnClickListener {
    private var graphsSeries : ArrayList <Pair <String, Series<DataPoint>>> = ArrayList()
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var viewModel : MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image_viewer, container, false)
    }

    override fun onResume() {
        //activity!!.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onResume()
    }

    override fun onPause() {
        //activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        super.onPause()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this.activity!!).get(MainViewModel::class.java)
        if(viewModel.interval != null && viewModel.step != null) {
            fullScreenGraphView.viewport.setMinX(viewModel.interval!!)
            fullScreenGraphView.viewport.setMaxX(viewModel.interval!! + viewModel.step!!*3)
        }
        if(viewModel.fullscreenGraphType == "MainGraph" && viewModel.graphs1Series != null) {
            graphsSeries = viewModel.graphs1Series!!
            updateGraph(graphsSeries)
        }
        if(viewModel.fullscreenGraphType == "ErrorGraph" && viewModel.graphs2Series != null) {
            graphsSeries = viewModel.graphs2Series!!
            updateGraph(graphsSeries)
        }
        fullScreenGraphView.legendRenderer.isVisible = true
        fullScreenGraphView.legendRenderer.align = LegendRenderer.LegendAlign.TOP;
        fullScreenGraphView.legendRenderer.width = 300

        fullScreenGraphView.viewport.isYAxisBoundsManual = true
        fullScreenGraphView.viewport.isXAxisBoundsManual = true
        fullScreenGraphView.viewport.isScrollable = true
        fullScreenGraphView.viewport.isScalable = true
        fullScreenGraphView.viewport.setScalableY(true)

        backBtn.setOnClickListener(this)
    }

    fun updateGraph(graphsSeries : ArrayList <Pair <String, Series<DataPoint>>>) {
        for(series in graphsSeries) {
            fullScreenGraphView.addSeries(series.second)
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.backBtn -> {
                listener?.onFragmentBackButtonClick()
            }
        }
    }

    interface OnFragmentInteractionListener {
        fun onFragmentBackButtonClick()
    }

    fun setOnFragmentInteractionListener(listener : OnFragmentInteractionListener) {
        this.listener = listener
    }

    companion object {
        @JvmStatic
        fun newInstance() = ImageViewerFragment()
    }
}
