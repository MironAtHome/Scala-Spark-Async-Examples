# Scala Spark Async Examples

Scala appeared to gain prominence, 
thanks to appeal of Apache Spark speed.
But today's enterprise programming expects 
asynchronous code execution as a norm, particularly 
for fast code that scales. Spark is not fast enough 
to be immune from this requirement. Here is first script 
I put together over past 24 hours to cover this gap.
The most difficulty is scala Spark "either or" behavior 
with regards to producing output. When "it works", 
everything looks right, when it doesn't frequently no output is produced. 
No error or exception, no message. And so mounts sense of "something is wrong".
The answers to find a way from similar situation, when studying foreachPartitionAsync 
came from reading carefully Scala programming language description of Futures.
In the end all it took is to place .onComplete at the tail of the foreachPartitionAsync call, 
add import of 

and suddenly accumulator that was producing no output for good part of the day started 
behaving exactly same way, as in synchronous programming, when using foreachPartition, no Async suffix.

To some degree, this script is a result of reading following post on StackOverflow: 
https://stackoverflow.com/questions/41502550/foreachpartitionasync-throws-cannot-call-methods-on-stopped-sparkcontext

specifically this line from, supposedly, an expert, "That being said your code is invalid 
even if we ignore async actions. You cannot use distributed data structures inside 
an action or a transformation"
As much as I like when people share their insight, in this particular case I feel an 
presence of alternate example of correct usage was due. Because the overarching message 
of a professional should be, in my opinion, of encouraging one "here is how it should be done".
In the end, as it turns out, distributed structures can be used from within foreachPartitionAsync 
to a great deal of success.
