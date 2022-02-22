import java.util.ArrayList;

public class ModifierCell extends Cell {
    ArrayList<String> tiers;
    boolean isUsed = false;

    public ModifierCell(String _type, String _multiplier, ArrayList<Integer> _ports,
                ArrayList<Integer> _ports_input , ArrayList<String> _tiers){
        super(_type,_multiplier, _ports, _ports_input);
        tiers = _tiers;
    }

    public int getTierValue(double strength){
          int s = Math.toIntExact(Math.round(Math.floor(strength))) -1;
          if (s < 0){
              String value = tiers.get(0).split(",")[1];
              if (value.contains("."))
                  return Integer.parseInt(value.split("\\.")[0] + value.split("\\.")[1]);
              else return Integer.parseInt(value);
          }
        String value;
        if (s >= tiers.size()){
            value = tiers.get(tiers.size() - 1).split(",")[1];
        }
          else{
            value = tiers.get(s).split(",")[1];
        }
        if (value.contains("."))
            return Integer.parseInt(value.split("\\.")[0] + value.split("\\.")[1]);
        else return Integer.parseInt(value);
    }
}
