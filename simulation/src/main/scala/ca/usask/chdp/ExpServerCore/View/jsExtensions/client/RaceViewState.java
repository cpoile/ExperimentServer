package ca.usask.chdp.ExpServerCore.View.jsExtensions.client;

import com.vaadin.shared.ui.JavaScriptComponentState;

import java.util.List;
import java.util.Map;

public class RaceViewState extends JavaScriptComponentState {
   public List<List<Integer>> raceHistory;
   public List<List<Map<String, Integer>>> historyOfPosChange;
   public Boolean startRace = false;
   public Integer trackNum;

   public RaceViewState(List<List<Integer>> raceHistory,
                        List<List<Map<String, Integer>>> historyOfPosChange,
                        Integer trackNum) {
      this.raceHistory = raceHistory;
      this.historyOfPosChange = historyOfPosChange;
      this.trackNum = trackNum;
   }
}
