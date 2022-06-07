package com.example.platformsciencecodingtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import org.json.JSONObject
const val TAG="MainActivity";
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val jsonString ="{" +
                "  \"shipments\": [" +
                "    \"215 Osinski Manors\"," +
                "    \"9856 Marvin Stravenue\"," +
                "    \"7127 Kathlyn Ferry\"," +
                "    \"987 Champlin Lake\"," +
                "    \"63187 Volkman Garden Suite 447\"," +
                "    \"75855 Dessie Lights\",\n" +
                "    \"1797 Adolf Island Apt. 744\"," +
                "    \"2431 Lindgren Corners\"," +
                "    \"8725 Aufderhar River Suite 859\"," +
                "    \"79035 Shanna Light Apt. 322\"" +
                "  ]," +
                "  \"drivers\": [" +
                "    \"Everardo Welch\"," +
                "    \"Orval Mayert\"," +
                "    \"Howard Emmerich\"," +
                "    \"Izaiah Lowe\"," +
                "    \"Monica Hermann\"," +
                "    \"Ellis Wisozk\"," +
                "    \"Noemie Murphy\"," +
                "    \"Cleve Durgan\"," +
                "    \"Murphy Mosciski\"," +
                "    \"Kaiser Sose\"" +
                "  ]" +
                "}"
        val data = Gson().fromJson(jsonString, Data::class.java)

       // Log.v(TAG,"shipments address size list: ${data.shipments?.size}")
        //Log.v(TAG,"Fifth name list: ${data.drivers?.get(4)}")

       val optimizedShipments= getHighestSS(data);
        // use arrayadapter and define an array
        val arrayAdapter: ArrayAdapter<*>


        // access the listView from xml file
        val mListView = findViewById<ListView>(R.id.userlist)
       arrayAdapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, ArrayList(optimizedShipments.keys))

            mListView.adapter = arrayAdapter
        mListView.setOnItemClickListener { parent, view, position, id ->
            val element = arrayAdapter.getItem(position) // The item that was clicked
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Shipment")
            builder.setMessage(optimizedShipments.get(element.toString()))
            builder.setPositiveButton("Continue") { dialog, which ->
                dialog.dismiss()
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    private fun getHighestSS(data: Data): LinkedHashMap<String,String> {
        var shipments: List<String> = ArrayList()
        var optimizedPairs:LinkedHashMap<String,String> = LinkedHashMap()
        if (data.shipments != null&& data.drivers!=null) {
         shipments = data.shipments!!
            var iterationSSList= mutableListOf<MutableList<Double>>()

            for(i in 0 until shipments.size){
                Log.v(TAG,"Beginning iteration: ${i}")
            iterationSSList.add(getIterationSS(data.drivers!![i],shipments)); //getting driver's SS scores for all shipments
                                      }
      //getting the highest SS in each iteration
            do{
            var valueMax=0.0
            var indexShipmentMax=0
            var previousIndexShipmentMax=0
            var previousValueMax=0.0
            var indexDriverMax=0;
                     for(i in 0 until iterationSSList.size) {
                valueMax= iterationSSList[i].maxOrNull()?:0.0
                    indexShipmentMax=iterationSSList[i].indices.maxByOrNull { iterationSSList[i][it] }?:0
                    Log.v(TAG,"Driver $i Highest SS= $valueMax")
                    if(valueMax>previousValueMax){ //this is to get the position of the highest SS overall
                        indexDriverMax=i;
                        previousIndexShipmentMax=indexShipmentMax;
                        previousValueMax=valueMax
                    }
            }
         optimizedPairs.put(data.drivers!![indexDriverMax], data.shipments!![previousIndexShipmentMax])  // generating the optimal shipment Hashmap by picking the highest SS overal each iteration
            Log.v(TAG,"Pair matched: Driver: ${data.drivers!![indexDriverMax]} , Shipment: ${data.shipments!![previousIndexShipmentMax]} ")
           iterationSSList= updateSSList(iterationSSList,indexDriverMax,previousIndexShipmentMax) //delete combinations that are no longer valid
            }while(optimizedPairs.size< data.drivers!!.size)
        }
        Log.v(TAG,"OptimizedPairs result: ${optimizedPairs.toString()} ")
                return optimizedPairs

    }

    private fun updateSSList(iterationSSList: MutableList<MutableList<Double>>, indexDriverMax: Int, previousIndexShipmentMax: Int): MutableList<MutableList<Double>> {
    for(i in 0 until iterationSSList[indexDriverMax].size){
        iterationSSList[indexDriverMax][i]=0.0 //discarding all other shipments for the current driver
    }
        for (i in 0 until iterationSSList.size) {
            iterationSSList[i][previousIndexShipmentMax]=0.0 //discarding all other drivers for this shipment
        }
        return iterationSSList
    }


    private fun getIterationSS(driver: String, shipments: List<String>): MutableList<Double> {
        val ssList= mutableListOf<Double>()
                     val   vowels = getVowels(driver)
            val consonants = getConsonants(driver)
            var bonus=1.0;
            for (i in 0..shipments.size - 1) {
                // checking common factors:
                val cf: Int = checkCommonFactors(shipments[i].length, driver.length);
                if (cf>1)
                    bonus=1.5
                else
                    bonus=1.0

                 //checking even or odd
                if (shipments[i].length % 2 == 0) { //check if its even
                    Log.v(TAG,"Vowels case Address length: ${shipments[i].length} SS=${vowels * 1.5*bonus} ")
                    ssList.add( vowels * 1.5*bonus)
                } else {
                    ssList.add( consonants*bonus)
                    Log.v(TAG,"Consonants case Address length: ${shipments[i].length} SS=${consonants *bonus} ")
                }


            }
        Log.v(TAG,"List of SS for driver  ${ssList.toString()} ")
               return ssList }

    private fun checkCommonFactors(n1: Int, n2: Int): Int {
        var gcd = 1

        var i = 1
        while (i <= n1 && i <= n2) {
            // Checks if i is factor of both integers
            if (n1 % i == 0 && n2 % i == 0)
                gcd = i
            ++i
        }
        return gcd
    }

    private fun getConsonants(name: String?): Int {
var c=0
        if (name != null) {
        val name2= name.lowercase()
                    for(i in name2.indices){
                        val ch=name2[i]
                        if (ch in 'a'..'z'&&ch!='a'&&ch!='e'&&ch!='i'&&ch!='o'&&ch!='u') {
            c++
                        }
                    }
        }
        Log.v(TAG,"Number of Consonants in $name : $c")
        return c
    }


    private fun getVowels(name: String?): Int {
 var v=0
        if (name != null) {
            val name2= name.lowercase()
            for(i in name2.indices){
                val ch=name2[i]
                if (ch == 'a' || ch == 'e' || ch == 'i'
                    || ch == 'o' || ch == 'u')
                        v++;
            }
                    }
        Log.v(TAG,"Number of Vowels in $name : $v")
        return v
    }
}