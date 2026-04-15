import java.util.Properties;
import java.util.Iterator;
import java.util.Scanner;
import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;
import src.*;

/**
 * Classe principal que actua com a punt d'entrada del programa.
 * Configura l'estat inicial, defineix el problema de cerca i llança els algorismes d'optimització.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // 1. Inicialització de l'escenari base (5 centres, 100 grups, 1 helicòpter/centre, llavor 1234)
        ProbBoard board = new ProbBoard(5, 100, 1, 1234);

        // Imprimeix l'estat inicial per verificar la correcta assignació de rutes abans de l'optimització
        board.imprimirEstat();

        // 2. Selecció de l'algorisme de Cerca Local
        int tipusCerca = llegirTipusCerca();

        if (tipusCerca == 1) {
            // 3. Configuració del problema per a Hill Climbing amb exploració completa del veïnatge
            Problem p = new Problem(
                    board,
                    new ProbSuccessorFunction(),
                    new ProbGoalTest(),
                    new ProbHeuristicFunction()
            );

            Search alg = new HillClimbingSearch();

            System.out.println("Iniciant Cerca Local (Hill Climbing)...");
            SearchAgent agent = new SearchAgent(p, alg);

            System.out.println("--- ESTADÍSTIQUES DE LA CERCA ---");
            printInstrumentation(agent.getInstrumentation());
        }

        if (tipusCerca == 2) {
            // 3. Configuració del problema per a Simulated Annealing amb generació d'un successor aleatori
            Problem p = new Problem(
                    board,
                    new ProbSuccessorFunctionSA(),
                    new ProbGoalTest(),
                    new ProbHeuristicFunction()
            );

            // 4. Paràmetres del Simulated Annealing: iteracions, passos per temperatura, k, lambda
            Search alg = new SimulatedAnnealingSearch(10000, 100, 5, 0.001);

            System.out.println("Iniciant Cerca Local (Simulated Annealing)...");
            SearchAgent agent = new SearchAgent(p, alg);

            System.out.println("--- ESTADÍSTIQUES DE LA CERCA ---");
            printInstrumentation(agent.getInstrumentation());
        }
    }

    /**
     * Llegeix per entrada estàndard quin algorisme de cerca desitja executar l'usuari.
     * @return 1 per Hill Climbing, 2 per Simulated Annealing.
     */
    private static int llegirTipusCerca() {
        Scanner readInput = new Scanner(System.in);
        System.out.println("Indica el tipus de cerca a utilitzar:");
        System.out.println("    1. Hill Climbing");
        System.out.println("    2. Simulated Annealing");
        int tipusCerca = readInput.nextInt();
        while (tipusCerca != 1 && tipusCerca != 2) {
            System.out.println("Insereix un valor vàlid (1 o 2):");
            tipusCerca = readInput.nextInt();
        }
        readInput.close();
        return tipusCerca;
    }

    /**
     * Mètode auxiliar per imprimir les estadístiques internes de l'agent de cerca de la llibreria AIMA,
     * incloent-hi els nodes expandits o el temps de CPU consumit.
     * @param properties Propietats instrumentals de l'agent.
     */
    private static void printInstrumentation(Properties properties) {
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String property = properties.getProperty(key);
            System.out.println(key + " : " + property);
        }
    }
}
