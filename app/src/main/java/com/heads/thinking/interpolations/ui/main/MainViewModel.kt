package com.heads.thinking.interpolations.ui.main

import android.arch.lifecycle.ViewModel
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.Series
import net.objecthunter.exp4j.Expression

class MainViewModel : ViewModel() {

    var fullscreenGraphType : String? = null
    var graphs1Series : ArrayList <Pair <String, Series<DataPoint>>>? = null
    var graphs2Series : ArrayList <Pair <String, Series<DataPoint>>>? = null

    var interpolationValue : String? = null
    var interpolationError : String? = null
    var arg : String? = null


    var step : Double? = null
    var interval : Double? = null
    var function : String? = null
    var expression : Expression? = null

    var currentFragmentName : String? = null

    override fun onCleared() {
        super.onCleared()
    }
}
