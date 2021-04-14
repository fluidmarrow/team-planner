package com.srivastava.teamplanner.adapters

 import android.content.Context
 import android.view.LayoutInflater
 import android.view.View
 import android.view.ViewGroup
 import androidx.recyclerview.widget.RecyclerView
 import com.bumptech.glide.Glide
 import com.srivastava.teamplanner.R
 import com.srivastava.teamplanner.models.Board
 import kotlinx.android.synthetic.main.item_board.view.*

open class BoardItemsAdapter(private val context: Context,
                             private val list:ArrayList<Board>):
                             RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
                LayoutInflater.from(context).inflate(
                        R.layout.item_board,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            model.image.let {
                Glide
                        .with(context)
                        .load(it)
                        .centerCrop()
                        .placeholder(R.drawable.ic_board_place_holder)
                        .into(holder.itemView.boardImageView)
            }

            holder.itemView.tv_board_name.text = model.name
            holder.itemView.tv_created_by.text = model.createdBy

            holder.itemView.setOnClickListener{
                if(onClickListener != null){
                    onClickListener!!.onClick(position,model)
                }
            }
        }
    }

    interface OnClickListener{
        fun onClick(position: Int, model: Board)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}