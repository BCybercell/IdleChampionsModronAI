import java.util.ArrayList;

public class AutoCell extends Cell {
    public boolean used = false;
    public AutoCell(String _type, String _multiplier, ArrayList<Integer> _ports,
                        ArrayList<Integer> _ports_input){
        super(_type,_multiplier, _ports, _ports_input);

    }
}
