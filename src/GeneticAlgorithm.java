import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class GeneticAlgorithm {
    private Random randomGenerator;
    //Core core;
    ArrayList<Core> cores;
    ArrayList<Pipe> pipes;
    ArrayList<Integer> DNA = new ArrayList<>();
    //int level;
    ArrayList<Integer> levels;
    Home home;

    public GeneticAlgorithm(Home h){
        cores = Cache.getBaseCores();
        pipes = CoreImporter.userPipes;
        home = h;
    }

    private ArrayList<Pipe> clonePipes(){
        ArrayList<Pipe> toReturn = new ArrayList<>();

        for (Pipe pipe: pipes
        ) {
            toReturn.add(new Pipe(pipe.pipeName, pipe.multiplier,
                    pipe.ports, pipe.ports_input, pipe.multiInputBonus, pipe.id));
        }
        return toReturn;
    }

    private ArrayList<GAIndividual> createInitialPopulationMultiCore(int size, ArrayList<Integer> importedDna){

        ArrayList<GAIndividual> population = new ArrayList<>();
        int totalCells = 0;
        for (int i =0; i< levels.size();i++)
        {
            totalCells +=  cores.get(i).getEmptyCellLocations(levels.get(i)).size();
        }


        randomGenerator = new Random();
        for (int i = 0; i < size; i++) {
            // make dna
            DNA = new ArrayList<>(); //
            if (i==0){ // only 1 individual
                int additive = 0;
                for (int j =0; j< levels.size();j++) {
                    for (String loc : cores.get(j).getEmptyCellLocations(levels.get(j))) {
                        int posHeight = Integer.parseInt(loc.split(",")[0]);
                        int posWidth = Integer.parseInt(loc.split(",")[1]);
                        int index = posHeight * cores.get(j).width + posWidth;

                        DNA.add(importedDna.get(index+additive));
                    }
                    if (levels.get(j) != -1)
                        additive+=(cores.get(j).width*cores.get(j).height);
                }
            }

            int sizeToUse = pipes.size();
            if (sizeToUse < totalCells)
                sizeToUse = totalCells;
            while (sizeToUse != DNA.size()){

                int index = randomGenerator.nextInt(sizeToUse);
                if (index >= pipes.size()){
                    index = -1;
                    DNA.add(index);
                }
                else if (!DNA.contains(index))
                    DNA.add(index);
            }
            ArrayList<Core> clones = new ArrayList<>();

            try {
                for (Core c: cores
                ) {
                    if (levels.get(c.core_id-1) != -1)
                        clones.add(Cache.getCore(c.core_id));
                }

                population.add(new GAIndividual(DNA, clones, clonePipes(), levels));
            }
            catch (Exception e){
                Logger.Error(e.getMessage());
                Logger.Error(Arrays.toString(e.getStackTrace()));
            }

        }
        return  population;
    }

    private ArrayList<GAIndividual> createInitialPopulationMultiCore(int size){

        ArrayList<GAIndividual> population = new ArrayList<>();
        int totalCells = 0;
        int count = 0;
        for (Core c:cores
             ) {
            if (count>= levels.size())
                continue;
            totalCells +=  c.getEmptyCellLocations(levels.get(count++)).size();
        }

        randomGenerator = new Random();
        for (int i = 0; i < size; i++) {
            DNA = new ArrayList<>();
            int sizeToUse = pipes.size();
            if (sizeToUse < totalCells)
                sizeToUse = totalCells;
            while (sizeToUse != DNA.size()){

                int index = randomGenerator.nextInt(sizeToUse);
                if (index >= pipes.size()){
                    index = -1;
                    DNA.add(index);
                }
                else if (!DNA.contains(index))
                    DNA.add(index);
            }
            ArrayList<Core> clones = new ArrayList<>();

            try {
                for (Core c: cores
                ) {
                    if (levels.get(c.core_id-1) != -1)
                        clones.add(Cache.getCore(c.core_id));
                }

                population.add(new GAIndividual(DNA, clones, clonePipes(), levels));
            }
            catch (Exception e){
                Logger.Error(e.getMessage());
                Logger.Error(Arrays.toString(e.getStackTrace()));
            }

        }
        return  population;
    }

    private ArrayList<Integer> convertDnaToPipeIds(ArrayList<Integer> dnaString){
        ArrayList<Integer> toReturn = new ArrayList<>();
        for (int dna: dnaString
             ) {
            if (dna != -1){
                toReturn.add(pipes.get(dna).id);
            }
            else {
                toReturn.add(-1);
            }
        }
        return toReturn;
    }

    public void RunLongDNAMultiCore(int populationSize,int generations, ArrayList<Integer> lvls,
                                    ArrayList<Integer> importedDNA){
        levels = lvls;
        ArrayList<GAIndividual> population;
        boolean hasImportedDna = false;
        for (int d:importedDNA
             ) {
            if (d> -1)
            {
                hasImportedDna = true;
                break;
            }
        }
        //Cache.resetAllCores();
        if (hasImportedDna)
            population = createInitialPopulationMultiCore(populationSize, importedDNA);
        else
            population = createInitialPopulationMultiCore(populationSize);

        // first run
        Logger.Info("createInitialPopulation");
        int best = -1;
        for (GAIndividual gaIndividual: population
        ) {
            int out = gaIndividual.getOutput(); // multicore
            if (out > best)
            {
                best = out;
            }
        }
        Logger.Info("Run 1 done");

        int bestTracker = -99;
        int bestTrackerCount= 0;
        GAIndividual bestIndividual = null;
        double mod = 0.0;
        try (PrintWriter writer = new PrintWriter(System.getProperty("user.dir") + "\\Output.csv")) {
            StringBuilder sb = new StringBuilder();
            sb.append("Gen");
            sb.append(';');
            sb.append("Size");
            sb.append(';');
            sb.append("Best");
            sb.append(';');
            sb.append("DNA");
            sb.append(';');
            sb.append("DNA (pipe ids)");
            sb.append('\n');
            writer.write(sb.toString());
            int percentage = generations/(100);
            if (percentage == 0)
                percentage = 1;
            for (int i = 0; i < generations; i++) {
                // tournament
                int toChange = (int) (populationSize *(0.1+mod));
                for (int j = 0; j < toChange; j++) { // kill 20 percent
                    GAIndividual kill = tournamentSelection(population, false, 3);
                    // delete
                    kill.removeCoresFromUse();
                    population.remove(kill);
                }

                population.addAll(applyGeneticOperatorsMultiCore(population, toChange));

                bestIndividual = null;
                best = -6666;

                for (GAIndividual gaIndividual: population
                ) {
                    int out = gaIndividual.getOutput();
                    if (out > best){
                        best = out;
                        bestIndividual = gaIndividual;
                    }
                }

                if (i % 100 == 0){
                    Logger.Info("Gen: " +i);
                    Logger.Info("Best: " +best);
                    Logger.Info("Mod: " +mod);
                    if (bestIndividual != null){
                        int count =0;
                        bestIndividual.setUpCores();
                        for (Core c : bestIndividual.cores
                             ) {
                            Logger.Info("core "+ (++count) +": " +c.getOutputValue());
                        }
//                        if (i%1000 ==0){
//
//                            for (Core c : bestIndividual.cores
//                            ) {
//                                c.printPath();
//                            }
//                        }
                    }

                }
                if (bestTracker != best){
                    bestTracker = best;
                    bestTrackerCount = 0;
                    mod = 0.0;
                }
                bestTrackerCount++;

                if (bestTrackerCount > 0 && bestTrackerCount % 10 == 0 && mod < 0.5){
                    mod += 0.01;
                }
                if (mod > 0.4){
                    mod = 0.0;
                    Logger.Info("Re creating population MC");
                    if (bestIndividual != null){
                        for (Core c: bestIndividual.cores
                        ) {
                            c.doNotRest = true;
                        }
                    }
                    Cache.resetAllCores();
                    if (bestIndividual != null){
                        for (Core c: bestIndividual.cores
                        ) {
                            c.doNotRest = false;
                        }
                        population = createInitialPopulationMultiCore(populationSize-1);
                        population.add(bestIndividual);
                    }
                    else {
                        population = createInitialPopulationMultiCore(populationSize);
                    }

                    Logger.Info("Re created population MC");
                }
                sb = new StringBuilder();
                sb.append(i);
                sb.append(';');
                sb.append(population.size());
                sb.append(';');
                sb.append(best);
                sb.append(';');
                sb.append(bestIndividual != null ? bestIndividual.DNA.toString() : "");
                sb.append(';');
                sb.append(bestIndividual != null ? convertDnaToPipeIds(bestIndividual.DNA).toString() : "");
                sb.append('\n');
                writer.write(sb.toString());

                if (i % percentage ==0){
                    home.addProgress();
                    home.appendNewText("Generation " + i + " of " + generations);
                    if (bestIndividual != null)
                    {
                        for (Core core: bestIndividual.cores
                        ) {
                            JSONObject export = new JSONObject();
                            JSONArray grid = new JSONArray();
                            for (var row: core.cells
                            ) {
                                JSONArray jsonPipes = new JSONArray();
                                for (var pipe: row
                                ) {
                                    if (pipe instanceof Pipe){
                                        jsonPipes.add(((Pipe) pipe).id);
                                    }
                                    else
                                    {
                                        jsonPipes.add(0);
                                    }
                                }
                                grid.add(jsonPipes);
                            }

                            JSONObject coreJson = new JSONObject();
                            coreJson.put("core_id", String.valueOf(core.core_id));
                            coreJson.put("grid", grid);
                            coreJson.put("exp_total", String.valueOf(CoreImporter.xpAmounts.get(core.core_id-1)));
                            export.put("core", coreJson);
                            export.put("ownedTiles", CoreImporter.ownedTiles);
                            FileWriter file = null;
                            try {
                                file = new FileWriter(System.getProperty("user.dir") + "\\"+core.core_id+".json");
                                file.write(export.toJSONString());

                            } catch (IOException e) {
                                e.printStackTrace();

                            }finally {
                                try {
                                    file.flush();
                                    file.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }

            if (bestIndividual != null)
            {
                for (Core core: bestIndividual.cores
                ) {
                    JSONObject export = new JSONObject();
                    JSONArray grid = new JSONArray();
                    for (var row: core.cells
                    ) {
                        JSONArray jsonPipes = new JSONArray();
                        for (var pipe: row
                        ) {
                            if (pipe instanceof Pipe){
                                jsonPipes.add(((Pipe) pipe).id);
                            }
                            else
                            {
                                jsonPipes.add(0);
                            }
                        }
                        grid.add(jsonPipes);
                    }

                    JSONObject coreJson = new JSONObject();
                    coreJson.put("core_id", String.valueOf(core.core_id));
                    coreJson.put("grid", grid);
                    coreJson.put("exp_total", String.valueOf(CoreImporter.xpAmounts.get(core.core_id-1)));
                    export.put("core", coreJson);
                    export.put("ownedTiles", CoreImporter.ownedTiles);
                    FileWriter file = null;
                    try {
                        file = new FileWriter(System.getProperty("user.dir") + "\\"+core.core_id+".json");
                        file.write(export.toJSONString());

                    } catch (IOException e) {
                        e.printStackTrace();

                    }finally {
                        try {
                            file.flush();
                            file.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Logger.Info("Final: ");
                int count = 0;
                for (Core c : bestIndividual.cores
                ) {
                    Logger.Info("core "+ (++count) +": " +c.getOutputValue());
                }
            }
        } catch (FileNotFoundException e) {
            Logger.Error(e.getMessage());
        }

    }

    private GAIndividual mutationMultiCore(GAIndividual parent){
        ArrayList<Core> clones = new ArrayList<>();
        for (Core c: cores
             ) {
            clones.add(Cache.getCore(c.core_id));
        }
        ArrayList<Integer> childDna = new ArrayList<>(parent.DNA);
        int index = randomGenerator.nextInt(childDna.size()-3);
        for (int i = index; i < childDna.size()-1; i+=2) {
            int temp = childDna.get(i);
            childDna.set(i , childDna.get(i+1));
            childDna.set(i + 1, temp);
        }
        return new GAIndividual(childDna, clones, clonePipes(), levels);
    }

    private GAIndividual pointMutationMultiCore(GAIndividual parent){
        ArrayList<Core> clones = new ArrayList<>();
        for (Core c: cores
        ) {
            clones.add(Cache.getCore(c.core_id));
        }
        ArrayList<Integer> childDna = new ArrayList<>(parent.DNA);
        int index = randomGenerator.nextInt(childDna.size()-1);
        int index2 = randomGenerator.nextInt(childDna.size()-1);
        int temp = childDna.get(index);
        childDna.set(index , childDna.get(index2));
        childDna.set(index2, temp);
        return new GAIndividual(childDna, clones, clonePipes(), levels);
    }

    private GAIndividual creationMultiCore(){
        ArrayList<Core> clones = new ArrayList<>();
        int totalCells = 0;

        int count = 0;
        for (Core c: cores
        ) {
            if (count>= levels.size())
                continue;
            clones.add(Cache.getCore(c.core_id));
            totalCells += c.getEmptyCellLocations(levels.get(count++)).size();
        }

        DNA = new ArrayList<>();
        int sizeToUse = pipes.size();
        if (sizeToUse < totalCells)
            sizeToUse = totalCells;
        while (sizeToUse != DNA.size()){

            int index = randomGenerator.nextInt(sizeToUse);
            if (index >= pipes.size()){
                index = -1;
                DNA.add(index);
            }
            else if (!DNA.contains(index))
                DNA.add(index);
        }

        return new GAIndividual(DNA, clones, clonePipes(), levels);

    }

    private GAIndividual reproductionMultiCore(GAIndividual parent){
        ArrayList<Core> clones = new ArrayList<>();
        for (Core c: cores
        ) {
            clones.add(Cache.getCore(c.core_id));
        }
        return new GAIndividual(new ArrayList<>(parent.DNA), clones, clonePipes(), levels);
    }

    private GAIndividual crossoverMultiCore(GAIndividual parentA, GAIndividual parentB){
        ArrayList<Core> clones = new ArrayList<>();
        for (Core c: cores
        ) {
            clones.add(Cache.getCore(c.core_id));
        }
        ArrayList<Integer> childDna = new ArrayList<>(parentA.DNA);
        int index = randomGenerator.nextInt(childDna.size()-3);
        //clear out
        for (int i = index; i < childDna.size(); i++) {
            childDna.set(i, -1);
        }
        //cross
        for (int i = index; i < childDna.size(); i++) {
            if (!childDna.contains(parentB.DNA.get(i)))
            {
                childDna.set(i, parentB.DNA.get(i));
            }
        }
        // fill gaps
        for (int i = 0; i < childDna.size(); i++) {
            if (!childDna.contains(parentB.DNA.get(i)))
            {
                for (int j = 0; j < childDna.size(); j++) {
                    if (childDna.get(j) == -1){
                        childDna.set(j, parentB.DNA.get(i));
                        break;
                    }
                }
            }
        }

        return new GAIndividual(childDna, clones, clonePipes(), levels);
    }

    private ArrayList<GAIndividual> applyGeneticOperatorsMultiCore(ArrayList<GAIndividual> population,int numChildrenToReturn) {
        ArrayList<GAIndividual> toReturn = new ArrayList<>();

        for (int i = 0; i < numChildrenToReturn; i++) {
            int choice = randomGenerator.nextInt(100);
            GAIndividual parent = tournamentSelection(population, true, 3);

            if (choice <= 20){
                //mutation
                toReturn.add(mutationMultiCore(parent));
            }
            else if (choice <= 30){
                toReturn.add(pointMutationMultiCore(parent));
            }
            else if (choice <=45){
                // creation
                toReturn.add(creationMultiCore());
            }
            else if (choice <=60){
                // reproduction
                toReturn.add(reproductionMultiCore(parent));
            }
            else {
                // crossover
                GAIndividual parent2 = tournamentSelection(population, true, 5);
                toReturn.add(crossoverMultiCore(parent, parent2));
            }
        }

        return toReturn;
    }

    private GAIndividual tournamentSelection(ArrayList<GAIndividual> population, boolean fittestSelection, int size) {
        randomGenerator = new Random();
        ArrayList<Integer> indexList = new ArrayList<>();
        ArrayList<GAIndividual> tourn = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            boolean found = false;
            int index = -1;
            while (!found){
                index =  randomGenerator.nextInt(population.size());
                if (!indexList.contains(index))
                {
                    indexList.add(index);
                    found = true;
                }
            }
            tourn.add(population.get(index));
        }
        int toReturn = 0;
        for (int i = 0; i < size; i++) {
            if (fittestSelection){
                if (tourn.get(toReturn).getOutput() < tourn.get(i).getOutput())
                    toReturn = i;
            }
            else {
                if (tourn.get(toReturn).getOutput() > tourn.get(i).getOutput())
                    toReturn = i;
            }
        }
        return tourn.get(toReturn);

    }
}
