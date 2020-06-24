package kr.puze.autoparking

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_done.*
import kotlinx.android.synthetic.main.dialog_exit.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    companion object{
        lateinit var recyclerAdapter: CarRecyclerAdapter
        val firebaseDatabase = FirebaseDatabase.getInstance()
        var myRef = firebaseDatabase.reference.child("list")
        val item = ArrayList<CarData>()
        lateinit var context: Context
        private var REQUEST_PERMISSION_CODE = 100
        private lateinit var prefUtil: PrefUtil
        lateinit var textPrice: TextView
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
    }

    fun addItem(text: String){
        Log.d("LOGTAG", "addItem")
        Log.d("LOGTAG", getTime(Calendar.getInstance().timeInMillis))
        item.add(CarData(text, Calendar.getInstance().timeInMillis))
        myRef.setValue(item)
    }

    fun editItem(position: Int, text: String, time: Long){
        item[position].carName = text
        item[position].timeStamp = time
        myRef.setValue(item)
    }

    fun findItem(text: String){
        Log.d("LOGTAG", "findItem")
        loop@ for(i in 0 until item.size){
            if(item[i].carName.equals(text)){
                removeItem(i)
                break@loop
            }
        }
    }

    fun removeItem(position: Int) {
        val enterMinute = (Calendar.getInstance().timeInMillis - item[position].timeStamp) / 60000
        prefUtil.todayPrice += calculatePay(item[position].timeStamp)
        textPrice.text = "${prefUtil.todayPrice} 원"
        Log.d("LOGTAG", "removeItem")
        dialogExit(item[position].carName!!, enterMinute.toInt(), prefUtil.todayPrice)
        item.removeAt(position)
        myRef.setValue(item)
    }

    private fun getTime(timeStamp: Long): String{
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(timeStamp)
    }

    //4분까진 0원, 30분 500원, 10분 초과시 200원
    private fun calculatePay(prevTime: Long): Int {
        val enterMinute = (Calendar.getInstance().timeInMillis - prevTime) / 60000
        return when {
            enterMinute <= 4 -> 0
            enterMinute <= 30 -> 500
            else -> (500 + ((((enterMinute - 30) / 10) + 1) * 200)).toInt()
        }
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
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_done)
        if (type == 0) dialog.text_type.text = "입차하시겠습니까?" else dialog.text_type.text = "출차하시겠습니까?"
        dialog.button_dialog_cancel.setOnClickListener { dialog.dismiss() }
        dialog.button_dialog_check.setOnClickListener {
            if (type == 0) {
                addItem(dialog.edit_dialog_text.text.toString())
            } else {
                findItem(dialog.edit_dialog_text.text.toString())
            }
            dialog.dismiss()
        }
        dialog.show()
    }
    @IgnoreExtraProperties
    data class Data(
        var carName: String = "",
        var timeStamp: Long = 0
    )

    private fun dialogExit(carName: String, time: Int, price: Int){
        val dialog = Dialog(this)
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
}
