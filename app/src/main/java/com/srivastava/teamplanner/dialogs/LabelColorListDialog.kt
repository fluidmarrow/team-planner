package com.srivastava.teamplanner.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.srivastava.teamplanner.R
import com.srivastava.teamplanner.adapters.LabelColorPickerAdapter
import kotlinx.android.synthetic.main.dialog_list.view.*

abstract class LabelColorListDialog(context: Context,
                                    private var list: ArrayList<String>,
                                    private var title : String="",
                                    private var mSelectedColor: String=""
): Dialog(context) {
    private var adapter: LabelColorPickerAdapter? = null

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list,null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View){
        view.tvTitle.text = title
        view.rvList.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorPickerAdapter(context,list,mSelectedColor)
        view.rvList.adapter = adapter
        adapter!!.onItemClickListener = object : LabelColorPickerAdapter.OnItemClickListener{
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemsSelected(color)
            }
        }

    }

    protected abstract fun onItemsSelected(color: String)
}