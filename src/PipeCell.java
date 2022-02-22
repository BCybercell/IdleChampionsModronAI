import java.util.ArrayList;

public class PipeCell extends Cell{
    String [] percentages ;
    String [] directions;
    ArrayList<Double> multiInputBonus;

    public PipeCell(String _type, String _multiplier, ArrayList<Integer> _ports,
                        ArrayList<Integer> _ports_input, ArrayList<Double> _multiInputBonus){
        super(_type,_multiplier, _ports, _ports_input);
        multiInputBonus = _multiInputBonus;
    }

    public double getMultiplier(){
        int num = -2;
        for (int i = 0; i < 4; i++) {
            num+= actualInputs.get(i);
        }
        if (num < 0 || multiInputBonus.size() == 0){
            return Double.parseDouble(multiplier);
        }
        if (num >= actualInputs.size())
            return multiInputBonus.get(multiInputBonus.size()-1);
        else return multiInputBonus.get(num);
    }
}
