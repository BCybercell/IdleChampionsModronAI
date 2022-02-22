import java.util.ArrayList;

public class Pipe extends Cell{
    ArrayList<Double> multiInputBonus;
    int id;
    String pipeName;
    double strength;


    public Pipe(String name, String _multiplier, ArrayList<Integer> _ports,
                ArrayList<Integer> _ports_input, ArrayList<Double> _multiInputBonus, int _id){
        super("Pipe" + " " + name ,_multiplier, _ports, _ports_input);
        multiInputBonus = _multiInputBonus;
        id = _id;
        pipeName = name;
        strength = 0.0;
    }

    public double getMultiplier(){ // use
        double toReturn = Double.parseDouble(multiplier);
        int num = -2;
        for (int i = 0; i < 4; i++) {
            num+= actualInputs.get(i);
        }
        if (num < 0 || multiInputBonus.size() == 0){
            return Double.parseDouble(multiplier);
        }
        if (num+2 > timesCalled)
            return -1;
        int count =0;
        for (Double val: multiInputBonus
             ) {
            if (num>=count++){
                toReturn += val;
            }
        }

        return toReturn;
    }

    public double getMultiplier(double s){ // use
        //double toReturn = Double.parseDouble(multiplier);
        int num = -2;
        for (int i = 0; i < 4; i++) {
            num+= actualInputs.get(i);
        }
        if (num < 0 || multiInputBonus.size() == 0){
            return Double.parseDouble(multiplier);
        }
        if (num+2 > timesCalled){
            strength += s;
            return -1;
        }

        return -2;
    }
    public double getMultiInputStrength(double s){
        double temp = strength + s;
        strength = 0.0;
        double toReturn = Double.parseDouble(multiplier);
        int num = -2;
        for (int i = 0; i < 4; i++) {
            num+= actualInputs.get(i);
        }
        int count =0;
        for (Double val: multiInputBonus
        ) {
            if (num>=count++){
                toReturn += val;
            }
        }
        toReturn = Math.round(
                temp *(1 +toReturn)
                        *10000.0)/10000.0;
        return toReturn;
    }
}
