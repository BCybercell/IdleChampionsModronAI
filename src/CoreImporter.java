import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class CoreImporter {
    private static ArrayList<ModronTile> modronTiles;
    static ArrayList<Integer> importedDna;
    static ArrayList<Pipe> userPipes;
    private static String userID = "";
    private static String hash = "";
    private static String instanceKey = "";
    static ArrayList<Integer> levels;
    static ArrayList<Integer> xpAmounts;
    static JSONArray ownedTiles = new JSONArray();
    public CoreImporter(){

    }
    public static boolean ImportWRL(String location){
        try {
            File myObj = new File(location);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.contains("getuserdetails&language_id=1&user_id=")){
                    int index = data.indexOf("getuserdetails&language_id=1&user_id=")+37;
                    int index2 = data.indexOf("&hash=");
                    userID=data.substring(index, index2);
                    index = index2+6;
                    index2 = data.indexOf("&instance_key=");
                    hash = data.substring(index, index2);
                    return true;
                }
            }
            myReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return false;
        }
        return false;
    }

    public static void getInstanceID(){
        String objString = HttpsClient.call(
                "http://ps7.idlechampions.com/~idledragons/post.php?call=getuserdetails&include_free_play_objectives=true&instance_key=1&language_id=1&" +
                        "user_id=" +
                        userID +
                        "&hash=" +
                        hash);
        JSONParser parser = new JSONParser();
        try {
            var obj = parser.parse(objString);
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject details = (JSONObject) jsonObject.get("details");
            instanceKey = details.get("instance_id").toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void ImportModronTilesAndCores(int popSize){// + core
        String objString = HttpsClient.call(
                "https://ps6.idlechampions.com/~idledragons/post.php?call=getDefinitions&language_id=1&network_id=11&mobile_client_version=410");
        JSONParser parser = new JSONParser();
        try {
            JSONObject parsedObject = (JSONObject) parser.parse(objString);
            modronTiles = new ArrayList<>();

            JSONArray modron_tile_defines = (JSONArray) parsedObject.get("modron_tile_defines");
            JSONArray modron_core_defines = (JSONArray) parsedObject.get("modron_core_defines");
            for (Object o: modron_tile_defines)
            {
                JSONObject jsonObject = (JSONObject) o;
                int tile_id = Integer.parseInt(jsonObject.get("tile_id").toString());
                String name = jsonObject.get("name").toString();
                String multiplier = jsonObject.get("multiplier").toString();
                ArrayList<Integer> ports = new ArrayList<>();
                ArrayList<Integer> ports_input= new ArrayList<>();
                ArrayList<String> tiers= new ArrayList<>();
                ArrayList<Double> multiInputBonus= new ArrayList<>();

                JSONArray p = (JSONArray) jsonObject.get("ports");
                for (Object op: p
                ) {
                    ports.add(Integer.parseInt(op.toString()));
                }
                JSONArray pi = (JSONArray) jsonObject.get("ports_input");
                for (Object op: pi
                ) {
                    ports_input.add(Integer.parseInt(op.toString()));
                }
                if (!jsonObject.get("properties").equals("")){
                    JSONObject prop = (JSONObject) jsonObject.get("properties");
                    if (prop.containsKey("multi_input_bonuses")){
                        JSONArray mi = (JSONArray) prop.get("multi_input_bonuses");
                        for (Object m: mi
                        ) {
                            multiInputBonus.add(Double.parseDouble(m.toString()));
                        }
                    }
                    if (prop.containsKey("tiers")){
                        JSONArray ti = (JSONArray) prop.get("tiers");
                        for (Object m: ti
                        ) {
                            if (((JSONObject)m).containsKey("effect"))
                                tiers.add(((JSONObject)m).get("effect").toString());
                            else if (((JSONObject)m).containsKey("effect_string"))
                                tiers.add(((JSONObject)m).get("effect_string").toString());
                            else Logger.Error(m.toString());
                        }
                    }
                }
                ModronTile t = new ModronTile(tile_id, name, ports, ports_input, multiplier, tiers, multiInputBonus);
                modronTiles.add(t);
            }

            for (Object o: modron_core_defines)
            {
                JSONObject jsonObject = (JSONObject) o;
                Cache.setCoreJson(Integer.parseInt(jsonObject.get("core_id").toString()),jsonObject,popSize);
            }
        } catch (Exception e) {
            Logger.Error(e.getMessage());
            Logger.Error(Arrays.toString(e.getStackTrace()));
        }
    }

    public static void ImportUserDetails(){
        String objString = HttpsClient.call(
                "https://ps12.idlechampions.com/~idledragons/post.php?call=getuserdetails" +
                        "&instance_key=" +
                        instanceKey +
                        "&user_id=" +
                        userID +
                        "&hash=" +
                        hash);
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(objString);
            JSONObject details = (JSONObject) jsonObject.get("details");
            JSONObject modron_saves = (JSONObject) details.get("modron_saves");

            JSONArray tiles = (JSONArray) details.get("tiles");
            ownedTiles = tiles;
            userPipes = new ArrayList<>();
            for (Object o : tiles)
            {
                JSONObject tile = (JSONObject) o;
                int iType = Integer.parseInt(tile.get("tile_id").toString());
                int iAmount = Integer.parseInt(tile.get("count").toString());
                for (int i = 0; i < iAmount; i++)
                {
                    ModronTile placeableTile = getModronTile(iType);
                    if (placeableTile == null)
                        continue;
                    userPipes.add(new Pipe(placeableTile.name, placeableTile.multiplier,
                            placeableTile.ports, placeableTile.ports_input, placeableTile.multiInputBonus, iType));
                }
            }
            importedDna = new ArrayList<>();
            levels = new ArrayList<>();
            xpAmounts = new ArrayList<>();
            int coreCount = 1;
            for (var k :  modron_saves.keySet())
            {
                JSONObject core = (JSONObject) modron_saves.get(k);
                if (core == null)
                    continue;
                int xptolevel = Integer.parseInt(core.get("exp_total").toString());

                int corelevel = 1;
                int levelxp = 8000;
                while (xptolevel > (levelxp - 1)) {
                    corelevel += 1;
                    xptolevel -= levelxp;
                    levelxp += 4000;
                }
                while (coreCount != Integer.parseInt(k.toString())){
                    if (coreCount == 10)
                        break;
                    levels.add(-1);
                    xpAmounts.add(-1);
                    coreCount++;
                }
                coreCount++;
                levels.add(corelevel);
                xpAmounts.add(Integer.parseInt(core.get("exp_total").toString()));
                JSONArray grid = (JSONArray) core.get("grid");
                for (Object o: grid
                     ) {
                    JSONArray pipes = (JSONArray) o;
                    for (Object o2: pipes)
                    {
                        int iType = Integer.parseInt(o2.toString());
                        if (iType==0){
                            importedDna.add(-1);

                        }else {
                            int count = -1;
                            for (Pipe p: userPipes
                            ) {
                                count++;
                                if (p.id == iType && !importedDna.contains(count)){
                                    importedDna.add(count);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            try (PrintWriter writer = new PrintWriter(System.getProperty("user.dir") + "\\pipes.csv")) {
                StringBuilder sb = new StringBuilder();
                sb.append("Index");
                sb.append(';');
                sb.append("ID");
                sb.append(';');
                sb.append("Type");
                sb.append('\n');
                writer.write(sb.toString());
                int count = 0;
                for (Pipe p :userPipes
                ) {
                    sb = new StringBuilder();
                    sb.append(count++);
                    sb.append(';');
                    sb.append(p.id);
                    sb.append(';');
                    sb.append(p.type);
                    sb.append('\n');
                    writer.write(sb.toString());
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static String getModronTileType(int index){
        if (index < 1)
            return "Brick";
        for (ModronTile element : modronTiles){
            if (element.tile_id == index){
                return element.type;
            }
        }
        return "Brick";
    }

    public static ModronTile getModronTile(int index) {
        for (ModronTile element : modronTiles){
            if (element.tile_id == index){
                return element;
            }
        }
        return null;
    }
}
