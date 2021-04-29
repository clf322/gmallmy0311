package com.atguigu.gmallmy0311.passport;

import ch.qos.logback.core.net.SyslogOutputStream;
import org.codehaus.groovy.runtime.powerassert.SourceText;

public class mytest {
    public static void main(String[] args) {
      int i=2;
        Long start=System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mytest.sleepOne();
            }
        }).start();
        sleepTwo();
        System.out.println(Thread.currentThread().getName()+Thread.activeCount());
        System.out.println(Thread.currentThread().getName()+(System.currentTimeMillis()-start)/1000);
        while (Thread.activeCount()>=i){
            Thread.yield();
        }
        System.out.println((System.currentTimeMillis()-start)/1000);



    }
      public  static     void sleepOne (){
          try {
              System.out.println(Thread.currentThread().getName());
              Thread.sleep(3000L);
          } catch (InterruptedException e) {
              e.printStackTrace();
          }


      }
    public  static     void sleepTwo (){
        try {
            System.out.println(Thread.currentThread().getName());
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }




}
