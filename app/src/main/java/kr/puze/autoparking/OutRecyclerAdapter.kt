package kr.puze.autoparking

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_delete.*
import kotlinx.android.synthetic.main.item_car_out.view.*
import java.util.*

class OutRecyclerAdapter(var items: ArrayList<OutData>, var context: Context, var activity: Activity) : RecyclerView.Adapter<OutRecyclerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_car_out, null))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
        holder.itemView.setOnClickListener {
            itemClick?.onItemClick(holder.itemView, position)
        }

        holder.itemView.image_outstanding_out.setOnClickListener {
            val dialog = Dialog(activity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_delete)
            dialog.text_car.text = items[position].carName.toString()
            dialog.text_price.text = "${items[position].price} 원"
            dialog.button_dialog_cancel_delete.setOnClickListener {
                dialog.dismiss()
            }
            dialog.button_dialog_check_delete.setOnClickListener {
                OutActivity().deleteItem(position)
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context = itemView.context!!
        fun bind(item: OutData, position: Int) {
            itemView.text_num_out.text = "${position+1}"
            itemView.text_title_out.text = item.carName.toString()
            itemView.text_time_out.text = item.time
            itemView.text_pay_out.text = "${item.price}원"
        }
    }

    private var itemClick: ItemClick? = null

    interface ItemClick {
        fun onItemClick(view: View?, position: Int)
    }
}