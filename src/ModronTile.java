import java.util.ArrayList;

public class ModronTile {
    String name;
    int tile_id;
    ArrayList<Integer> ports;
    ArrayList<Integer> ports_input;
    String multiplier;
    ArrayList<String> tiers;
    String type;
    ArrayList<Double> multiInputBonus;

    public ModronTile(int id, String n, ArrayList<Integer> p, ArrayList<Integer> pi,
                      String m, ArrayList<String> t, ArrayList<Double> mi){
        tile_id = id;
        name = n;
        ports = p;
        ports_input = pi;
        multiplier = m;
        tiers = t;
        multiInputBonus = mi;
        if (name.contains("Endpoint"))
            type = "ModifierCell";
        else if (name.contains("Automation"))
            type = "AutoCell";
        else if (name.contains("Power"))
            type = "InputCell";
        else if (name.toLowerCase().contains("directed"))
            type = "Pipe";
    }

}
