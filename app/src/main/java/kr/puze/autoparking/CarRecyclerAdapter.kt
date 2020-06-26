package kr.puze.autoparking

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_delete.*
import kotlinx.android.synthetic.main.dialog_edit.*
import kotlinx.android.synthetic.main.dialog_outstandling.*
import kotlinx.android.synthetic.main.item_car.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CarRecyclerAdapter(var items: ArrayList<CarData>, var context: Context, var activity: Activity) : RecyclerView.Adapter<CarRecyclerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_car, null))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
        holder.itemView.setOnClickListener {
            itemClick?.onItemClick(holder.itemView, position)
        }
        holder.itemView.image_edit.setOnClickListener {
            val dialog = Dialog(activity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_edit)
            dialog.edit_dialog_car_edit.setText(items[position].carName.toString())
            dialog.edit_dialog_time_edit.setText(getTime(items[position].timeStamp))
            dialog.button_dialog_cancel_edit.setOnClickListener {
                dialog.dismiss()
            }
            dialog.button_dialog_check_edit.setOnClickListener {
                val editCar = dialog.edit_dialog_car_edit.text.toString()
                try {
                    val editDate = SimpleDateFormat("yyyy-MM-dd hh:mm").parse(dialog.edit_dialog_time_edit.text.toString()).time
                    MainActivity().editItem(position, editCar, editDate)
                }catch (e: ParseException){
                    Toast.makeText(context,"시간 변경 양식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                dialog.dismiss()
            }
            dialog.show()
        }

        holder.itemView.image_delete.setOnClickListener {
            val dialog = Dialog(activity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_delete)
            dialog.text_car.text = items[position].carName.toString()
            dialog.text_price.text = "${calculatePay(items[position].timeStamp)}원"
            dialog.button_dialog_cancel_delete.setOnClickListener {
                dialog.dismiss()
            }
            dialog.button_dialog_check_delete.setOnClickListener {
                MainActivity().removeItem(position)
                dialog.dismiss()
            }
            dialog.show()
        }

        holder.itemView.image_outstanding.setOnClickListener {
            val dialog = Dialog(activity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_outstandling)
            dialog.text_car_out.text = items[position].carName.toString()
            dialog.text_price_out.text = "${calculatePay(items[position].timeStamp)}원"
            dialog.button_dialog_cancel_out.setOnClickListener {
                dialog.dismiss()
            }
            dialog.button_dialog_check_out.setOnClickListener {
                MainActivity().outItem(position)
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context = itemView.context!!
        fun bind(item: CarData, position: Int) {
            itemView.text_num.text = "${position+1}"
            itemView.text_title.text = item.carName.toString()
            itemView.text_time.text = getTime(item.timeStamp)
            itemView.text_pay.text = "${calculatePay(item.timeStamp)}원"
        }

        private fun getTime(timeStamp: Long): String{
            return SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault()).format(timeStamp)
        }

        //4분까진 0원, 30분 500원, 10분 초과시 200원, 최대 6000
        private fun calculatePay(prevTime: Long): Int {
            val enterMinute = (Calendar.getInstance().timeInMillis - prevTime) / 60000
            var pay = when {
                enterMinute <= 4 -> 0
                enterMinute <= 30 -> 500
                else -> (500 + ((((enterMinute - 30) / 10) + 1) * 200)).toInt()
            }
            return if(pay <= 6000) pay
            else 6000
        }
    }

    private var itemClick: ItemClick? = null

    interface ItemClick {
        fun onItemClick(view: View?, position: Int)
    }

    private fun getTime(timeStamp: Long): String{
        return SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault()).format(timeStamp)
    }

    //4분까진 0원, 30분 500원, 10분 초과시 200원원, 최대 6000원
    private fun calculatePay(prevTime: Long): Int {
        val enterMinute = (Calendar.getInstance().timeInMillis - prevTime) / 60000
        var pay = when {
            enterMinute <= 4 -> 0
            enterMinute <= 30 -> 500
            else -> (500 + ((((enterMinute - 30) / 10) + 1) * 200)).toInt()
        }
        return if(pay <= 6000) pay
        else 6000
    }
}