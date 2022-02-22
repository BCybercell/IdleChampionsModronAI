import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

public class Cache {
    private static final Map<Integer ,ArrayList<Core>> cores = new Hashtable<>();
    private static final Dictionary<Integer, JSONObject> coreJson = new Hashtable<>();
    private static int populationSize = 100;

    public static Core getCore(int id){
        if (cores.get(id) == null)
            initializeCores(id);
        for (Core core: cores.get(id)
             ) {
            if (!core.inUse)
            {
                core.inUse = true;
                return core;
            }
        }
        // Safety measure
        Logger.Error("Safety measure implemented");
        Core newCore = new Core(coreJson.get(id));
        cores.get(id).add(newCore);
        newCore.inUse = true;
        return  newCore;
    }

    public static void setCoreJson(int coreId, JSONObject json, int _populationSize){
        coreJson.put(coreId, json);
        populationSize = _populationSize;
        initializeCores(coreId);
    }

    private static void initializeCores(int coreId){
        Logger.Info("Initializing " + populationSize*1.1+ " cores");
        JSONObject json = coreJson.get(coreId);
        ArrayList<Core> newCores = new ArrayList<>();
        for (int i = 0; i < populationSize*1.1; i++) {
            newCores.add(new Core(json));
        }
        cores.put(coreId, newCores);
    }

    public static void resetAllCores(){
        Logger.Info("Resetting cores");
        for (int key: cores.keySet()
             ) {
            for (Core core: cores.get(key)
            ) {
                resetCore(core);
            }
        }

    }

    public static void resetCore(Core core){
        if (core.doNotRest)
            return;
        core.inUse = false;
        for (int i = 0; i < core.height; i++) {
            for (int j = 0; j < core.width; j++) {
                Cell temp = core.cells.get(i).get(j);
                temp.timesCalled =0;
                if (temp instanceof Pipe){
                    //Logger.Info("yes");
                    core.cells.get(i).set(j, new EmptyCell(1));
                }
                else{
                    temp.leftLink = null;
                    temp.downLink = null;
                    temp.rightLink = null;
                    temp.upLink = null;
                    temp.backlinks = new ArrayList<>();
                    for (int k = 0; k < 4; k++) {
                        temp.backlinks.add(null);
                    }
                }
            }
        }
    }

    public static ArrayList<Core> getBaseCores(){
        ArrayList<Core> toReturn = new ArrayList<>();
        for (var ignored : cores.keySet())
            toReturn.add(null);

        for (var k: cores.keySet())
        {
            if (cores.get(k).size()> 0)
                toReturn.set(k-1, cores.get(k).get(0));
        }
        return toReturn;
    }
}
