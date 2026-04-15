package src;

import aima.search.framework.GoalTest;

/**
 * Test de meta per a la resolució del problema mitjançant algoritmes AIMA.
 * En entorns d'optimització mitjançant cerca local, aquest test ha de retornar fals
 * en tots els casos per forçar a l'algorisme a buscar la convergència en els òptims locals.
 */
public class ProbGoalTest implements GoalTest {

    public boolean isGoalState(Object state) {
        return ((ProbBoard) state).is_goal();
    }
}