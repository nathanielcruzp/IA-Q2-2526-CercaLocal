package src;

import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Funció de successió específica per a l'algorisme de Simulated Annealing (Recuita Simulada).
 * A diferència de Hill Climbing, aquest mètode retorna un únic successor generat a l'atzar.
 */
public class ProbSuccessorFunctionSA implements SuccessorFunction {

    public List getSuccessors(Object state) {
        ArrayList<Successor> retval = new ArrayList<>();
        ProbBoard board = (ProbBoard) state;
        Random rand = new Random();

        ProbBoard nouEstat = new ProbBoard(board);
        String accio = "";

        // Selecció aleatòria de l'operador que definirà el següent estat veí.
        int tipusOperador = rand.nextInt(2);

        if (tipusOperador == 0) {
            // --- APLICACIÓ ALEATÒRIA DE L'OPERADOR: MOURE GRUP ---
            int heliOrigen, viatgeOrigen, posGrup, heliDesti;

            do {
                heliOrigen = rand.nextInt(board.assignacioHelicopters.size());
            } while (board.assignacioHelicopters.get(heliOrigen).isEmpty());

            viatgeOrigen = rand.nextInt(board.assignacioHelicopters.get(heliOrigen).size());
            posGrup = rand.nextInt(board.assignacioHelicopters.get(heliOrigen).get(viatgeOrigen).size());

            do {
                heliDesti = rand.nextInt(board.numHelicopters);
            } while (heliDesti == heliOrigen);

            nouEstat.moureGrup(heliOrigen, viatgeOrigen, posGrup, heliDesti);
            accio = "SA: Mou grup de l'Heli " + heliOrigen + " a l'Heli " + heliDesti;

        } else {
            // --- APLICACIÓ ALEATÒRIA DE L'OPERADOR: INTERCANVIAR GRUPS ---
            int h1 = 0, v1 = 0, p1 = 0, h2 = 0, v2 = 0, p2 = 0;
            boolean valid = false;

            // S'imposa un límit d'intents heurístic per evitar caure en bucles infinits
            // en cas que el tauler presenti una assignació on l'intercanvi sigui sistemàticament inviable.
            int intents = 0;
            while (!valid && intents < 10) {
                do { h1 = rand.nextInt(board.assignacioHelicopters.size()); } while (board.assignacioHelicopters.get(h1).isEmpty());
                v1 = rand.nextInt(board.assignacioHelicopters.get(h1).size());
                p1 = rand.nextInt(board.assignacioHelicopters.get(h1).get(v1).size());

                do { h2 = rand.nextInt(board.assignacioHelicopters.size()); } while (board.assignacioHelicopters.get(h2).isEmpty());
                v2 = rand.nextInt(board.assignacioHelicopters.get(h2).size());
                p2 = rand.nextInt(board.assignacioHelicopters.get(h2).get(v2).size());

                if (h1 == h2 && v1 == v2) continue;

                valid = nouEstat.intercanviarGrups(h1, v1, p1, h2, v2, p2);
                intents++;
            }

            if (valid) {
                accio = "SA: Intercanvi Heli " + h1 + " amb Heli " + h2;
            } else {
                accio = "SA: Intercanvi fallit";
            }
        }

        // Retorna la col·lecció instrumental amb un únic element requerida per l'AIMA.
        retval.add(new Successor(accio, nouEstat));
        return retval;
    }
}