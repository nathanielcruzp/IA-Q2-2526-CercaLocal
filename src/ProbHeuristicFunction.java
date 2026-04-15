package src;

import aima.search.framework.HeuristicFunction;

/**
 * Encapsulació de la funció d'avaluació heurística per integrar-se a l'arquitectura d'AIMA.
 * Interroga l'estat actual per obtenir el cost numèric que s'intenta minimitzar en la cerca.
 */
public class ProbHeuristicFunction implements HeuristicFunction {

    public double getHeuristicValue(Object n) {
        // --- CONFIGURACIÓ PER DEFECTE (Experiments 1 al 6) ---
        // Utilitza l'heurística 1 pura: minimitzar el temps total de vol de la flota.
        return ((ProbBoard) n).heuristic();

        // --- CONFIGURACIÓ PER A L'EXPERIMENT 7 ---
        // Per avaluar l'Experiment 7 (priorització de ferits),
        // cal comentar la línia superior i descomentar la línia inferior per activar
        // la funció amb suma ponderada (modificant els pesos a dins de ProbBoard.java).
        //
        // return ((ProbBoard) n).ponderacioHeuristics();
    }
}
