import java.util.Properties;
import java.util.Iterator;
import java.util.Scanner;
import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;
import src.*;

public class Main {

    // Array de 10 llavors fixes per garantir que els experiments siguin justos i repetibles
    private static final int[] LLAVORS = {1234, 2345, 3456, 4567, 5678, 6789, 7890, 8901, 9012, 123};

    public static void main(String[] args) throws Exception {
        int tipusCerca = llegirTipusCerca();

        if (tipusCerca == 1) {
            // 1. Inicialitzem l'estat base amb els valors del primer experiment de l'enunciat:
            // 5 centres de comandament, 100 grups de persones a rescatar, 1 helicòpter per centre i la llavor 1234.
            ProbBoard board = new ProbBoard(5, 100, 1, 1234);

            // COMPROVACIÓ: Imprimim per pantalla com hem repartit inicialment la gent als helicòpters.
            board.imprimirEstat();
            //System.exit(0); // Podem descomentar això si només volem veure l'estat inicial i aturar el programa.

            // 3. Creem el problema per a la llibreria d'AIMA. Li passem el nostre tauler,
            // la funció que genera els moviments (ara mateix HC), el test de meta i com calculem el temps.
            Problem p = new Problem(
                    board,
                    new ProbSuccessorFunction(), // Funció que explorarà totes les combinacions possibles
                    new ProbGoalTest(),
                    new ProbHeuristicFunction()
            );

            // 4. Preparem l'algorisme de Hill Climbing.
            Search alg = new HillClimbingSearch();

            // 5. Posem l'agent a treballar perquè busqui la millor combinació de rescats.
            System.out.println("Iniciant Cerca Local (Hill Climbing)...");
            SearchAgent agent = new SearchAgent(p, alg);

            // 6. Un cop ha acabat, mostrem quantes iteracions i passes ha fet per trobar la solució.
            System.out.println("--- ESTADÍSTIQUES DE LA CERCA ---");
            printInstrumentation(agent.getInstrumentation());
        }
        else if (tipusCerca == 2) {
            // 1. Inicialitzem l'estat base amb els valors del primer experiment de l'enunciat:
            // 5 centres de comandament, 100 grups de persones a rescatar, 1 helicòpter per centre i la llavor 1234.
            ProbBoard board = new ProbBoard(5, 100, 1, 1234);

            // COMPROVACIÓ: Imprimim per pantalla com hem repartit inicialment la gent als helicòpters.
            board.imprimirEstat();
            //System.exit(0); // Podem descomentar això si només volem veure l'estat inicial i aturar el programa.

            // 3. Creem el problema per a la llibreria d'AIMA. Li passem el nostre tauler,
            // la funció que genera els moviments (ara mateix SA), el test de meta i com calculem el temps.
            Problem p = new Problem(
                    board,
                    new ProbSuccessorFunctionSA(), // Funció que triarà un moviment a l'atzar
                    new ProbGoalTest(),
                    new ProbHeuristicFunction()
            );

            // 4. Preparem l'algorisme de Simulated Annealing (Recuita Simulada).
            // Aquests números són els paràmetres que anirem canviant per fer els experiments de la pràctica.
            Search alg = new SimulatedAnnealingSearch(10000, 100, 5, 0.001);

            // 5. Posem l'agent a treballar perquè busqui la millor combinació de rescats.
            System.out.println("Iniciant Cerca Local (Simulated Annealing)...");
            SearchAgent agent = new SearchAgent(p, alg);

            // 6. Un cop ha acabat, mostrem quantes iteracions i passes ha fet per trobar la solució.
            System.out.println("--- ESTADÍSTIQUES DE LA CERCA ---");
            printInstrumentation(agent.getInstrumentation());
        }
        else if (tipusCerca == 3) {
            runExperimento3();
        } 
        else if (tipusCerca == 4) {
            runExperimento4();
        }
    }

    private static int llegirTipusCerca() {
        Scanner readInput = new Scanner(System.in);
        System.out.println("Indica què vols executar:");
        System.out.println("    1. Hill Climbing (Una execució)");
        System.out.println("    2. Simulated Annealing (Una execució)");
        System.out.println("    3. EXPERIMENT 3: Grid Search de paràmetres SA");
        System.out.println("    4. EXPERIMENT 4: Escalabilitat HC vs SA");
        int tipusCerca = readInput.nextInt();
        while (tipusCerca != 1 && tipusCerca != 2 && tipusCerca != 3 && tipusCerca != 4) {
           System.out.println("Insereix un valor vàlid (1, 2, 3 o 4):");
           tipusCerca = readInput.nextInt();
        }
        readInput.close();
        return tipusCerca;
    }

    // --- EXPERIMENT 3: Ajust de paràmetres del SA ---
    private static void runExperimento3() throws Exception {
        System.out.println("\n--- INICIANT EXPERIMENT 3 ---");
        System.out.println("Buscant els millors paràmetres (k, lambda) per a 5 centres i 100 grups...");
        
        int iteracions = 100000; 
        int steps = 100;
        int[] valorsK = {3, 4, 5};
        double[] valorsLambda = {0.00005, 0.00002, 0.00001};

        System.out.printf("%-5s | %-8s | %-12s | %-12s | %-12s | %-12s | %-12s\n", "K", "Lambda", "T. Mig(ms)", "T. Min(ms)", "T. Max(ms)", "Nodes Migs", "Cost Mig");
        System.out.println("---------------------------------------------------------------------------------------------");

        ProbHeuristicFunction heuristic = new ProbHeuristicFunction();

        for (int k : valorsK) {
            for (double lambda : valorsLambda) {
                long tempsTotal = 0;
                double nodesTotals = 0;
                double costTotal = 0; 

                long tempsMin = Long.MAX_VALUE;
                long tempsMax = Long.MIN_VALUE;

                for (int i = 0; i < 10; i++) { 
                    ProbBoard board = new ProbBoard(5, 100, 1, LLAVORS[i]);
                    Problem p = new Problem(board, new ProbSuccessorFunctionSA(), new ProbGoalTest(), heuristic);
                    Search alg = new SimulatedAnnealingSearch(iteracions, steps, k, lambda);
                    
                    long startTime = System.currentTimeMillis();
                    SearchAgent agent = new SearchAgent(p, alg);
                    long endTime = System.currentTimeMillis();
                    
                    long tempsActual = endTime - startTime;
                    tempsTotal += tempsActual;
                    
                    if (tempsActual < tempsMin) tempsMin = tempsActual;
                    if (tempsActual > tempsMax) tempsMax = tempsActual;

                    nodesTotals += Double.parseDouble(agent.getInstrumentation().getProperty("nodesExpanded", "0"));
                    Object finalState = alg.getGoalState();
                    costTotal += heuristic.getHeuristicValue(finalState);
                }
                
                System.out.printf("%-5d | %-8.5f | %-12d | %-12d | %-12d | %-12.1f | %-12.2f\n", 
                        k, lambda, (tempsTotal / 10), tempsMin, tempsMax, (nodesTotals / 10), (costTotal / 10));
            }
        }
    }

    // --- EXPERIMENT 4: Escalabilitat ---
    /*private static void runExperimento4() throws Exception {
        System.out.println("\n--- INICIANT EXPERIMENT 4 ---");
        int millorK = 4; 
        double millorLambda = 0.00002; 

        System.out.printf("%-7s | %-5s | %-9s | %-9s | %-9s | %-9s | %-9s | %-9s | %-10s | %-10s\n", 
                "Centres", "Grups", "Mig HC", "Min HC", "Max HC", "Mig SA", "Min SA", "Max SA", "Cost HC", "Cost SA");
        System.out.println("-------------------------------------------------------------------------------------------------------");

        ProbHeuristicFunction heuristic = new ProbHeuristicFunction();

        for (int mult = 1; mult <= 5; mult++) {
            int numCentres = 5 * mult;
            int numGrups = 100 * mult;

            long tempsTotalHC = 0, tempsTotalSA = 0;
            double costTotalHC = 0, costTotalSA = 0;

            long tempsMinHC = Long.MAX_VALUE, tempsMaxHC = Long.MIN_VALUE;
            long tempsMinSA = Long.MAX_VALUE, tempsMaxSA = Long.MIN_VALUE;

            for (int i = 0; i < 10; i++) {
                // Execució Hill Climbing
                ProbBoard boardHC = new ProbBoard(numCentres, numGrups, 1, LLAVORS[i]);
                Problem pHC = new Problem(boardHC, new ProbSuccessorFunction(), new ProbGoalTest(), heuristic);
                Search algHC = new HillClimbingSearch();
                long startHC = System.currentTimeMillis();
                new SearchAgent(pHC, algHC);
                
                long tempsActualHC = System.currentTimeMillis() - startHC;
                tempsTotalHC += tempsActualHC;
                if (tempsActualHC < tempsMinHC) tempsMinHC = tempsActualHC;
                if (tempsActualHC > tempsMaxHC) tempsMaxHC = tempsActualHC;
                
                costTotalHC += heuristic.getHeuristicValue(algHC.getGoalState());

                // Execució Simulated Annealing
                ProbBoard boardSA = new ProbBoard(numCentres, numGrups, 1, LLAVORS[i]);
                Problem pSA = new Problem(boardSA, new ProbSuccessorFunctionSA(), new ProbGoalTest(), heuristic);
                Search algSA = new SimulatedAnnealingSearch(100000, 100, millorK, millorLambda);
                long startSA = System.currentTimeMillis();
                new SearchAgent(pSA, algSA);
                
                long tempsActualSA = System.currentTimeMillis() - startSA;
                tempsTotalSA += tempsActualSA;
                if (tempsActualSA < tempsMinSA) tempsMinSA = tempsActualSA;
                if (tempsActualSA > tempsMaxSA) tempsMaxSA = tempsActualSA;
                
                costTotalSA += heuristic.getHeuristicValue(algSA.getGoalState());
            }

            System.out.printf("%-7d | %-5d | %-9d | %-9d | %-9d | %-9d | %-9d | %-9d | %-10.2f | %-10.2f\n", 
                    numCentres, numGrups, (tempsTotalHC / 10), tempsMinHC, tempsMaxHC, 
                    (tempsTotalSA / 10), tempsMinSA, tempsMaxSA, (costTotalHC / 10), (costTotalSA / 10));
        }
    }*/

    // --- EXPERIMENT 4: Escalabilitat (VERSIÓ EXPRÉS NOMÉS SA 25/500) ---
    private static void runExperimento4() throws Exception {
        System.out.println("\n--- RECUPERANT DADES DEL SA PER A 25 CENTRES I 500 GRUPS ---");
        int millorK = 4; 
        double millorLambda = 0.00002; 

        System.out.printf("%-7s | %-5s | %-9s | %-9s | %-9s | %-10s\n", 
                "Centres", "Grups", "Mig SA", "Min SA", "Max SA", "Cost SA");
        System.out.println("------------------------------------------------------------------");

        ProbHeuristicFunction heuristic = new ProbHeuristicFunction();

        int numCentres = 25;
        int numGrups = 500;

        long tempsTotalSA = 0;
        double costTotalSA = 0;

        long tempsMinSA = Long.MAX_VALUE, tempsMaxSA = Long.MIN_VALUE;

        for (int i = 0; i < 10; i++) {
            // Execució Simulated Annealing NOMÉS
            ProbBoard boardSA = new ProbBoard(numCentres, numGrups, 1, LLAVORS[i]);
            Problem pSA = new Problem(boardSA, new ProbSuccessorFunctionSA(), new ProbGoalTest(), heuristic);
            Search algSA = new SimulatedAnnealingSearch(100000, 100, millorK, millorLambda);
            long startSA = System.currentTimeMillis();
            new SearchAgent(pSA, algSA);
            
            long tempsActualSA = System.currentTimeMillis() - startSA;
            tempsTotalSA += tempsActualSA;
            if (tempsActualSA < tempsMinSA) tempsMinSA = tempsActualSA;
            if (tempsActualSA > tempsMaxSA) tempsMaxSA = tempsActualSA;
            
            costTotalSA += heuristic.getHeuristicValue(algSA.getGoalState());
        }

        System.out.printf("%-7d | %-5d | %-9d | %-9d | %-9d | %-10.2f\n", 
                numCentres, numGrups, (tempsTotalSA / 10), tempsMinSA, tempsMaxSA, (costTotalSA / 10));
    }

        // Aquesta petita funció l'hem afegit per poder veure les dades ocultes (com els nodes expandits) de l'AIMA.
    private static void printInstrumentation(Properties properties) {
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String property = properties.getProperty(key);
            System.out.println(key + " : " + property);
        }
    }
}
