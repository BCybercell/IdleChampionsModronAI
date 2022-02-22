public class EmptyCell extends Cell{
    public int levelLock;
    public EmptyCell(int lvl){
        super("Empty", lvl);
        levelLock = lvl;
    }

    public int getLevelLock(){
        return levelLock;
    }
}
