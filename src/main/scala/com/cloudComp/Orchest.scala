package com.cloudComp

import scala.collection.mutable.HashMap
import scala.io.StdIn.readLine
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.collection.mutable.ListBuffer


class Orchest extends Actor {
    import Messages._

    private var store_list: ListBuffer[ActorRef] = ListBuffer[ActorRef]()  // will hold a ref on the durable storage object
    private var last_key: String = ""
    private var last_value: String = ""
    private var id_to_query = 0     // current id that a HasKey query will be sent to
    private var current_id = 0      // id that the load balancer will next pick for Store(key, value)

    override def receive: Receive = {
    case Start(ar : ActorRef) =>
        ar ! SetId(store_list.length)   // send the id as the number of store before the current joining in
        store_list.append(ar)       // append the received actor ref to the list
        
    case Store(key: String, value: String) =>
        last_key = key          // save the key and the value
        last_value = value
        current_id = 0          // reset: a new querying starts
        store_list(current_id) ! HasKey(key)   // start querying the store for who already has the key in 
        // increment_current_id
    case NoHasKey() =>
        if (current_id < store_list.length - 1){    // if the last element is not already reached
            current_id += 1
            store_list(current_id) ! HasKey(last_key)
        }
        else{ // the last element is reached (and it does not have the key)
            current_id = 0      // reset
            store_list(id_to_query) ! Store(last_key, last_value)   // ask the current store picked by the load balancer to Store the key-value
            id_to_query += 1
            if (id_to_query == store_list.length){
                id_to_query = 0
            }
        }
        
    case YesHasKey(id: Int) =>  // a store responded it has the key
        current_id = 0          // reset
        store_list(id) ! Store(last_key, last_value)    // ask this store to Store the key-value 
    case Lookup(key: String) =>
        // forward it to all stores
        current_id = 0
        while(current_id < store_list.length){
            store_list(current_id) ! Lookup(key)
            current_id += 1
        }
    case Delete(key: String) =>
        // forward the delete msg
        current_id = 0
        while(current_id < store_list.length){
            store_list(current_id) ! Delete(key)
            current_id += 1
        }
    case _ =>
        println("Err!")
  }
}