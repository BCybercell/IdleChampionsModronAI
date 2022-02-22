import java.util.ArrayList;

public class GAIndividual {
    Core core;
    ArrayList<Core> cores;
    ArrayList<Pipe> pipes;
    ArrayList<Integer> DNA;
    private int output = -999;
    public int level = 1;
    public ArrayList<Integer> levels = new ArrayList<>();

    public GAIndividual(ArrayList<Integer> d, ArrayList<Core> cs, ArrayList<Pipe> p, ArrayList<Integer> lvls){ //multicore
        cores = cs;
        DNA = d;
        pipes =p;
        levels = lvls;
        setUpCores();
    }

    private void setUpCore(){
        setUpCore(1);
    }

    private void setUpCore(int lvl){
        boolean test = false;
        int count = 0;
        ArrayList<String> emptyCellLocations = core.getEmptyCellLocations(lvl);
        for (String loc : emptyCellLocations) {
            int posHeight = Integer.parseInt(loc.split(",")[0]);
            int posWidth = Integer.parseInt(loc.split(",")[1]);
            if (DNA.size() > count && DNA.get(count) != -1 && pipes.size() > DNA.get(count)) {
                test = core.setPipe(pipes.get(DNA.get(count++)), posHeight, posWidth, lvl);
            } else count++;
        }
    }

    public void setUpCores(){
        boolean test = false;
        int count = 0;
        int coreCount = -1;
        for (Core c: cores
             ) {
            coreCount++;
            if (coreCount>= levels.size() || levels.get(c.core_id-1) == -1)
                continue;
            ArrayList<String> emptyCellLocations = c.getEmptyCellLocations(levels.get(c.core_id-1));
            for (String loc : emptyCellLocations) {
                int posHeight = Integer.parseInt(loc.split(",")[0]);
                int posWidth = Integer.parseInt(loc.split(",")[1]);
                if (DNA.size() > count && DNA.get(count) != -1 && pipes.size() > DNA.get(count)) {
                    test = c.setPipe(pipes.get(DNA.get(count)), posHeight, posWidth, levels.get(c.core_id-1));
                }
                count++;
            }
        }
    }

    public void resetOutput(){
        output = -999;
    }

    public int getOutput() {
        if (output == -999){
            output = 0;
            for (Core c: cores
                 ) {
                int temp = c.getOutputValue();
                if (temp < 0){
                    output = temp;
                    return output;
                }
                output += temp;
            }
        }
        return output;
    }

    public void removeCoresFromUse(){
        for (Core c: cores
             ) {
           c.inUse = false;
           Cache.resetCore(c);
        }
    }
}
