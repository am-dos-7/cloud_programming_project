package com.cloudComp

import scala.collection.mutable.HashMap
import scala.io.StdIn.readLine
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

class ConsoleSystem extends Actor{
  import Messages._ 
    // private var store_sys: ActorRef = null  // will hold ref to the store system

    override def receive: Receive =  {
      case Start(ar : ActorRef) =>
        var store_sys = ar

            // main execution stream
        while(true){
          var line = readLine(">_ ")
          var index : Int = 0
      
          // var cmd = new ListBuffer[String]()   // will serve to aggregate params to be sent to the store system
      
          if(line.length()>=6 && line.substring(0, 6) == "Store("){
            var key : String = ""
            var value : String = ""
            
            // We try to fetch the key
            index = 6   
            while(line.length > index && line.charAt(index)!=','){
              key += line.charAt(index)
              index += 1
            }
      
            // By there current element at index should be ',' if correct command structure
            if(line.length > index && line.charAt(index) == ','){
              index += 1
              while(line.length > index && line.charAt(index)!=')'){
                value += line.charAt(index)
                index += 1
              }
      
              // By there, the current element should be ')'
              if(line.length > index) {
                store_sys ! Store(key, value)
              }
              else{
                println("Incorrect command structure ! ")
              }
            }
            else{
              println("Incorrect command structure ! ") //; no ','")
            }
      
      
          }
          else if(line.length()>=7 && line.substring(0, 7) == "Lookup("){
      
              var key = fetch_key(line)
              if(key != null){
                store_sys ! Lookup(key)
              }
              else{
                println("Incorrect command structure ! ") //; no ')'")
              }
      
          }
          else if(line.length()>=7 && line.substring(0, 7) == "Delete("){
      
              var key = fetch_key(line)
              if(key != null){
                store_sys ! Delete(key)
              }
              else{
                println("Incorrect command structure ! ") //; no ')'")
              }
          }
          else{
              println("Unrecognized command")
          }
        }
    }

    def fetch_key(command: String): String = {
      var key = ""
      var index = 7   
      while(command.length > index && command.charAt(index)!=')'){
        key += command.charAt(index)
        index += 1
      }
      /* Element at current index should be ')' */
      if(command.length > index){
        return key
      }
      else{
        return null
      }
    }
    
  }