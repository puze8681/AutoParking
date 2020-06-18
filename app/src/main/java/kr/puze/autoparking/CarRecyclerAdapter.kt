package kr.puze.autoparking

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_done.*
import kotlinx.android.synthetic.main.item_car.view.*
import java.text.SimpleDateFormat
import java.util.*

class CarRecyclerAdapter(var items: ArrayList<CarData>, var context: Context) : RecyclerView.Adapter<CarRecyclerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_car, null))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
        holder.itemView.setOnClickListener {
            itemClick?.onItemClick(holder.itemView, position)
        }
        holder.itemView.image_edit.setOnClickListener {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_done)
            dialog.text_type.text = "번호판을 수정하시겠습니까?"
            dialog.edit_dialog_text.setText(items[position].carName.toString())
            dialog.button_dialog_cancel.setOnClickListener {
                dialog.dismiss()
            }
            dialog.button_dialog_check.setOnClickListener {
                MainActivity().editItem(position,  dialog.edit_dialog_text.toString())
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context = itemView.context!!
        fun bind(item: CarData, position: Int) {
            itemView.text_num.text = (position+1).toString()
            itemView.text_title.text = item.carName.toString()
            itemView.text_time.text = getTime(item.time)
            itemView.text_pay.text = "${calculatePay(item.time)}원"
        }

        fun getTime(timeStamp: Long): String{
            return SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).format(timeStamp)
        }

        //30분 500원, 10분 초과시 200원
        fun calculatePay(prevTime: Long): Int {
            var enterMinute = (Calendar.getInstance().timeInMillis - prevTime) / 60000
            if (enterMinute <= 30) {
                return 500
            } else {
                return (500 + ((((enterMinute - 30) / 10) + 1) * 200)).toInt()
            }
        }
    }

    private var itemClick: ItemClick? = null

    interface ItemClick {
        fun onItemClick(view: View?, position: Int)
    }
}