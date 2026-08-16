package com.nimeshkmr.core;

public class OrderQueue {
    OrderNode head;
    OrderNode tail;

    public void append(OrderNode node){
        if(this.isEmpty()){
            this.head = node;
            this.tail = node;
            node.next = null;
            node.prev = null;
        }
        else{
        node.prev = this.tail;
        this.tail.next = node;
        this.tail = node;
        this.tail.next = null;
        }  
    }

    public OrderNode getHead(){
        if(this.isEmpty()){
            return null;
        }
        return this.head;
    }

    public OrderNode getTail(){
        if(this.isEmpty()){
            return null;
        }
        return this.tail;
    }

    public void cancel(OrderNode node){
        if(node == this.head){
            if(this.head.next == null){
                this.head = null;
            }
            else{this.head = this.head.next;
            this.head.prev = null;}
        }
        else if(node == this.tail){
            this.tail = this.tail.prev;
            this.tail.next = null;
        }
        else{
            OrderNode newPrev = node.prev;
            OrderNode newNext = node.next;
            newPrev.next = newNext;
            newNext.prev = newPrev;
        }
    }

    public boolean isEmpty(){
        return this.head == null; 
    }

    public OrderQueue(){
        this.head = null;
        this.tail = null;
    }
}
