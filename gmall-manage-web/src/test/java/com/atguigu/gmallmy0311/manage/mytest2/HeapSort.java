package com.atguigu.gmallmy0311.manage.mytest2;

import java.util.Arrays;
       /*
  * 思路：
  * 1.方法adjustDown:对于一个数组a[],针对第i个数进行向下（直到len-1）调整，使得该位置成为大顶堆
   * 2.方法bulidMaxHeap:从len/2-1位置到0位置，循环调用adjustDown，使其成为大顶堆
   * 3.方法heapSort:建立大顶堆，让第一个与最后一个调换位置，然后将第一个adjustDown一下。循环。
   */

public class HeapSort {
    //建立大顶堆
    private static void buildMaxHeap(int[] a) {
        for (int i = (a.length / 2) - 1; i >= 0; i--) {
            adjustDown(a, i, a.length);
        }
    }

    //向下调整
    private static void adjustDown(int[] a, int i, int len) {
        int temp, j;
        temp = a[i];
        for (j = 2 * i + 1; j < len; j = 2 * j + 1) {       //j为当前i的子节点，默认为左节点
            if (j + 1 < len && a[j + 1] > a[j])       //如果右节点大，则选右节点
                j++;
            if (a[j] <= temp)                 //若子节点都比初始值temp小，说明找到了位置
                break;
            else {
                a[i] = a[j];                   //如果没有终止，那么将子节点中数值大的上调至i处
                i = j;                       //同时i下降到j这个位置
            }
        }
        a[i] = temp;                           //将temp放在最终的位置
    }

    //堆排序
    private static void heapSort(int[] a) {
        buildMaxHeap(a);
        for (int i = a.length - 1; i >= 0; i--) {
            int temp = a[0];
            a[0] = a[i];
            a[i] = temp;
            adjustDown(a, 0, i);  //将剩余len-1调整为大顶堆，循环，所以用i表示
        }
    }

    public static void main(String[] args) {
        int[] a = {5, 88, 45, 37, 91, 26, 13, 66, 50};
        heapSort(a);
        System.out.println(Arrays.toString(a));
    }


}

