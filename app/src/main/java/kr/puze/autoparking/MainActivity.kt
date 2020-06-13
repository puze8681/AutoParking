package kr.puze.autoparking

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
    }

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
            startActivity(Intent(this@MainActivity, CameraActivity::class.java).putExtra("type", 0))
//            item.add(CarData("", Calendar.getInstance().timeInMillis))
//            myRef.setValue(item)
        }

        text_exit.setOnClickListener {
            startActivity(Intent(this@MainActivity, CameraActivity::class.java).putExtra("type", 1))
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
}
