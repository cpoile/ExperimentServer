package ca.usask.chdp.ExpServerCore.View.jsExtensions.client;

import com.vaadin.shared.ui.JavaScriptComponentState;

import java.util.ArrayList;
import java.util.List;


public class FlotGraphState extends JavaScriptComponentState {
   public List<List<Integer>> proj2Work = new ArrayList<List<Integer>>();
   public List<List<Double>> proj2TotalWork = new ArrayList<List<Double>>();
   public Boolean isInitialized = true;
}
