package com.atguigu.gmallmy0311.manage.mytest2;

import org.junit.Test;

import java.util.Arrays;


/**
 * 排序功能类
 */
public class sort {

    /**
     * 测试排序
     */
    @Test
    public  void testSelectSort() {
        //1.初始化变量
        int[] arr = {43, 21, 65, 23, 65, 33, 21, 12, 43, 54};
        System.out.println("=======选择排序开始==================");
        //2.调用选择排序方法
        selectSort(arr);
        //3.打印数组
        print(arr);
        System.out.println("=======冒泡排序开始==================");
       int[] arr1 = new int[]{43, 21, 65, 23, 65, 33, 21, 12, 43, 54};
        bubleSort(arr1);

    }







    /**
     * 2.冒泡排序
     */
  private static void  bubleSort (int[] arr){
      for (int i = 0; i < arr.length-1; i++) {
          for (int j = 0; j < arr.length-1-i; j++) {
            if (arr[j]>arr[j+1]){
                int t=arr[j];
                arr[j]=arr[j+1];
                arr[j+1]=t;
            }

          }

      }
      print(arr);

  }






    /** 1.选择排序
     *
     * @param arr
     */
    private static void selectSort (int[] arr){
        for (int i = 0; i < arr.length; i++) {
           int minIndex=i;
            for (int j = i+1; j < arr.length; j++) {
                if (arr[minIndex]>arr[j]){
                    minIndex=j;
                }



            }
            if (minIndex!=i){
                int temp=arr[i];
                arr[i]=arr[minIndex];
                arr[minIndex]=temp;
            }

        }

    }

    /**
     * 打印数组
     * @param arr
     */
    private static void print (int[] arr){
        for (int i : arr) {
            System.out.println(i);
        }

    }




}
