package kr.puze.autoparking

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
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
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this@MainActivity
        prefUtil = PrefUtil(this@MainActivity, getTime(Calendar.getInstance().timeInMillis))
        recyclerAdapter = CarRecyclerAdapter(item, this@MainActivity)
        recycler_main.adapter = recyclerAdapter
        recyclerAdapter.notifyDataSetChanged()

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
                recyclerAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        button_total.setOnClickListener {
            text_total.text = "${prefUtil.todayPrice} 원"
            recyclerAdapter.notifyDataSetChanged()
        }

        text_enter.setOnClickListener {
            checkPermission(0)
        }

        text_exit.setOnClickListener {
            checkPermission(1)
        }
    }

    fun addItem(text: String){
        Log.d("LOGTAG", "addItem")
        Log.d("LOGTAG", getTime(Calendar.getInstance().timeInMillis))
        item.add(CarData(text, Calendar.getInstance().timeInMillis))
        myRef.setValue(item)
    }

    fun editItem(position: Int, text: String){
        item[position].carName = text
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
        Log.d("LOGTAG", "removeItem")
        prefUtil.todayPrice += calculatePay(item[position].timeStamp)
        text_total.text = "${prefUtil.todayPrice} 원"
        item.removeAt(position)
        myRef.setValue(item)
    }

    private fun getTime(timeStamp: Long): String{
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(timeStamp)
    }

    //30분 500원, 10분 초과시 200원
    private fun calculatePay(prevTime: Long): Int {
        var enterMinute = (Calendar.getInstance().timeInMillis - prevTime) / 60000
        if (enterMinute <= 30) {
            return 500
        } else {
            return (500 + ((((enterMinute - 30) / 10) + 1) * 200)).toInt()
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

    @IgnoreExtraProperties
    data class Data(
        var carName: String = "",
        var timeStamp: Long = 0
    )
}
