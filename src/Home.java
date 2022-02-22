import javax.swing.*;
import java.util.Objects;

public class Home {
    private JPanel HomePanel;
    private JSpinner popSpinner;
    private JSpinner genSpinner;
    private JButton Start;
    private JProgressBar progressBar1;
    private JTextArea textArea1;
    private JTextArea textArea2;

    public Home(){
        popSpinner.setValue(300);
        genSpinner.setValue(1000);
        textArea1.append("C:\\Program Files\\Epic Games\\IdleChampions\\" +
                "IdleDragons_Data\\StreamingAssets\\downloaded_files\\webRequestLog.txt");

        Start.addActionListener(e -> {
            Runnable run = new exec_class(this);
            Thread T = new Thread(run);
            T.start();
        });

    }

    public void addProgress(){
        progressBar1.setValue(progressBar1.getValue()+1);
    }


    public void appendNewText(String txt) {
        SwingUtilities.invokeLater(() -> textArea2.setText(txt));
    }


    public static void main(String[] args) {
        if (args.length > 0 && Objects.equals(args[0], "-debug")){
            Logger.debug = true;
        }
        JFrame frame = new JFrame("Modron AI");
        frame.setContentPane(new Home().HomePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);


        /*
         * TODO
         * Needs styling fixes
         * Needs friendlier user output, output same as game?
         * Fix total modifier calculation, is this necessary?
         * Add more user inputs, mod, tournament selection, percentage for operators
         * clean up pipes not in use before exporting
         * */
    }

    class exec_class implements Runnable {
        Home home;
        public exec_class(Home h) {
            home = h;
        }

        public void run() {
            progressBar1.setValue(0);
            Start.setEnabled(false);
            appendNewText("Starting web request import");
            if (!CoreImporter.ImportWRL(textArea1.getText())) {
                progressBar1.setValue(0);
                Start.setEnabled(true);
                appendNewText("FAILED");
                return;
            }

            appendNewText("Imported web request");
            CoreImporter.getInstanceID();
            appendNewText("Found instance ID");
            CoreImporter.ImportModronTilesAndCores(Integer.parseInt(popSpinner.getValue().toString()));
            Logger.Success("Base setup done");
            appendNewText("Base setup done");
            progressBar1.setValue(50);
            CoreImporter.ImportUserDetails();
            Logger.Success("User setup done");
            appendNewText("User setup done");
            progressBar1.setValue(100);
            appendNewText("Setting up AI");
            GeneticAlgorithm ga = new GeneticAlgorithm(home);
            appendNewText("Starting AI");
            progressBar1.setValue(0);
            ga.RunLongDNAMultiCore(Integer.parseInt(popSpinner.getValue().toString()),
                    Integer.parseInt(genSpinner.getValue().toString()), CoreImporter.levels, CoreImporter.importedDna);
            progressBar1.setValue(100);
            appendNewText("Done");
            Start.setEnabled(true);

        }
    }
}
