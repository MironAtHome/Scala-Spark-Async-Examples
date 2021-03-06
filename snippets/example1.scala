import org.apache.spark.sql.functions.to_json
import scala.util.{Try, Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.spark.sql.types.{StructType, StructField, LongType}

val df = spark.sqlContext.read.json("testJson.json")
df.show()
var acc = spark.sparkContext.collectionAccumulator[(String)]("SomeName")
spark.time (
  df.
    select(to_json(struct(col("*"))).alias("content")).
	     rdd.
		foreachPartitionAsync(iter => 
		  {
			iter.foreach(row => 
			acc.add(
				row.getString(row.fieldIndex("content"))
			)
			) 
			Thread.sleep(10000)
		  }
		).
		onComplete { 
		  case Success(v) => println(s"Reporting from foreachPartitionAsync: ${acc.value.toArray.size}") 
		  case Failure (t) => println("Error occurred")
	    }
)
while(acc.value.toArray.size == 0) {
  System.out.println(s"Cannot advance forward. Array acc is empty. Time: ${java.time.Instant.now}")
  Thread.sleep(1000)
}
val jsonString = (new ObjectMapper()).writeValueAsString( acc.value.toArray )
jsonString
val schema = new StructType (
                   Array(
                     StructField("C1", LongType, true)
                     , StructField("C2", LongType, true)
                     , StructField("C3", LongType, true)
               )
            )
val round2acc = (new ObjectMapper()).readValue(jsonString, classOf[Array[String]])
val rddRound2 = sc.parallelize(round2acc(0) :: Nil)
var dfRound2 = spark.sqlContext.read.schema(schema).json(rddRound2.toDS)
dfRound2.show
var x = 1
while (x < round2acc.size) {
  val rddRound2 = sc.parallelize(round2acc(x) :: Nil)
  dfRound2 = dfRound2.union(spark.sqlContext.read.schema(schema).json(rddRound2.toDS))
  dfRound2.show
  x += 1
}