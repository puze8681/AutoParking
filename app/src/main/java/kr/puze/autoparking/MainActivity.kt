package kr.puze.autoparking

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_done.*
import kotlinx.android.synthetic.main.dialog_exit.*
import kotlinx.android.synthetic.main.dialog_exit.button_dialog_check_exit
import kotlinx.android.synthetic.main.dialog_out.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    companion object{
        lateinit var dialogContext: Context
        lateinit var recyclerAdapter: CarRecyclerAdapter
        val firebaseDatabase = FirebaseDatabase.getInstance()
        var myRef = firebaseDatabase.reference.child("list")
        var outRef = firebaseDatabase.reference.child("out")
        val item = ArrayList<CarData>()
        val itemOut = ArrayList<OutData>()
        lateinit var context: Context
        private var REQUEST_PERMISSION_CODE = 100
        private lateinit var prefUtil: PrefUtil
        lateinit var textPrice: TextView
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dialogContext = this
        context = applicationContext
        prefUtil = PrefUtil(applicationContext, getTime(Calendar.getInstance().timeInMillis))
        recyclerAdapter = CarRecyclerAdapter(item, context, this)
        recycler_main.adapter = recyclerAdapter
        recyclerAdapter.notifyDataSetChanged()
        textPrice = findViewById(R.id.text_total)
        textPrice.text = "${prefUtil.todayPrice} 원"

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("LOGTAG", "myRef OnDataChange")
                item.clear()
                dataSnapshot.children.forEach{
                    it.getValue(Data::class.java)?.let { data ->
                        Log.d("LOGTAG", "myRef $data")
                        item.add(CarData(data.carName, data.timeStamp))
                    }
                }
                textPrice.text = "${prefUtil.todayPrice} 원"
                recyclerAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        outRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("LOGTAG", "outRef OnDataChange")
                itemOut.clear()
                dataSnapshot.children.forEach{
                    it.getValue(OutData::class.java)?.let { data ->
                        Log.d("LOGTAG", "outRef $data")
                        itemOut.add(OutData(data.carName, data.time, data.price))
                    }
                }
                textPrice.text = "${prefUtil.todayPrice} 원"
                recyclerAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        button_total.setOnClickListener {
            textPrice.text = "${prefUtil.todayPrice} 원"
            recyclerAdapter.notifyDataSetChanged()
        }

        text_enter.setOnClickListener {
            dialogDone(0)
//            checkPermission(0)
        }

        text_exit.setOnClickListener {
            dialogDone(1)
//            checkPermission(1)
        }

        button_out.setOnClickListener {
            startActivity(Intent(this@MainActivity, OutActivity::class.java))
        }
    }

    fun findItem(text: String, type: Int){
        Log.d("LOGTAG", "findItem")
        var overlap = false
        loop@ for(i in 0 until item.size){
            if(item[i].carName.equals(text)){
                overlap = true
                if(type == 1) removeItem(i)
                break@loop
            }
        }
        if(type == 0 && !overlap) addItem(text)
        else if(type == 0 && overlap) Toast.makeText(this@MainActivity,"차량번호 중복입니다.", Toast.LENGTH_SHORT).show()

    }

    fun addItem(text: String){
        Log.d("LOGTAG", "addItem")
        Log.d("LOGTAG", getTime(Calendar.getInstance().timeInMillis))
        item.add(CarData(text, Calendar.getInstance().timeInMillis))
        myRef.setValue(item)
        findOutItem(text)
    }

    fun findOutItem(text: String){
        var overlap = false
        var overlapIndex = 0
        loop@ for(i in 0 until itemOut.size){
            if(itemOut[i].carName.equals(text)){
                overlap = true
                overlapIndex = i
                break@loop
            }
        }
        if(overlap) dialogOut(text, itemOut[overlapIndex].time, itemOut[overlapIndex].price)
        else Toast.makeText(dialogContext,"입차완료.", Toast.LENGTH_SHORT).show()

    }

    fun editItem(position: Int, text: String, time: Long){
        item[position].carName = text
        item[position].timeStamp = time
        myRef.setValue(item)
        Toast.makeText(dialogContext,"수정완료.", Toast.LENGTH_SHORT).show()
    }

    fun removeItem(position: Int) {
        val enterMinute = (Calendar.getInstance().timeInMillis - item[position].timeStamp) / 60000
        prefUtil.todayPrice += calculatePay(item[position].timeStamp)
        textPrice.text = "${prefUtil.todayPrice} 원"
        Log.d("LOGTAG", "removeItem")
        dialogExit(item[position].carName!!, enterMinute.toInt(), calculatePay(item[position].timeStamp))
        item.removeAt(position)
        myRef.setValue(item)
    }

    fun outItem(position: Int){
        itemOut.add(OutData(item[position].carName!!, getTime(item[position].timeStamp),calculatePay(item[position].timeStamp)))
        outRef.setValue(itemOut)
        item.removeAt(position)
        myRef.setValue(item)
        Toast.makeText(dialogContext,"미결제 차량 등록 완료.", Toast.LENGTH_SHORT).show()
    }

    private fun getTime(timeStamp: Long): String{
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(timeStamp)
    }

    //4분까진 0원, 30분 500원, 10분 초과시 200원, 최대 6000원
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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission(type: Int) {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(this@MainActivity,"업로드할 사진을 갤러리에서 불러오기 위해 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
            }
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSION_CODE)
        } else {
            startActivity(Intent(this@MainActivity, OpenActivity::class.java).putExtra("type", type))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this@MainActivity,"기능 사용을 위한 권한 동의가 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dialogDone(type: Int) {
        val dialog = Dialog(dialogContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_done)
        dialog.edit_dialog_text.onFocusChangeListener =
            View.OnFocusChangeListener { v, _ ->
                dialog.edit_dialog_text.post(Runnable {
                    val inputMethodManager: InputMethodManager = this@MainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.showSoftInput(dialog.edit_dialog_text, InputMethodManager.SHOW_IMPLICIT)
                })
            }
        dialog.edit_dialog_text.requestFocus()
        if (type == 0) dialog.text_type.text = "입차하시겠습니까?" else dialog.text_type.text = "출차하시겠습니까?"
        dialog.button_dialog_cancel.setOnClickListener { dialog.dismiss() }
        dialog.button_dialog_check.setOnClickListener {
            if (type == 0) {
                findItem(dialog.edit_dialog_text.text.toString(), type)
            } else {
                findItem(dialog.edit_dialog_text.text.toString(), type)
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun dialogExit(carName: String, time: Int, price: Int){
        val dialog = Dialog(dialogContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_exit)
        dialog.text_car_exit.text = carName
        dialog.text_time_exit.text = "$time 분"
        dialog.text_price_exit.text = "$price 원"
        dialog.button_dialog_check_exit.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun dialogOut(carName: String, time: String, price: Int){
        val dialog = Dialog(dialogContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_out)
        dialog.text_car_out.text = carName
        dialog.text_time_out.text = time
        dialog.text_price_out.text = "$price 원"
        dialog.button_dialog_check_exit.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    @IgnoreExtraProperties
    data class Data(
        var carName: String = "",
        var timeStamp: Long = 0
    )

    @IgnoreExtraProperties
    data class OutData(
        var carName: String = "",
        var time: String = "",
        var price: Int = 0
    )
}
