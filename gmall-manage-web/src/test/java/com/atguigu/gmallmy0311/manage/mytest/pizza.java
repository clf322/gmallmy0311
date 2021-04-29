package com.atguigu.gmallmy0311.manage.mytest;

 class Pizza {
   private  String parts;
   public void setParts(String parts){
       this.parts=parts;
   }

   public  String toString(){
       return  this.parts;
   }

}
abstract class PizzaBuilder{
     protected Pizza pizza;
     public Pizza getPizza(){
         return pizza;
     }
   public  void createNewPizza(){
         pizza=new Pizza();
   }
   public abstract void buildParts();


}
  class HawaiianPizzBuilder extends PizzaBuilder{


      @Override
      public void buildParts() {

              pizza.setParts("cross+mild"+"ham&pineapple");

      }

  }
class  SpicyPizzaBuilder extends  PizzaBuilder{


    @Override
    public void buildParts() {
        pizza.setParts("pan baked+hot+pepperoni&salami");
    }

}

class  Waiter{
     private PizzaBuilder pizzaBuilder;

    public Pizza getPizza() {
        return pizzaBuilder.getPizza();
    }

    public void setPizzaBuilder(PizzaBuilder pizzaBuilder) {
        this.pizzaBuilder = pizzaBuilder;
    }

    public void construct(){
        pizzaBuilder.createNewPizza();
        pizzaBuilder.buildParts();
    }

}

  class  FastFoodOrdering{
      public static void main(String[] args) {
          Waiter waiter=new Waiter();
          PizzaBuilder hawaiianPizzBuilder = new HawaiianPizzBuilder();
          waiter.setPizzaBuilder(hawaiianPizzBuilder);
          waiter.construct();
          System.out.println("pizza:"+waiter.getPizza());



      }

  }







