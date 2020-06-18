package kr.puze.autoparking

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object{
        lateinit var recyclerAdapter: CarRecyclerAdapter
        val firebaseDatabase = FirebaseDatabase.getInstance()
        var myRef = firebaseDatabase.reference.child("list")
        val item = ArrayList<CarData>()
        lateinit var context: Context
        private var REQUEST_PERMISSION_CODE = 100
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this@MainActivity
        recyclerAdapter = CarRecyclerAdapter(item, this@MainActivity)
        recycler_main.adapter = recyclerAdapter
        recyclerAdapter.notifyDataSetChanged()

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                item.clear()
                dataSnapshot.children.forEach{
                    it.getValue(Data::class.java)?.let { data ->
                        item.add(CarData(data.carName, data.timeStamp))
                    }
                }
                recyclerAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        text_total.setOnClickListener {

        }

        text_enter.setOnClickListener {
            checkPermission(0)
//            item.add(CarData("", Calendar.getInstance().timeInMillis))
//            myRef.setValue(item)
        }

        text_exit.setOnClickListener {
            checkPermission(1)
//            removeItem(0)
        }
    }

    fun removeItem(position: Int) {
        item.removeAt(position)
        myRef.setValue(item)
    }

    @IgnoreExtraProperties
    data class Data(
        var carName: String = "",
        var timeStamp: Long = 0
    )

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
}
