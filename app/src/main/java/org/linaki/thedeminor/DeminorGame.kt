package org.linaki.thedeminor
import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import android.os.Bundle


@Suppress("DEPRECATION")
class DeminorGame(val context: Context, private val activity: MainActivity) {
    var rows = 8
    var cols = 8
    var totalBombs=20
    val flag="🚩"
    var totalFlags=0
    private lateinit var mineField: Array<IntArray>
    private lateinit var buttons: Array<Array<Button>>


    fun drawField(fieldView: GridLayout) {
        fieldView.columnCount = cols
        fieldView.rowCount = rows

        buttons = Array(rows) { Array(cols) { Button(context) } }

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val button = buttons[i][j]

                // Configure button
                button.text = ""
                button.textSize = 30f
                button.setOnClickListener { openCell(i, j) }
                button.setOnLongClickListener { flagCell(i, j);true}

                // Set layout params for GridLayout
                val params = GridLayout.LayoutParams().apply {
                    width = 200
                    height = 200
                    columnSpec = GridLayout.spec(j, 1f)
                    rowSpec = GridLayout.spec(i, 1f)
                }

                fieldView.addView(button, params)
            }
        }
    }

    fun openCell(row: Int, col: Int) {
        if (row !in 0..<rows) {
            Log.d("DeminorGame", "Invalid row index");
            return
        }
        if (col !in 0..<cols) {
            Log.d("DeminorGame", "Invalid row index");
            return
        }
        val button = buttons[row][col]
        if ((!button.isEnabled) || button.text==flag) { //Cell was already opened/flagged
            return
        }
        button.isEnabled = false
        Log.d("DeminorGame", mineField[row][col].toString())
        val cellType=mineField[row][col]
        if (cellType==-1) { //Cell is a bomb
            Log.d("DeminorGame", "Bomb touched : Game over")
            button.text="🌻"
            showGameOverDialog()

        }
        else if (cellType>0) {
            button.text=cellType.toString()
            changeCellColor(button, cellType);
        }
        if (cellType==0) { //Cell is empty, open all adjacent
            revealAdjacent(row, col)
        }

        //button.text = "Hihi ($row,$col) !!"

    }

    fun flagCell(row: Int, col: Int) {
        val button = buttons[row][col]
        if (button.text==flag) {
            button.text=""
            totalFlags--
        }
        else {
            button.text = flag
            totalFlags++
        }
        updateTopText()
        vibrateDevice()
        checkWin()
    }

    fun showGameOverDialog() {
        AlertDialog.Builder(context)
            .setTitle("Game Over!")
            .setMessage(context.getString(R.string.lost))
            .setPositiveButton(context.getString(R.string.restart_game)) { _, _ ->
                activity.startGame()
            }
            .setNegativeButton(context.getString(R.string.back_to_menu)) { _, _ ->
                activity.openMenu()
            }
            .setCancelable(true) // User must choose an option
            .show()
    }
    fun showGameWonDialog() {
        AlertDialog.Builder(context)
            .setTitle("YOUPII !!")
            .setMessage(context.getString(R.string.won))
            .setPositiveButton(context.getString(R.string.restart_game)) { _, _ ->
                activity.startGame()
            }
            .setNegativeButton(context.getString(R.string.back_to_menu)) { _, _ ->
                activity.openMenu()
            }
                /*.setNeutralButton("Partager mon exploit") {_, _ ->
                    val intent= Intent()
                    intent.action=Intent.ACTION_SEND
                    intent.putExtra(Intent.EXTRA_TEXT,"Hey Check out this Great app:")
                    intent.type="text/plain"
                    //startActivity(Intent.createChooser(intent,"Share To:") as Context)
                }*/

            .setCancelable(true) // User must choose an option
            .show()
    }

    private fun revealAdjacent(row: Int, col: Int) {
        Log.d("DeminorGame", "Calling RevealAdjacent"+row.toString()+","+col.toString())
        for (r in (row - 1)..(row + 1)) {
            for (c in (col - 1)..(col + 1)) {
                if (r in 0 until rows && c in 0 until cols) {
                    Log.d("DeminorGame", "Opening adjacent cell")
                    openCell(r, c)
                }
            }
        }
    }

    fun generateField(){
        // Validate inputs
        if (totalBombs > rows * cols) {
            throw IllegalArgumentException("Number of bombs cannot exceed grid size")
        }
        if (totalBombs < 0) {
            throw IllegalArgumentException("Number of bombs must be positive")
        }

        mineField = Array(rows) { IntArray(cols) { 0 } }
        val totalCells = rows * cols
        val bombPositions = mutableSetOf<Int>()

        // Generate random bomb positions (as flat indices)
        while (bombPositions.size < totalBombs) {
            bombPositions.add((0 until totalCells).random())
        }

        // Place bombs in the field (-1 represents a bomb)
        for (pos in bombPositions) {
            val row = pos / cols
            val col = pos % cols
            mineField[row][col] = -1
        }

        // Calculate adjacent bomb counts
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (mineField[row][col] != -1) { // Only for non-bomb cells
                    var count = 0
                    // Check all 8 adjacent cells
                    for (r in (row - 1)..(row + 1)) {
                        for (c in (col - 1)..(col + 1)) {
                            if (r in 0 until rows && c in 0 until cols && mineField[r][c] == -1) {
                                count++
                            }
                        }
                    }
                    mineField[row][col] = count
                }
            }
        }
        Log.d("DeminorGame", mineField.contentDeepToString())

    }

    fun findHarmlessCell(): Pair<Int, Int>? {
        val safeCells = mutableListOf<Pair<Int, Int>>()

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                // Find cells with 0 adjacent mines (completely safe)
                if (mineField[row][col] == 0) {
                    safeCells.add(Pair(row, col))
                }
            }
        }

        return safeCells.randomOrNull()
    }

    fun openHarmlessCell() {
        val cell = findHarmlessCell()
        if (cell != null) {
            openCell(cell.first, cell.second)
        }
    }

    fun updateTopText() {
        activity.top_text.text= "$totalFlags drapeaux/$totalBombs bombes"
    }

    fun checkWin() {
        if (totalFlags!=totalBombs) { //If there is not the same number of flags and bombs
            return
        }
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (mineField[row][col] == -1) {
                    if (buttons[row][col].text!=flag) { //Bomb is not flagged
                        return
                    }
                }
                else {
                    if (buttons[row][col].text == flag) { //Harmless cell is flagged
                        return
                    }
                }
            }
        }
        showGameWonDialog()
    }

    fun vibrateDevice() {
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun changeCellColor(cell: Button, cellType: Int) {
        cell.setTextColor(context.getColor(when (cellType) {
            1 -> R.color.cell_1_bg
            2 -> R.color.cell_2_bg
            3 -> R.color.cell_3_bg
            4 -> R.color.cell_4_bg
            else -> R.color.cell_5_bg }))

    }
}