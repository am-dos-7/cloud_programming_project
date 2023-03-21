package com.cloudComp

import scala.collection.mutable.HashMap
import scala.io.StdIn.readLine
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
// import scala.collection.mutable.ListBuffer

class StoreSystem extends Actor {

    /* Remenber to add the delay before system response, at the end */

    import Messages._

    private var hash_tab = HashMap.empty[String,String]
    private var dur_stor: ActorRef = null   // will hold a ref on the durable storage object
    private var store_id = 0    // id of the store; will be set by the orchestrator
    private var orchest: ActorRef = null
    private var cache: ActorRef = null
    // The current design propagates Store and Delete msgs to to the DurableStorage actor
  override def receive: Receive = {
    case Start(ar : ActorRef) =>
        dur_stor = ar
        // println(dur_stor)
    case SetOrchestrator(ar: ActorRef) =>
        orchest = ar
    case SetId(id: Int) =>
        store_id = id
    case SetCache(ar: ActorRef) =>
        cache = ar
    case Store(key: String, value: String) =>
        // println("Store msg")
        dur_stor ! Store(key, value)        // propagate the msg to the durable storage
        cache ! Store(key, value)           // propagate to the associated cache    
        hash_tab(key) = value
        // println("Entry " + key + "->" + value + " saved !")
    case Lookup(key: String) =>
        cache ! Lookup(key)     // propagate to cache
        // if (hash_tab.contains(key)){
        //     println(key + "->" + hash_tab(key))
        // }
        
    case Delete(key: String) =>
        // println("Delete msg")             
        if (hash_tab.contains(key)){
            hash_tab.remove(key)
                dur_stor ! Delete(key)              // propagate the msg to the durable storage, if the key is currently stored
                cache ! Delete(key)                 // propagate to cache
            // println("Entry " + key + " deleted !")
        }
        else{
            // println("No entry for " + key)
        }
    case HasKey(key: String) =>
        if (hash_tab.contains(key)){
            orchest ! YesHasKey(store_id)
        }
        else{
            orchest ! NoHasKey()
        } 

    case NotInCache(key) => // got from the cache
        // print the entry if we have it
        // Thread.sleep(100)           // simulating the system response delay             
        if (hash_tab.contains(key)){
            println(key + "->" + hash_tab(key))
        }
    case _ =>
      println("Err!")

  }
}