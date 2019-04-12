package com.heads.thinking.interpolations.custom.math.methods.interpolation

import kotlin.math.absoluteValue
import kotlin.math.sin


fun function(x : Double) : Double = Math.PI * sin(8*x) /x + x*x

val tabulation : ArrayList<Pair<Double, Double>> = ArrayList()

fun Lagrange(tabulation : ArrayList<Pair<Double, Double>>) : ((x : Double) -> Double) {
    return { x: Double ->
        var result = 0.0
        for (i in 0 until tabulation.size) {
            var chislitel = 1.0
            var znamenatel = 1.0
            for (j in 0 until tabulation.size) {
                if (i != j) {
                    chislitel *= x - tabulation[j].first
                    znamenatel *= tabulation[i].first - tabulation[j].first
                }
            }
            result += tabulation[i].second * chislitel / znamenatel
        }
        result
    }
}

fun createTable(tabulation : ArrayList<Pair<Double, Double>>) : Array<Array<Double>> {
    val size = tabulation.size
    val table = Array<Array<Double>>(size + 1) { i : Int ->
        if(i == 0 || i == 1)
            Array(size , {0.0})
        else
            Array(size - i + 1, {0.0})
    }
    for(j in 0 until size) {
        table[0][j] = tabulation[j].first
        table[1][j] = tabulation[j].second
    }
    for(i in 2 until size + 1)
        for(j in 0 until size - i + 1)
            table[i][j] = (table[i-1][j + 1] - table[i-1][j]) / (table[0][j + i - 1] - table[0][j])

    return table
}

fun Newton(tabulation : ArrayList<Pair<Double, Double>>) : ((x : Double) -> Double) {
    val table = createTable(tabulation)
    return { x : Double ->
        var result = 0.0
        for(i in 1 until table.size) {
            var delta = 1.0
            for(j in 0 until (i - 1)) {
                delta *= x - table[0][j]
            }
            result += table[i][0] * delta
        }
        result
    }
}

fun createLinearSpline(tabulation: ArrayList<Pair<Double, Double>>) : ((x : Double) -> Double) = { x : Double ->
    var interval : IntRange? = null
    for(i in 1 until tabulation.size) {
        if(x >= tabulation[i - 1].first && x <= tabulation[i].first) {
            if(i == (tabulation.size - 1)) interval = (tabulation.size - 2)..(tabulation.size - 1)
            else if(i == 0) interval = null
            else interval = (i - 1)..i
            break
        }
    }
    if(interval == null) throw ArithmeticException("Argument out of range")
    val first = interval.first
    val last = interval.last

    -((tabulation[first].second - tabulation[last].second) * x +
            (tabulation[first].first * tabulation[last].second
                    - tabulation[last].first * tabulation[first].second)) / (tabulation[last].first - tabulation[first].first)
}


fun getMatrixForParabolaSpline(h : Double, y : Array<Double>) : Array<Array<Double>> {
    val h2 = Math.pow(h, 2.0)
    return arrayOf(
            doubleArrayOf(1.0,  0.0,     0.0,    0.0,    0.0,    0.0,    0.0,    0.0,   0.0,    y[0]).toTypedArray(),
            doubleArrayOf(1.0,  h,       h2,     0.0,    0.0,    0.0,    0.0,    0.0,   0.0,    y[1]).toTypedArray(),
            doubleArrayOf(0.0,  0.0,     0.0,    1.0,    0.0,    0.0,    0.0,    0.0,   0.0,    y[1]).toTypedArray(),
            doubleArrayOf(0.0,  0.0,     0.0,    1.0,    h,      h2,     0.0,    0.0,   0.0,    y[2]).toTypedArray(),
            doubleArrayOf(0.0,  0.0,     0.0,    0.0,    0.0,    0.0,    1.0,    0.0,   0.0,    y[2]).toTypedArray(),
            doubleArrayOf(0.0,  0.0,     0.0,    0.0,    0.0,    0.0,    1.0,    h,     h2,     y[3]).toTypedArray(),
            doubleArrayOf(0.0,  1.0,     2 * h,  0.0,    -1.0,   0.0,    0.0,    0.0,   0.0,     0.0).toTypedArray(),
            doubleArrayOf(0.0,  0.0,     0.0,    0.0,    1.0,    2 * h,  0.0,    -1.0,  0.0,     0.0).toTypedArray(),
            doubleArrayOf(0.0,  0.0,     1.0,    0.0,    0.0,    0.0,    0.0,    0.0,   0.0,     0.0).toTypedArray()
    )
}


fun getMatrixForCubicSpline(h : Double, y : Array<Double>) : Array<Array<Double>> {
    val h2 = Math.pow(h, 2.0)
    val h3 = Math.pow(h, 3.0)
    return arrayOf(
            doubleArrayOf(1.0,  0.0,  0.0, 0.0,     0.0, 0.0,   0.0,    0.0,    0.0, 0.0,   0.0,    0.0,    y[0]).toTypedArray(),
            doubleArrayOf(1.0,  h,    h2,  h3,      0.0, 0.0,   0.0,    0.0,    0.0, 0.0,   0.0,    0.0,    y[1]).toTypedArray(),
            doubleArrayOf(0.0,  0.0,  0.0, 0.0,     1.0, 0.0,   0.0,    0.0,    0.0, 0.0,   0.0,    0.0,    y[1]).toTypedArray(),
            doubleArrayOf(0.0,  0.0,  0.0, 0.0,     1.0, h,     h2,     h3,     0.0, 0.0,   0.0,    0.0,    y[2]).toTypedArray(),
            doubleArrayOf(0.0,  0.0,  0.0, 0.0,     0.0, 0.0,   0.0,    0.0,    1.0, 0.0,   0.0,    0.0,    y[2]).toTypedArray(),
            doubleArrayOf(0.0,  0.0,  0.0, 0.0,     0.0, 0.0,   0.0,    0.0,    1.0, h,     h2,     h3,     y[3]).toTypedArray(),
            doubleArrayOf(0.0,  1.0,  2*h, 3 * h2,  0.0, -1.0,   0.0,   0.0,    0.0, 0.0,   0.0,    0.0,    0.0 ).toTypedArray(),
            doubleArrayOf(0.0,  0.0,  1.0, 3 * h,   0.0, 0.0,   -1.0,   0.0,    0.0, 0.0,   0.0,    0.0,    0.0 ).toTypedArray(),
            doubleArrayOf(0.0,  0.0,  0.0, 0.0,     0.0, 1.0,   2 * h,  3 * h2, 0.0, -1.0,  0.0,    0.0,    0.0 ).toTypedArray(),
            doubleArrayOf(0.0,  0.0,  0.0, 0.0,     0.0, 0.0,   1.0,    3 * h,  0.0, 0.0,   -1.0,   0.0,    0.0 ).toTypedArray(),
            doubleArrayOf(0.0,  0.0,  0.0, 0.0,     0.0, 0.0,   0.0,    0.0,    0.0, 0.0,   1.0,    3 * h,  0.0 ).toTypedArray(),
            doubleArrayOf(0.0,  0.0,  1.0, 0.0,     0.0, 0.0,   0.0,    0.0,    0.0, 0.0,   0.0,    0.0,    0.0 ).toTypedArray()
    )
}

fun createParabolaSpline(tabulation : ArrayList<Pair<Double, Double>>) : ((x : Double) -> Double) {
    val y = Array<Double>(tabulation.size, {
        tabulation[it].second
    })
    val h = tabulation[1].first - tabulation[0].first
    val matrix = getMatrixForParabolaSpline(h, y)
    val coefficient = gauss(matrix) ?: throw ArithmeticException("Can`t find coefficient for spline")
    return { arg : Double ->
        var currentPolynom : Int? = null
        for (i in 0 until tabulation.size) {
            if(arg < tabulation[i].first) {
                currentPolynom = i - 1
                break
            }
        }
        if(currentPolynom == null || currentPolynom < 0) throw ArithmeticException("Argument out of range")
        val newX = arg - tabulation[currentPolynom].first
        Math.pow(newX, 2.0) * coefficient[currentPolynom*3 + 2] + newX * coefficient[currentPolynom * 3 + 1] + coefficient[currentPolynom * 3]
    }
}

fun createCubicSpline(tabulation : ArrayList<Pair<Double, Double>>) : ((x : Double) -> Double) {
    val y = Array<Double>(tabulation.size, {
        tabulation[it].second
    })
    val h = tabulation[1].first - tabulation[0].first
    val matrix = getMatrixForCubicSpline(h, y)
    val coefficient = gauss(matrix) ?: throw ArithmeticException("Can`t find coefficient for spline")
    return { arg : Double ->
        var currentPolynom : Int? = null
        for (i in 0 until tabulation.size) {
            if(arg < tabulation[i].first) {
                currentPolynom = i - 1
                break
            }
        }
        if(currentPolynom == null || currentPolynom < 0) throw ArithmeticException("Argument out of range")
        val newX = arg - tabulation[currentPolynom].first
        Math.pow(newX, 3.0) * coefficient[currentPolynom*4 + 3] + Math.pow(newX, 2.0) * coefficient[currentPolynom * 4 + 2] + newX * coefficient[currentPolynom * 4 + 1] + coefficient[currentPolynom * 4]
    }
}

fun gauss(matrix : Array<Array<Double>>) : ArrayList<Double>? {
    for(i in 0 until (matrix.size)) {
        var columnsMax = 0.0
        var indexColumnsMax = 0
        for(j in i until matrix.size) {
            if(matrix[j][i].absoluteValue > columnsMax) {
                columnsMax = matrix[j][i]
                indexColumnsMax = j
            }
        }
        if(columnsMax == 0.0) return null
        if(i != indexColumnsMax) {
            for(j in i until matrix[0].size) {
                //swap
                matrix[i][j] = matrix[i][j] + matrix[indexColumnsMax][j]
                matrix[indexColumnsMax][j] = matrix[i][j] - matrix[indexColumnsMax][j]
                matrix[i][j] = matrix[i][j] - matrix[indexColumnsMax][j]
            }
        }
        for(j in i + 1 until matrix.size) {
            val coeff = matrix[j][i] / columnsMax
            matrix[j][i] = 0.0
            for(k in i + 1 until matrix[0].size)
                matrix[j][k] = matrix[j][k] - matrix[i][k] * coeff
        }
    }
    val ans = ArrayList<Double>(matrix.size)
    for(i in 0 until matrix.size) ans.add(0.0)
    for(i in (matrix.size - 1) downTo 0) {
        if(matrix[i][i] != 0.0) {
            ans[i] = matrix[i][matrix[0].size - 1]
            for(j in (matrix.size - 1) downTo (i + 1))
                ans[i] -= ans[j] * matrix[i][j]
            ans[i] /= matrix[i][i]
        } else ans[i] = 0.0

    }
    return ans
}
/*
fun gauss(matrix : ArrayList<ArrayList<Double>>) : ArrayList<Double>? {
    for(i in 0 until (matrix.size)) {
        var columnsMax = 0.0
        var indexColumnsMax = 0
        for(j in i until matrix.size) {
            if(matrix[j][i].absoluteValue > columnsMax) {
                columnsMax = matrix[j][i]
                indexColumnsMax = j
            }
        }
        if(columnsMax == 0.0) return null
        if(i != indexColumnsMax) {
            for(j in i until matrix[0].size) {
                //swap
                matrix[i][j] = matrix[i][j] + matrix[indexColumnsMax][j]
                matrix[indexColumnsMax][j] = matrix[i][j] - matrix[indexColumnsMax][j]
                matrix[i][j] = matrix[i][j] - matrix[indexColumnsMax][j]
            }
        }
        for(j in i + 1 until matrix.size) {
            val coeff = matrix[j][i] / columnsMax
            matrix[j][i] = 0.0
            for(k in i + 1 until matrix[0].size)
                matrix[j][k] = matrix[j][k] - matrix[i][k] * coeff
        }
    }
    val ans = ArrayList<Double>(matrix.size)
    for(i in 0 until matrix.size) ans.add(0.0)
    for(i in (matrix.size - 1) downTo 0) {
        if(matrix[i][i] != 0.0) {
            ans[i] = matrix[i][matrix[0].size - 1]
            for(j in (matrix.size - 1) downTo (i + 1))
                ans[i] -= ans[j] * matrix[i][j]
            ans[i] /= matrix[i][i]
        } else ans[i] = 0.0

    }
    return ans
}



fun qubicSpline(tabulation: ArrayList<Pair<Double, Double>>)
    : ((x : Double) -> Double?) {
val countOfPoly = tabulation.size - 1
val matrix = ArrayList<ArrayList<Double>>()
for(i in 0 until countOfPoly * 4) {
    matrix.add(ArrayList())
    for(j in 0 until 4 * countOfPoly + 1)
        matrix[i].add(0.0)
}
// заполняем матрицу слау
// равенство функции и полиномов в узлах = 2n - 2 уравнений (по 2 для каждого полинома)
// Pi(Xi) = f(Xi)
// Pi(Xi) = f(Xi+1)
var pointIndex = 0
var currentPoly = 0
for(i in 0 until countOfPoly * 2) {
    val x = tabulation[pointIndex].first
    val y = tabulation[pointIndex].second
    matrix[i][currentPoly*4] = Math.pow(x, 3.0)
    matrix[i][currentPoly*4 + 1] = Math.pow(x, 2.0)
    matrix[i][currentPoly*4 + 2] = x
    matrix[i][currentPoly*4 + 3] = 1.0
    matrix[i][matrix[currentPoly].size - 1] = y
    if((i % 2) == 0) pointIndex++
    if(i % 2 == 1) currentPoly++
}
// равенство производных полиномов в соседних узлах
// Pi`(Xi+1) = Pi+1`(Xi+1)
pointIndex = 1
for(i in countOfPoly*2 until countOfPoly*3 - 1) {
    val x = tabulation[pointIndex].first
    val polyIndex = i - countOfPoly*2
    matrix[i][polyIndex * 4] = 3.0 * Math.pow(x, 2.0)
    matrix[i][polyIndex * 4 + 1] = 2.0 * x
    matrix[i][polyIndex * 4 + 2] = 1.0
    matrix[i][(polyIndex + 1) * 4] = -3.0 * Math.pow(x, 2.0)
    matrix[i][(polyIndex + 1) * 4 + 1] = -2.0 * x
    matrix[i][(polyIndex + 1) * 4 + 2] = -1.0
    pointIndex++
}
pointIndex = 1
for(i in countOfPoly*3 - 1 until countOfPoly*4 - 2) {
    val x = tabulation[pointIndex].first
    val polyIndex = i - (countOfPoly*3 - 1)
    matrix[i][polyIndex * 4] = 6.0 * x
    matrix[i][polyIndex * 4 + 1] = 2.0
    matrix[i][(polyIndex + 1) * 4] = -6.0 * x
    matrix[i][(polyIndex + 1) * 4 + 1] = -2.0
    pointIndex++
}
// вторая производная на границе равна 0
matrix[countOfPoly*4 - 1][0] = 6.0 * tabulation[0].first
matrix[countOfPoly*4 - 1][1] = 2.0
matrix[countOfPoly*4 - 2][matrix[countOfPoly*4 - 2].size - 4] = 6.0 * tabulation[tabulation.size - 1].first
matrix[countOfPoly*4 - 2][matrix[countOfPoly*4 - 2].size - 3] = 2.0
val coefficient = gauss(matrix) ?: throw ArithmeticException("Не удалось подсчитать коэффициенты");

return { x : Double ->
    var currentPolynom : Int? = null
    for (i in 0 until tabulation.size - 1) {
        if(x > tabulation[i].first) {
            currentPolynom = i
            break
        }
    }
    if(currentPolynom == null) throw ArithmeticException("Аргумент вне границ сплайна")
    Math.pow(x, 3.0) * coefficient[currentPolynom*4] + Math.pow(x, 2.0) * coefficient[currentPolynom * 4 + 1]
    + x * coefficient[currentPolynom * 4 + 2] + coefficient[currentPolynom * 4 + 3]
}
}



fun quadraticSpline(tabulation: ArrayList<Pair<Double, Double>>)
: ((x : Double) -> Double?) {
val countOfPoly = tabulation.size - 1
val matrix = ArrayList<ArrayList<Double>>()
for(i in 0 until countOfPoly * 3) {
    matrix.add(ArrayList())
    for(j in 0 until 3 * countOfPoly + 1)
        matrix[i].add(0.0)
}
// заполняем матрицу слау
// равенство функции и полиномов в узлах = 2n - 2 уравнений (по 2 для каждого полинома)
// Pi(Xi) = f(Xi)
// Pi(Xi) = f(Xi+1)
var pointIndex = 0
var currentPoly = 0
for(i in 0 until countOfPoly * 2) {
    val x = tabulation[pointIndex].first
    val y = tabulation[pointIndex].second
    matrix[i][currentPoly*3] = Math.pow(x, 2.0)
    matrix[i][currentPoly*3 + 1] = x
    matrix[i][currentPoly*3 + 2] = 1.0
    matrix[i][matrix[currentPoly].size - 1] = y
    if((i % 2) == 0) pointIndex++
    if(i % 2 == 1) currentPoly++
}
// равенство производных полиномов в соседних узлах
// Pi`(Xi+1) = Pi+1`(Xi+1)
pointIndex = 1
for(i in countOfPoly*2 until countOfPoly*3 - 1) {
    val x = tabulation[pointIndex].first
    val polyIndex = i - countOfPoly*2
    matrix[i][polyIndex * 3] = 2.0 * x
    matrix[i][polyIndex * 3 + 1] = 1.0
    matrix[i][(polyIndex + 1) * 3] = -2.0 * x
    matrix[i][(polyIndex + 1) * 3 + 1] = -1.0
    pointIndex++
}
// вторая производная на границе равна 0
matrix[countOfPoly*3 - 1][0] = 2.0
val coefficient = gauss(matrix) ?: throw ArithmeticException("Не удалось подсчитать коэффициенты");

return { x : Double ->
    var currentPolynom : Int? = null
    for (i in 0 until tabulation.size - 1) {
        if(x > tabulation[i].first) {
            currentPolynom = i
            break
        }
    }
    if(currentPolynom == null) throw ArithmeticException("Аргумент вне границ сплайна")
    Math.pow(x, 2.0) * coefficient[currentPolynom*3] + x * coefficient[currentPolynom * 3 + 1] + coefficient[currentPolynom * 3 + 2]
}
}*/