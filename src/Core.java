import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class Core {
    int width;
    int height;
    ArrayList<ArrayList<Cell>> cells;
    Cell inCell = null;
    int inCellRow;
    int inCellCol;
    AutoCell automationCell = null;
    JSONObject createdBy;
    boolean inUse;
    int core_id;

    boolean doNotRest = false;

    public Core(JSONObject p) {
        inUse = false;
        createdBy = p;
        core_id = Integer.parseInt(p.get("core_id").toString());

        JSONArray gridBase = (JSONArray) p.get("grid_base");
        JSONArray grid_levels = (JSONArray) p.get("grid_levels");
        height = gridBase.size();
        width = ((JSONArray)(gridBase).get(0)).size();
        cells = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            cells.add(new ArrayList<>());
            for (int j = 0; j < width; j++) {
                JSONArray row = (JSONArray) gridBase.get(i);
                int cellType = Integer.parseInt( row.get(j).toString());
                if (cellType == 0){
                    JSONArray lvlRow = (JSONArray) grid_levels.get(i);
                    int lvl = Integer.parseInt( lvlRow.get(j).toString());
                    cells.get(i).add(new EmptyCell(lvl ));
                }
                else {
                    String type = CoreImporter.getModronTileType(cellType);
                    ModronTile modronTile = null;
                    if (cellType != -1)
                        modronTile = CoreImporter.getModronTile(cellType);
                    if (modronTile == null && !Objects.equals(type, "Brick"))
                    {
                        Logger.Error(type);
                        Logger.Error("NULL error");
                        continue;
                    }
                    switch (type) {
                        case "ModifierCell" -> cells.get(i).add(new ModifierCell(type, modronTile.multiplier
                                , modronTile.ports, modronTile.ports_input, modronTile.tiers));
                        case "AutoCell" -> {
                            automationCell = new AutoCell(type, modronTile.multiplier
                                    , modronTile.ports, modronTile.ports_input);
                            cells.get(i).add(automationCell);
                        }
                        case "InputCell" ->{
                            inCell = new InputCell(type, modronTile.multiplier
                                    , modronTile.ports, modronTile.ports_input);
                            cells.get(i).add(inCell);
                            inCellRow = i;
                            inCellCol = j;
                        }
                        case "Pipe" ->
                            cells.get(i).add(new PipeCell(type + " " + modronTile.name , modronTile.multiplier
                                    , modronTile.ports, modronTile.ports_input, modronTile.multiInputBonus));
                        case "Brick" -> cells.get(i).add(new Cell());
                        default -> Logger.Error(type);
                    }
                }
            }
        }
    }

    public ArrayList<String> getEmptyCellLocations(int lvl){
        ArrayList<String> toReturn = new ArrayList<>();
        if (lvl == -1)
            return toReturn;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Cell temp = cells.get(i).get(j);
                if ((temp.type.equals("Empty") && temp.levelLock <= lvl) || temp instanceof Pipe){
                    toReturn.add(i + ","+ j);
                }

            }
        }
        return toReturn;
    }

    public boolean setPipe(Pipe p, int col, int row, int level){
        if ((cells.get(col).get(row) instanceof EmptyCell
                && ((EmptyCell)cells.get(col).get(row)).getLevelLock() <= level)
                || cells.get(col).get(row) instanceof Pipe)
            cells.get(col).set(row, p);
        else return false;
        return true;
    }

    private void reset(){
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                cells.get(i).get(j).timesCalled = 0;
            }
        }
    }

    private void clearBeforeFlow(){
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Cell temp = cells.get(i).get(j);
                temp.leftLink = null;
                temp.downLink = null;
                temp.rightLink = null;
                temp.upLink = null;
                temp.actualInputs = new ArrayList<>();
                temp.actualInputs.add(0);
                temp.actualInputs.add(0);
                temp.actualInputs.add(0);
                temp.actualInputs.add(0);
                if (temp instanceof ModifierCell)
                {
                    ((ModifierCell) temp).isUsed = false;
                }
                else if (temp instanceof Pipe)
                {
                    ((Pipe) temp).strength = 0.0;
                }
            }
        }
    }

    public int getOutputValue(){
        reset();
        clearBeforeFlow();
        int i;
        i = startFlow(inCell, "", inCellRow, inCellCol,0);
        reset();
        if (i == -10)
            return -666;
        automationCell.used = false;
        return getOutputValueRecursive(inCell, 1, 0);
    }
    public int getOutputValueRecursive(Cell c, double strength, int loopAvoidance){
        if (loopAvoidance> 1000){//1000
            return -666;
        }
        c.timesCalled++;
        if (c.timesCalled > 5000)  // 5 000
        {
            c.timesCalled = 0;
            return -666;
        }

        int toReturn = 1;
        if (c instanceof Pipe)
        {
            double multi = ((Pipe) c).getMultiplier(strength);
            if (multi == -1)
                return 1;
            if (multi == -2){
                strength = ((Pipe) c).getMultiInputStrength(strength);
            }
            else
                strength = Math.round(
                        strength *
                                (1 +
                                        (((Pipe) c).getMultiplier()
                                        )
                                )
                                *10000.0)/10000.0;
        }
        if (c instanceof PipeCell){
            strength = Math.round(
                    strength *
                            (1 +
                                    (((PipeCell) c).getMultiplier()
                                    )
                            )
            *10000.0)/10000.0;
        }
        if (c instanceof ModifierCell){
            if (((ModifierCell) c).isUsed)
                return 0;
            ((ModifierCell) c).isUsed = true;
            return ((ModifierCell) c).getTierValue(strength);
        }
        if (c instanceof AutoCell){
            if (!((AutoCell) c).used){
                toReturn += 1000;
                ((AutoCell) c).used = true;
            }

        }
        int splitAmount = 0;
        for (int i = 0; i < 4; i++) {
            if (c.type.contains("Directed") && c.ports.get(i) == 1 &&
                    c.ports_input.get(i) == 0 && c.actualInputs.get(i) == 0){
                splitAmount++;
            }
            else if (!c.type.contains("Directed") && c.actualInputs.get(i) == 0 && c.ports.get(i) == 1){
                splitAmount++;
            }
        }

        double tempS = Math.round((strength / splitAmount) * 10000.0) / 10000.0;
        if (c.upLink != null){
            int temp = getOutputValueRecursive(c.upLink,
                    tempS,
                    loopAvoidance+1);
            if (temp < 0)
                return temp;
            toReturn += temp;
        }
        if (c.rightLink != null){
            int temp = getOutputValueRecursive(c.rightLink,
                    tempS,
                    loopAvoidance+1);
            if (temp < 0)
                return temp;
            toReturn += temp;
        }
        if (c.downLink != null){
            int temp = getOutputValueRecursive(c.downLink, tempS,
                    loopAvoidance+1);
            if (temp < 0)
                return temp;
            toReturn += temp;
        }
        if (c.leftLink != null){
            int temp = getOutputValueRecursive(c.leftLink, tempS,
                    loopAvoidance+1);
            if (temp < 0)
                return temp;
            toReturn += temp;
        }

        return toReturn;
    }
    /*
    * 0  left
    * 1 up
    * 2 right
    * 3 down
    * */
    public int startFlow(Cell c, String comingFrom, int row, int col, int loopAvoidance){
        c.timesCalled++;
        if (loopAvoidance> 1000){//1000
            return -10;
        }
        if (c.timesCalled > 5000) //5 000
        {
            return -10;
        }
        if (c instanceof InputCell){
            for (int i = 0; i < 4; i++) {
                if (c.ports.get(i) == 1){
                    if (i == 0){
                        Cell link = cells.get(row).get(col-1);
                        if (link.ports_input == null || link.ports_input.get(2) != 1){
                            return -3;
                        }
                        c.leftLink = link;
                        link.backlinks.set(2, c);
                        return startFlow(c.leftLink, "right", row, col-1,
                                loopAvoidance+1);
                    }
                    if (i == 1){
                        Cell link = cells.get(row-1).get(col);
                        if (link.ports_input == null || link.ports_input.get(3) != 1){
                            return -3;
                        }
                        c.upLink = link;
                        link.backlinks.set(3, c);
                        return startFlow(c.upLink, "down", row-1, col,
                                loopAvoidance+1);
                    }
                    if (i == 2){
                        Cell link = cells.get(row).get(col+1);
                        if (link.ports_input == null || link.ports_input.get(0) != 1){
                            return -3;
                        }
                        c.rightLink = link;
                        link.backlinks.set(0, c);
                        return startFlow(c.rightLink, "left", row, col+1,
                                loopAvoidance+1);
                    }
                    if (i == 3){
                        Cell link = cells.get(row+1).get(col);

                        if (link.ports_input == null || link.ports_input.get(1) != 1){
                            return -3;
                        }
                        c.downLink = link;
                        link.backlinks.set(1, c);

                        return startFlow(c.downLink, "up", row+1, col,
                                loopAvoidance+1);
                    }
                }
            }

        }
        else if (c instanceof Pipe || c instanceof PipeCell || c instanceof  AutoCell){
            int dir = 0;
            switch (comingFrom) {
                case "left" -> dir = 0;
                case "up" -> dir = 1;
                case "right" -> dir = 2;
                case "down" -> dir = 3;
            }
            if (c.ports_input.get(dir) != 1){
                return -3;
            }
            else {
                c.actualInputs.set(dir,1);
            }
            int toReturn = -10;
            for (int i = 0; i < 4; i++) {
                if (c.ports.get(i) == 1 && dir != i &&
                (
                    ((c.type.contains("Undirected") || c.type.contains("AutoCell")) && c.actualInputs.get(i) != 1)
                    || (c.type.contains("Directed") && c.ports_input.get(i) != 1))
                )
                {
                    if (i == 0){
                        Cell link = cells.get(row).get(col-1);
                        if (link.ports_input == null || link.ports_input.get(2) != 1){
                            return -3;
                        }
                        if (link.hasOtherInput(2)){
                            return -3;
                        }
                        c.leftLink = link;
                        link.backlinks.set(2, c);

                        int temp =  startFlow(link, "right", row, col-1,
                                loopAvoidance+1);
                        if (temp == -10)
                            return temp;
                        if (temp > toReturn)
                            toReturn = temp;
                    }
                    if (i == 1){
                        Cell link = cells.get(row-1).get(col);
                        if (link.ports_input == null || link.ports_input.get(3) != 1){
                            return -3;
                        }
                        if (link.hasOtherInput(3)){
                            return -3;
                        }
                        c.upLink = link;
                        link.backlinks.set(3, c);

                        int temp =  startFlow(link, "down", row-1, col,
                                loopAvoidance+1);
                        if (temp == -10)
                            return temp;
                        if (temp > toReturn)
                            toReturn = temp;
                    }
                    if (i == 2){
                        Cell link = cells.get(row).get(col+1);
                        if (link.ports_input == null || link.ports_input.get(0) != 1){
                            return -3;
                        }
                        if (link.hasOtherInput(0)){
                            return -3;
                        }
                        c.rightLink = link;
                        link.backlinks.set(0, c);


                        int temp =  startFlow(link, "left", row, col+1,
                                loopAvoidance+1);
                        if (temp == -10)
                            return temp;
                        if (temp > toReturn)
                            toReturn = temp;
                    }
                    if (i == 3){
                        Cell link = cells.get(row+1).get(col);

                        if (link.ports_input == null || link.ports_input.get(1) != 1){
                            return -3;
                        }
                        if (link.hasOtherInput(1)){
                            return -3;
                        }
                        c.downLink = link;
                        link.backlinks.set(1, c);

                        int temp = startFlow(link, "up", row+1, col,
                                loopAvoidance+1);
                        if (temp == -10)
                            return temp;
                        if (temp > toReturn)
                            toReturn = temp;
                    }
                }
            }
            return toReturn;
        }
        else if (c instanceof ModifierCell)
        {
            int dir = 0;
            switch (comingFrom) {
                case "left" -> dir = 0;
                case "up" -> dir = 1;
                case "right" -> dir = 2;
                case "down" -> dir = 3;
            }
            if (c.ports_input.get(dir) != 1){
                return -2;
            }
            else return  1;
        }
        return -1;
    }
    public void printPath(){
        Logger.Info("==================================");
        reset();
        printPathRecursive(inCell, 1.0, 0);
        Logger.Info("==================================");
    }
    public void printPathRecursive(Cell c, double strength, int loopAvoidance){
        if (loopAvoidance> 2000){
            return;
        }
        c.timesCalled++;
        if (c.timesCalled > 20)
        {
            c.timesCalled = 0;
            return;
        }

        Logger.Info("Type: " + c.type);

        if (c instanceof Pipe)
        {
            strength = strength * (1 + (((Pipe) c).getMultiplier()));
            Logger.Info("Strength is now: " +String.format("%,.2f", strength ));
        }
        if (c instanceof PipeCell){
            strength = strength * (1 + (((PipeCell) c).getMultiplier()));
            Logger.Info("Strength is now: " +String.format("%,.2f", strength ));
        }
        if (c instanceof ModifierCell){
            Logger.Info("Modifier returns: " + ((ModifierCell) c).getTierValue(strength));
        }
        if (c instanceof AutoCell){
            if (!((AutoCell) c).used){
                ((AutoCell) c).used = true;
            }
        }
        int splitAmount = 0;
        if (c.upLink != null)
            splitAmount++;
        if (c.rightLink != null)
            splitAmount++;
        if (c.leftLink != null)
            splitAmount++;
        if (c.downLink != null)
            splitAmount++;

        if (c.upLink != null){
            Logger.Info("Going up");
            printPathRecursive(c.upLink, strength/splitAmount,
                    loopAvoidance+1);
        }
        if (c.rightLink != null){
            Logger.Info("Going right");
            printPathRecursive(c.rightLink, strength/splitAmount,
                    loopAvoidance+1);
        }
        if (c.downLink != null){
            Logger.Info("Going down");
            printPathRecursive(c.downLink, strength/splitAmount,
                    loopAvoidance+1);
        }
        if (c.leftLink != null){
            Logger.Info("Going left");
            printPathRecursive(c.leftLink, strength/splitAmount,
                    loopAvoidance+1);
        }
        Logger.Info("End");
    }
}
