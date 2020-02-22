package fse2006raxextended;

import java.lang.*;

/* 

The RAX Error - local deadlock. 

*/

class Event{
  int count = 0;
  int wrapCount = 3; 

  public Event(int wc) { wrapCount = wc; }

  public synchronized void wait_for_event(){
    try{wait();}catch(InterruptedException e){};
  }

  public synchronized void signal_event(){
    count = (count + 1) % wrapCount;
//System.out.println("count is "+count);
    notifyAll();
  }
}


class Events{
  static Event plan;
  static Event exec;  

  public static void initialize(int wc){
    plan = new Event(wc);
    exec = new Event(wc);
  }
}


class Plan{
  int[] timeline = new int[5];
  int currentToken = 0;

  public synchronized void generate(){
    timeline[0] = 10;
    timeline[1] = 11;
    timeline[2] = 12;
    timeline[3] = 13;
    timeline[4] = 14;
    currentToken = 0;
  }

  public synchronized int getCurrentToken(){
    return currentToken;
  }

  public synchronized boolean done(){
    return currentToken == 5;
  } 

  public synchronized void executeToken(){
    currentToken++;
  }

  public synchronized void repair(){
    currentToken = 0;
  }
}


class Planner extends Thread{
  Plan plan;
  int count = 0;

  public Planner(Plan plan){
    this.plan = plan;
//System.out.println("starting planner");
    this.start();
  }

  public void run(){
    count = Events.plan.count;
    while(true){
//System.out.println("planner loop with count ="+count+", Events.plan.count ="+Events.plan.count);
      if (count == Events.plan.count) {
//System.out.println("testing with count ="+count+", Events.plan.count ="+Events.plan.count);
	/*
        if (count != Events.plan.count) {
	  throw new RuntimeException("bug");
	}
	*/

        Events.plan.wait_for_event();
      }
      count = Events.plan.count;
      plan.generate();
      Events.exec.signal_event();
    }
  }
}

class Executive extends Thread{
  Plan plan;
  int count = 0;

  public Executive(Plan plan){
    this.plan = plan;
//System.out.println("starting executive");
    this.start();
  }

  public void run(){
    count = Events.exec.count;
    while(true){
//System.out.println("executive loop");
      Events.plan.signal_event();
      if (count == Events.exec.count)
        Events.exec.wait_for_event();
      count = Events.exec.count;
      while (!plan.done()) {
        plan.executeToken();
      }
    }
  }
}


class Diagnosis extends Thread{
  Plan plan;

  public Diagnosis(Plan plan){
    this.plan = plan;
//System.out.println("starting diagnosis");
    this.start();
  }

  public void run(){
    while (true){
//System.out.println("diagnosis loop");
      Events.exec.wait_for_event();
      plan.repair();
    }
  }
}


class Shared{
  int x = 0;

  public synchronized void write(int i){
    x = i;
  }
}


class Task extends Thread{
  Shared shared;

  public Task(Shared shared){
    this.shared = shared;
//System.out.println("starting task");
    this.start();
  }

  public void run(){
    while (true) {
//System.out.println("task running");
      shared.write(0);
    }
  }
}


class Environment{
  public static void create(int gc){
    for (int i = 0;i < gc;i++) {
      createGroup();
    }
  }

  public static void createGroup(){
    Shared shared = new Shared();
    new Task(shared);
    new Task(shared);
  }
}


class RAXextended{
  public static void main(String[] args){
    int gc = 0;
    int wc = 2;
    if (args != null && args.length == 2) {
      gc = Integer.parseInt(args[0]);
      wc = Integer.parseInt(args[1]);
    }

    //Environment.create();
    Events.initialize(wc);
    Plan plan = new Plan();
    new Planner(plan);
    new Executive(plan);
    new Diagnosis(plan);
    Environment.create(gc);
  }
}






