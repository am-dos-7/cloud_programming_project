package com.cloudComp

import scala.collection.mutable.HashMap
import scala.io.StdIn.readLine
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.File
import scala.io.Source.fromFile

class DurableStorage extends Actor{
    import Messages._
    var file_name = "storage_file.txt"
    // var store_sys: ActorRef = null

    def writeFile(msg: String){ // write a string to the file
        val buf_w = new BufferedWriter(new FileWriter(new File(file_name), true))
        buf_w.write(msg + "\n")
        buf_w.close
    }

    def getValue(key: String): String = {  // get the value associated with  'key' in the file
        var lines = fromFile(file_name).getLines().toList.reverse
        
        var i : Int = 0
        var deleted = false
        while (i < lines.length){
            var key_val = lines(i).split(" ")   // split current line into key-value with sep=" "
            if (key_val(0) == key){ // keys match
                if (key_val(1)=="deleted"){ // if it is a delete entry
                    deleted = true          // mark as is
                }
                else{                       // if not a delete
                    if (deleted){          // if already marked as deleted
                        deleted = false   // release
                    }
                    else{                  // if not marked
                        return key_val(1) // return the value
                    }
                }
                
            }
            i += 1
        }
        // If we made it here, then the whole file has been traversed and no not-deleted value has been found
        return null
    }

    def loadFromFile2Mem(store_sys: ActorRef){   // load entries from the file to memory; will be called once at start-up
        // we will scan the file from top to bottom and send every entry as a command to the store system
        var lines = fromFile(file_name).getLines().toList
        
        var i : Int = 0
        // var deleted = false
        while (i < lines.length){
            var key_val = lines(i).split(" ")   // split current line into key-value with sep=" "
                if (key_val(1)=="deleted"){     // if it is a delete entry
                    store_sys ! Delete(key_val(0))  // ask the store systemm to delete it
                }
                else{                       // if not a delete; thus it is a store
                    store_sys ! Store(key_val(0), key_val(1))   // ask the store system to store it
                }

            i += 1
        }

    }

    override def receive: Receive = {
        case SetFileName(name: String) =>
            file_name = name
        case Start(ar : ActorRef) =>
            loadFromFile2Mem(ar)
            // println(store_sys)
        case Store(key: String, value: String) =>
            writeFile(key + " " + value)    // write the key-value whitespace-separated to the file
            // println("Entry " + key + "->" + value + " saved !")
        case Lookup(key: String) =>
            var value = getValue(key)
            if (value == null){
                // println("No entry for " + key + " !")
            }
            else{
                // println(key + "->" + value)
            }
        case Delete(key: String) =>
            writeFile(key + " deleted")     // key-deleted whitespace-separated
        case _ =>
            println("Err!")
    }

}