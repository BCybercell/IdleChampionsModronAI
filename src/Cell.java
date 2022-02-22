import java.util.ArrayList;

public class Cell {
    public int levelLock;
    Cell upLink = null;
    Cell rightLink = null;
    Cell downLink = null;
    Cell leftLink = null;
    ArrayList<Cell> backlinks;
    ArrayList<Integer> actualInputs;
    int timesCalled = 0;
    ArrayList<Integer> ports;
    ArrayList<Integer> ports_input;
    String multiplier;
    String type;

    public Cell(){
        type = "Brick";
        actualInputs = new ArrayList<>();
        actualInputs.add(0);
        actualInputs.add(0);
        actualInputs.add(0);
        actualInputs.add(0);
        backlinks = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            backlinks.add(null);
        }
    }

    public Cell(String _type, int lvl){
        type = _type;
        levelLock = lvl;
        actualInputs = new ArrayList<>();
        actualInputs.add(0);
        actualInputs.add(0);
        actualInputs.add(0);
        actualInputs.add(0);
        backlinks = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            backlinks.add(null);
        }
    }

    public Cell(String _type, String _multiplier, ArrayList<Integer> _ports,
                ArrayList<Integer> _ports_input){
        type =_type;
        multiplier = _multiplier;
        ports = _ports;
        ports_input = _ports_input;
        actualInputs = new ArrayList<>();
        actualInputs.add(0);
        actualInputs.add(0);
        actualInputs.add(0);
        actualInputs.add(0);
        backlinks = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            backlinks.add(null);
        }

    }

    public boolean hasOtherInput(int inputShouldHave){
        if (!type.contains("Undirected"))
            return false;
        for (int i = 0; i < 4; i++) {
            if (i != inputShouldHave && actualInputs.get(i) == 1){
                return true;
            }
        }
        return false;
    }
}
