package com.cloudComp

package com.cloudComp

import scala.collection.mutable.HashMap
import scala.io.StdIn.readLine
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import java.io.{BufferedWriter, FileWriter, File}
import scala.io.Source.fromFile

class Cache extends Actor{
    import Messages._
    var store_sys: ActorRef = null
    val cache_lmt = 1000    // limit of the cache
    var cache_mem = HashMap.empty[String,String]
    val rand = new scala.util.Random

    def receive: Receive = {
        case Start(ar) =>   // get ref to the actor sys
            store_sys = ar
        case Store(key, value) =>
            if (cache_mem.size < cache_lmt){    // if there is space, just insert it
                cache_mem(key) = value
            }
            else{   // otherwise, we will need to remove an existing entry before inserting the new one
                var tmp = rand.nextInt()%cache_lmt  // get a random number in the range 0-limit
                var it = cache_mem.iterator         // get an iterator on the hashmap
                
                // now we will iterate the picked-random-number times
                var str = it.next()
                var i = 1
                while(i < tmp){
                    str = it.next()
                    i += 1
                }
                cache_mem.remove(str._1)    // remove the entry with the key 
                cache_mem(key) = value      // now store the new entry
            }
        case Lookup(key) =>
            if (cache_mem.contains(key)){
                println(key + "->" + cache_mem(key))
            }
            else{
                store_sys ! NotInCache(key)
            }
        case Delete(key) =>
            if (cache_mem.contains(key)){   // delete the entry if we have it
                cache_mem.remove(key)
                // store_sys ! Deleted()       // and let the store system know
            }
        // case PutInCache()
        case _ =>
            println("Err!")
    }
}