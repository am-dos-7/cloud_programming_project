/*
 * Amavi DOSSA
 * Cloud Programming
 * Project
 * 13/03/2023  
*/

package com.cloudComp

// import akka.actor.ActorSystem

import scala.collection.mutable.HashMap
import scala.io.StdIn.readLine
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import _root_.com.cloudComp.com.cloudComp.Cache
import scala.io.StdIn.readLine

object Messages {
  case class Store(key: String, value: String)
  case class Lookup(key: String)
  case class Delete(key: String)
  // case class Delete[A] (value : A)
  case class Start( actorRef: ActorRef )
  // case class AddStoreSystem(actorRef: ActorRef)
  case class SetFileName( fileName: String)   // used for comm between StoreSystem and DurableStorage
  case class SetOrchestrator(ar: ActorRef)
  case class SetId(id: Int)
  case class HasKey(key: String)
  case class YesHasKey(id: Int)
  case class NoHasKey()
  // case class Deleted()              // from Cache to StoreSystem to tell it has deleted an entry
  case class SetCache(ar: ActorRef) // to StoreSystem, with ref to Cache
  case class NotInCache(key: String)         // from Cache to StoreSystem
  case class PutInCache(key: String, value:String)  // from StoreSystem to Cache
}


object MainProc extends App { 
  import Messages._
  // Create actor system
  val as = ActorSystem("ActorSystem")
  println(as)

  val dur_stor1 = as.actorOf(Props[DurableStorage], "DurableStorage1")
  dur_stor1 ! SetFileName("storage_file1.txt")
  val dur_stor2 = as.actorOf(Props[DurableStorage], "DurableStorage2")
  dur_stor2 ! SetFileName("storage_file2.txt")
  val dur_stor3 = as.actorOf(Props[DurableStorage], "DurableStorage3")
  dur_stor3 ! SetFileName("storage_file3.txt")
  
  val orchest = as.actorOf(Props[Orchest], "Orchestrator")
  
  val store_sys1 = as.actorOf(Props[StoreSystem], "StoreSystem1")
  store_sys1 ! SetOrchestrator(orchest)
  store_sys1 ! Start(dur_stor1)
  dur_stor1 ! Start(store_sys1)

  val store_sys2 = as.actorOf(Props[StoreSystem], "StoreSystem2")
  store_sys2 ! SetOrchestrator(orchest)
  store_sys2 ! Start(dur_stor2)
  dur_stor2 ! Start(store_sys2)

  val store_sys3 = as.actorOf(Props[StoreSystem], "StoreSystem3")
  store_sys3 ! SetOrchestrator(orchest)
  store_sys3 ! Start(dur_stor3)
  dur_stor3 ! Start(store_sys3)

  // Instantiating caches
  val cache1 = as.actorOf(Props[Cache], "Cache1")
  cache1 ! Start(store_sys1)
  store_sys1 ! SetCache(cache1)

  val cache2 = as.actorOf(Props[Cache], "Cache2")
  cache2 ! Start(store_sys2)
  store_sys2 ! SetCache(cache2)

  val cache3 = as.actorOf(Props[Cache], "Cache3")
  cache3 ! Start(store_sys3)
  store_sys3 ! SetCache(cache3)

  orchest ! Start(store_sys1)
  orchest ! Start(store_sys2)
  orchest ! Start(store_sys3)
  
  var out = readLine("Please, choose an execution mode:\n[1] Manual execution: Interactive command prompt\n[2] Execute randomly generated instructions\n>_ ")
  if (out.equals("1")){
    val console_sys = as.actorOf(Props[ConsoleSystem], "ConsoleSystem")
    console_sys ! Start(orchest)
  }
  else if (out.equals("2")){  
    /* Client program */
    val rand = new scala.util.Random
    val nb_rqst = 1200  // number of request issued
    var i = 0
    var key :String = ""
    println("Populating the system with randomly generated keys...")
    while (i < nb_rqst){
      // var tmp = rand.between(1,501)
      key = (rand.nextInt(500) + 1).toString  // [1, 500]
      // key = tmp.toString()
      orchest ! Store(key, key)   // store the key with value the same as the key
      i += 1
      println("key:" + key)
      Thread.sleep(10)      // in ms
    }


    var last_key_lookd = "1"  // last key looked up; 1 as default to start
    var op = 0    // operation
    val nb_op = 500   // number of operation
    i = 0
    println("Performing random operations...")
    while(i < nb_op){
      op = rand.nextInt(3)
      if (op == 0){ // Store operation
        // pick a key greater than 500
        key = (rand.nextInt(500) + 501).toString // [501, 1000]
        println("Store(" + key + "," + key + ")")
        orchest ! Store(key, key)
      }
      else if (op == 1){  // Lookup op
        // Use the last key lookup if 0 is randomly picked
        if(rand.nextInt(2) == 0){
          println("Lookup(" + last_key_lookd + ")")
          orchest ! Lookup(last_key_lookd)
        }
        else{ // pick a new key [1, 500] otherwise
          key = (rand.nextInt(500) + 1).toString // [1, 500]
          last_key_lookd = key
          println("Lookup(" + last_key_lookd + ")")
          orchest ! Lookup(key)
        }
      }
      else if (op == 2){  // Delete op
        key = (rand.nextInt(500) + 501).toString // [501, 1000]
        println("Delete(" + key + ")")
        orchest ! Delete(key)
      }

      Thread.sleep(10)    // in ms
      i += 1
    }

  }

  else{
    println("Unsupported option ! Program will terminate !")
    System.exit(0)
  }
}

