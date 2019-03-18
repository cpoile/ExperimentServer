package ca.usask.chdp.surveyRedirect.View.jsExtensions.client;

import com.vaadin.shared.ui.JavaScriptComponentState;

import java.util.ArrayList;
import java.util.List;

public class SvoSliderState extends JavaScriptComponentState {
   public String name;
   public List<Integer> itemID = new ArrayList<Integer>();
   public List<Integer> itemLowvalYou = new ArrayList<Integer>();
   public List<Integer> itemHighvalYou = new ArrayList<Integer>();
   public List<Integer> itemDescYou = new ArrayList<Integer>();
   public List<Integer> itemLowvalOther = new ArrayList<Integer>();
   public List<Integer> itemHighvalOther = new ArrayList<Integer>();
   public List<Integer> itemDescOther = new ArrayList<Integer>();
   public List<Double> timeChoice = new ArrayList<Double>(); //d
   public List<Integer> choiceYou = new ArrayList<Integer>();   //i
   public List<Integer> choiceOther = new ArrayList<Integer>(); //i
   public List<Double> ticksTime = new ArrayList<Double>(); //d
   public List<Integer> ticksValYou = new ArrayList<Integer>(); // i
   public List<Integer> ticksValOther = new ArrayList<Integer>(); //i
   public List<Double> firstSixAngle = new ArrayList<Double>(); // d
   public List<String> firstSixCat = new ArrayList<String>(); // str
   public List<Integer> perc = new ArrayList<Integer>(); // int
   public List<Double> sessionStart = new ArrayList<Double>();  // d
   public List<Double> first_item_timestamp = new ArrayList<Double>(); // d
   public List<Integer> transitHolds = new ArrayList<Integer>(); // int
   public List<String> secondRes = new ArrayList<String>(); //str
   public List<Double> altr_value = new ArrayList<Double>();//d
   public List<Double> indiv_value = new ArrayList<Double>(); //d
   public List<Double> ineqav_value = new ArrayList<Double>(); //d
   public List<Double> jointgain_value = new ArrayList<Double>(); //d

   public SvoSliderState(String name) {
      this.name = name;
   }
}
