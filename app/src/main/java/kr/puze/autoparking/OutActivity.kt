package kr.puze.autoparking

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_out.*

class OutActivity : AppCompatActivity() {

    companion object{
        lateinit var dialogContext: Context
        lateinit var recyclerAdapter: OutRecyclerAdapter
        val firebaseDatabase = FirebaseDatabase.getInstance()
        var outRef = firebaseDatabase.reference.child("out")
        val itemOut = ArrayList<OutData>()
        lateinit var context: Context
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_out)
        context = applicationContext
        dialogContext = this
        recyclerAdapter = OutRecyclerAdapter(itemOut, context, this)
        recycler_out.adapter = recyclerAdapter
        recyclerAdapter.notifyDataSetChanged()

        outRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("LOGTAG", "outRef OnDataChange")
                itemOut.clear()
                dataSnapshot.children.forEach {
                    it.getValue(Data::class.java)?.let { data ->
                        Log.d("LOGTAG", "outRef $data")
                        itemOut.add(OutData(data.carName, data.time, data.price))
                    }
                }
                recyclerAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    fun deleteItem(position: Int){
        itemOut.removeAt(position)
        outRef.setValue(itemOut)
        Toast.makeText(dialogContext,"삭제완료.", Toast.LENGTH_SHORT).show()
    }

    @IgnoreExtraProperties
    data class Data(
        var carName: String = "",
        var time: String = "",
        var price: Int = 0
    )
}
