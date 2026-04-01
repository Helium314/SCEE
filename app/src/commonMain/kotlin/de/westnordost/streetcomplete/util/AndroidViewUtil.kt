package de.westnordost.streetcomplete.util

import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.util.ktx.dpToPx

// using setView fills the entire AlertDialog, while setMessage or set*Items add some padding
// this adds same/similar padding to setView
fun AlertDialog.Builder.setViewWithDefaultPadding(v: View): AlertDialog.Builder {
    v.setDefaultDialogPadding()
    return setView(v)
}

fun View.setDefaultDialogPadding() {
    val padding = getDimensionFromAttribute(context, android.R.attr.dialogPreferredPadding)
    // no source for /3, but it looks ok
    setPadding(padding, padding / 3, padding, padding / 3)
}

private fun getDimensionFromAttribute(context: Context, attr: Int): Int {
    val typedValue = TypedValue()
    return if (context.theme.resolveAttribute(attr, typedValue, true))
        TypedValue.complexToDimensionPixelSize(typedValue.data, context.resources.displayMetrics)
    else 0
}

class ArrayImageAdapter(context: Context, private val items: List<Int>, imageSizeDp: Int) :
    ArrayAdapter<Int>(context, android.R.layout.select_dialog_item, items)
{
    private val params = ViewGroup.LayoutParams(context.resources.dpToPx(imageSizeDp).toInt(), context.resources.dpToPx(imageSizeDp).toInt())
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View { // for non-dropdown
        val view = super.getView(position, convertView, parent)
        val tv = view.findViewById<TextView>(android.R.id.text1)
        tv.text = ""
        tv.background = context.getDrawable(items[position])
        tv.layoutParams = params
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = (convertView as? ImageView) ?: ImageView(context)
        v.setImageResource(items[position])
        v.layoutParams = params
        return v
    }
}
