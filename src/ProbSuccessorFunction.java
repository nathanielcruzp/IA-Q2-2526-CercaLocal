package src;

import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;
import java.util.ArrayList;
import java.util.List;

/**
 * Funció de successió dissenyada per a l'algorisme de Hill Climbing.
 * Genera l'espai de veïnatge complet aplicant els operadors de cerca sobre l'estat actual.
 */
public class ProbSuccessorFunction implements SuccessorFunction {

    public List getSuccessors(Object state) {
        ArrayList<Successor> retval = new ArrayList<>();
        ProbBoard board = (ProbBoard) state;

        // Traça opcional de l'heurística de l'estat en expansió
        // ProbHeuristicFunction heuristic = new ProbHeuristicFunction();
        // System.out.println("Heuristic: " + heuristic.getHeuristicValue(board));

        // --- APLICACIÓ DE L'OPERADOR 1: MOURE GRUP ---
        // Generació exhaustiva de totes les permutacions on s'extreu un grup
        // de la seva ruta actual i s'assigna a la resta de vehicles de la flota.
        for (int i = 0; i < board.assignacioHelicopters.size(); i++) {
            for (int j = 0; j < board.assignacioHelicopters.get(i).size(); j++) {
                for (int k = 0; k < board.assignacioHelicopters.get(i).get(j).size(); k++) {

                    for (int dest = 0; dest < board.numHelicopters; dest++) {
                        if (i != dest) {
                            ProbBoard nouEstat = new ProbBoard(board);
                            nouEstat.moureGrup(i, j, k, dest);

                            String accio = "Mou grup de l'Heli " + i + " a l'Heli " + dest;
                            retval.add(new Successor(accio, nouEstat));
                        }
                    }
                }
            }
        }

        // --- APLICACIÓ DE L'OPERADOR 2: INTERCANVIAR GRUPS ---
        // Exploració de tot l'espai d'intercanvis binaris possibles entre grups de diferents vols,
        // avaluant la viabilitat a posteriori mitjançant la lògica de la classe ProbBoard.
        for (int h1 = 0; h1 < board.assignacioHelicopters.size(); h1++) {
            for (int v1 = 0; v1 < board.assignacioHelicopters.get(h1).size(); v1++) {
                for (int p1 = 0; p1 < board.assignacioHelicopters.get(h1).get(v1).size(); p1++) {

                    for (int h2 = h1; h2 < board.assignacioHelicopters.size(); h2++) {
                        for (int v2 = 0; v2 < board.assignacioHelicopters.get(h2).size(); v2++) {
                            for (int p2 = 0; p2 < board.assignacioHelicopters.get(h2).get(v2).size(); p2++) {

                                if (h1 == h2 && v1 == v2) continue;

                                ProbBoard nouEstat = new ProbBoard(board);
                                boolean valid = nouEstat.intercanviarGrups(h1, v1, p1, h2, v2, p2);

                                if (valid) {
                                    String accio = "Intercanvia grup de l'Heli " + h1 + " amb Heli " + h2;
                                    retval.add(new Successor(accio, nouEstat));
                                }
                            }
                        }
                    }
                }
            }
        }

        return retval;
    }
}
