package src;

import IA.Desastres.*;
import java.util.ArrayList;

/**
 * Representació de l'estat del problema.
 * Gestiona les dades estàtiques de l'escenari i l'estructura de dades dinàmica
 * de l'assignació de grups a les rutes dels helicòpters.
 */
public class ProbBoard {

    // --- DADES ESTÀTIQUES ---
    // S'emmagatzemen com a variables de classe (static) per compartir la instància
    // entre tots els estats generats i evitar redundància espacial a la memòria.
    public static Grupos totsElsGrups;
    public static Centros totsElsCentres;
    public static int numHelicopters;

    // --- DADES DINÀMIQUES ---
    // Llista niuada per representar l'estat factible:
    // Helicòpter -> Llista de Viatges -> Llista d'IDs de Grups assignats a aquell viatge.
    public ArrayList<ArrayList<ArrayList<Integer>>> assignacioHelicopters;

    /**
     * Constructor inicial: Genera el problema base utilitzant la llibreria IA.Desastres.
     * @param numCentres Nombre de centres de comandament.
     * @param numGrups Nombre de grups de persones a rescatar.
     * @param nHelicoptersPerCentre Nombre d'helicòpters disponibles a cada centre.
     * @param seed Llavor per garantir la reproductibilitat de l'experiment.
     */
    public ProbBoard(int numCentres, int numGrups, int nHelicoptersPerCentre, int seed) {
        totsElsCentres = new Centros(numCentres, nHelicoptersPerCentre, seed);
        totsElsGrups = new Grupos(numGrups, seed);
        numHelicopters = numCentres * nHelicoptersPerCentre;

        assignacioHelicopters = new ArrayList<>();
        for (int i = 0; i < numHelicopters; i++) {
            assignacioHelicopters.add(new ArrayList<>());
        }

        // Inicialització de l'estat factible inicial (canviar estratègia segons l'experiment).
        generarSolucioAleatoria();
        // generarSolucioAvariciosa();
    }

    /**
     * Constructor còpia: Realitza una clonació profunda (deep copy) de l'estructura dinàmica.
     * Fonamental per avaluar successors sense mutar l'estat origen.
     * @param estatAnterior L'estat des del qual es vol derivar el nou estat.
     */
    public ProbBoard(ProbBoard estatAnterior) {
        this.assignacioHelicopters = new ArrayList<>();
        for (ArrayList<ArrayList<Integer>> viatgesHeli : estatAnterior.assignacioHelicopters) {
            ArrayList<ArrayList<Integer>> viatgesClonats = new ArrayList<>();
            for (ArrayList<Integer> viatge : viatgesHeli) {
                viatgesClonats.add(new ArrayList<>(viatge));
            }
            this.assignacioHelicopters.add(viatgesClonats);
        }
    }

    /**
     * Estratègia d'inicialització (Baseline): Genera un estat inicial assignant de manera
     * aleatòria cada grup a un helicòpter, respectant estrictament les restriccions de capacitat.
     */
    private void generarSolucioAleatoria() {
        java.util.Random rand = new java.util.Random();

        for (int idGrup = 0; idGrup < totsElsGrups.size(); idGrup++) {
            int personesDelGrup = totsElsGrups.get(idGrup).getNPersonas();
            int idHeli = rand.nextInt(numHelicopters);

            ArrayList<ArrayList<Integer>> viatgesHeli = assignacioHelicopters.get(idHeli);
            if (viatgesHeli.isEmpty()) {
                viatgesHeli.add(new ArrayList<>());
            }

            ArrayList<Integer> ultimViatge = viatgesHeli.get(viatgesHeli.size() - 1);

            int personesAlViatge = 0;
            for (int idGrupViatge : ultimViatge) {
                personesAlViatge += totsElsGrups.get(idGrupViatge).getNPersonas();
            }

            if (ultimViatge.size() < 3 && (personesAlViatge + personesDelGrup) <= 15) {
                ultimViatge.add(idGrup);
            } else {
                ArrayList<Integer> nouViatge = new ArrayList<>();
                nouViatge.add(idGrup);
                viatgesHeli.add(nouViatge);
            }
        }
    }

    /**
     * Estratègia d'inicialització Informada (Greedy): Genera un estat inicial agrupant
     * geogràficament els rescats, assignant cada grup a un helicòpter del centre més proper.
     */
    private void generarSolucioAvariciosa() {
        int helisPerCentre = numHelicopters / totsElsCentres.size();
        java.util.Random rand = new java.util.Random();

        for (int idGrup = 0; idGrup < totsElsGrups.size(); idGrup++) {
            Grupo grup = totsElsGrups.get(idGrup);
            int personesDelGrup = grup.getNPersonas();

            int idCentreMesProper = 0;
            double distanciaMinima = Double.MAX_VALUE;

            for (int idCentre = 0; idCentre < totsElsCentres.size(); idCentre++) {
                Centro centre = totsElsCentres.get(idCentre);
                double dx = grup.getCoordX() - centre.getCoordX();
                double dy = grup.getCoordY() - centre.getCoordY();
                double distancia = Math.sqrt(dx * dx + dy * dy);

                if (distancia < distanciaMinima) {
                    distanciaMinima = distancia;
                    idCentreMesProper = idCentre;
                }
            }

            int idHeli = (idCentreMesProper * helisPerCentre) + rand.nextInt(helisPerCentre);
            ArrayList<ArrayList<Integer>> viatgesHeli = assignacioHelicopters.get(idHeli);

            if (viatgesHeli.isEmpty()) {
                viatgesHeli.add(new ArrayList<>());
            }

            ArrayList<Integer> ultimViatge = viatgesHeli.get(viatgesHeli.size() - 1);
            int personesAlViatge = 0;
            for (int idGrupViatge : ultimViatge) {
                personesAlViatge += totsElsGrups.get(idGrupViatge).getNPersonas();
            }

            if (ultimViatge.size() < 3 && (personesAlViatge + personesDelGrup) <= 15) {
                ultimViatge.add(idGrup);
            } else {
                ArrayList<Integer> nouViatge = new ArrayList<>();
                nouViatge.add(idGrup);
                viatgesHeli.add(nouViatge);
            }
        }
    }

    /**
     * Funció heurística 1: Calcula el temps total acumulat de vol i operació de tota la flota,
     * incloent-hi distàncies de vol, temps d'embarcament i penalitzacions per viatges consecutius.
     * @return El cost total de l'estat en minuts.
     */
    public double heuristic() {
        double tempsTotalFlota = 0.0;
        int helisPerCentre = numHelicopters / totsElsCentres.size();

        for (int i = 0; i < assignacioHelicopters.size(); i++) {
            int idCentre = i / helisPerCentre;
            Centro centre = totsElsCentres.get(idCentre);

            double coordX_Centre = centre.getCoordX();
            double coordY_Centre = centre.getCoordY();

            ArrayList<ArrayList<Integer>> viatges = assignacioHelicopters.get(i);

            for (int v = 0; v < viatges.size(); v++) {
                ArrayList<Integer> viatge = viatges.get(v);
                if (viatge.isEmpty()) continue;

                // Penalització per múltiples viatges del mateix helicòpter
                if (v > 0) tempsTotalFlota += 10.0;

                double posActualX = coordX_Centre;
                double posActualY = coordY_Centre;

                for (int g = 0; g < viatge.size(); g++) {
                    int idGrup = viatge.get(g);
                    Grupo grup = totsElsGrups.get(idGrup);

                    double dx = grup.getCoordX() - posActualX;
                    double dy = grup.getCoordY() - posActualY;
                    double distancia = Math.sqrt(dx * dx + dy * dy);

                    tempsTotalFlota += (distancia / 100.0) * 60.0;

                    int persones = grup.getNPersonas();
                    int prioritat = grup.getPrioridad();

                    if (prioritat == 1) {
                        tempsTotalFlota += (persones * 2.0);
                    } else {
                        tempsTotalFlota += (persones * 1.0);
                    }

                    posActualX = grup.getCoordX();
                    posActualY = grup.getCoordY();
                }

                double dxTornada = coordX_Centre - posActualX;
                double dyTornada = coordY_Centre - posActualY;
                double distanciaTornada = Math.sqrt(dxTornada * dxTornada + dyTornada * dyTornada);

                tempsTotalFlota += (distanciaTornada / 100.0) * 60.0;
            }
        }
        return tempsTotalFlota;
    }

    /**
     * Funció heurística 2: Calcula el temps requerit per realitzar el darrer rescat
     * d'un grup marcat amb prioritat alta (ferits).
     * @return El cost de l'estat en minuts basat en l'eficiència del rescat de ferits.
     */
    public double heuristic2() {
        int helisPerCentre = numHelicopters / totsElsCentres.size();
        double tempsUltimP1 = 0.0;

        for (int i = 0; i < assignacioHelicopters.size(); i++) {
            int idCentre = i / helisPerCentre;
            Centro centre = totsElsCentres.get(idCentre);

            double coordX_Centre = centre.getCoordX();
            double coordY_Centre = centre.getCoordY();

            ArrayList<ArrayList<Integer>> viatges = assignacioHelicopters.get(i);

            double tempsDesdeInici = 0.0;
            double tempsUltimP1Heli = 0.0;

            for (int v = 0; v < viatges.size(); v++) {
                ArrayList<Integer> viatge = viatges.get(v);
                if (viatge.isEmpty()) continue;

                if (v > 0) tempsDesdeInici += 10.0;

                double posActualX = coordX_Centre;
                double posActualY = coordY_Centre;
                boolean P1Present = false;

                for (int g = 0; g < viatge.size(); g++) {
                    int idGrup = viatge.get(g);
                    Grupo grup = totsElsGrups.get(idGrup);

                    double dx = grup.getCoordX() - posActualX;
                    double dy = grup.getCoordY() - posActualY;
                    double distancia = Math.sqrt(dx * dx + dy * dy);

                    tempsDesdeInici += (distancia / 100.0) * 60.0;

                    int persones = grup.getNPersonas();
                    int prioritat = grup.getPrioridad();

                    if (prioritat == 1) {
                        tempsDesdeInici += (persones * 2.0);
                        P1Present = true;
                    } else {
                        tempsDesdeInici += (persones * 1.0);
                    }

                    posActualX = grup.getCoordX();
                    posActualY = grup.getCoordY();
                }

                double dxTornada = coordX_Centre - posActualX;
                double dyTornada = coordY_Centre - posActualY;
                double distanciaTornada = Math.sqrt(dxTornada * dxTornada + dyTornada * dyTornada);

                tempsDesdeInici += (distanciaTornada / 100.0) * 60.0;
                if (P1Present) tempsUltimP1Heli = tempsDesdeInici;
            }
            if (tempsUltimP1 < tempsUltimP1Heli) tempsUltimP1 = tempsUltimP1Heli;
        }
        return tempsUltimP1;
    }

    public double sumaHeuristics() {
        return heuristic() + heuristic2();
    }

    public double ponderacioHeuristics() {
        // El valor de la ponderació pot ser canviat. El valor 64 és òptim
        // per minimitzar al màxim el temps de rescat dels grups de prioritat 1
        return heuristic() + 64 * heuristic2();
    }

    /**
     * Mètode exigit per la llibreria AIMA per verificar si un estat és final.
     * Atès que es tracta d'un problema d'optimització (Cerca Local), no existeix
     * un estat meta absolut i, per tant, sempre retorna fals.
     * @return false.
     */
    public boolean is_goal() {
        return false;
    }

    /**
     * Operador 1: "Moure Grup". Reassigna un grup extret d'una posició i un viatge concrets
     * i l'insereix al darrer viatge disponible de l'helicòpter de destí. Si la restricció
     * de capacitat no ho permet, genera un viatge nou de forma automàtica.
     */
    public void moureGrup(int idHeliOrigen, int numViatgeOrigen, int posGrup, int idHeliDesti) {
        ArrayList<Integer> viatgeOrigen = assignacioHelicopters.get(idHeliOrigen).get(numViatgeOrigen);
        int idGrup = viatgeOrigen.get(posGrup);
        int personesDelGrup = totsElsGrups.get(idGrup).getNPersonas();

        viatgeOrigen.remove(posGrup);

        if (viatgeOrigen.isEmpty()) {
            assignacioHelicopters.get(idHeliOrigen).remove(numViatgeOrigen);
        }

        ArrayList<ArrayList<Integer>> viatgesDesti = assignacioHelicopters.get(idHeliDesti);
        if (viatgesDesti.isEmpty()) {
            viatgesDesti.add(new ArrayList<>());
        }

        ArrayList<Integer> ultimViatgeDesti = viatgesDesti.get(viatgesDesti.size() - 1);
        int personesAlViatge = 0;
        for (int grupViatge : ultimViatgeDesti) {
            personesAlViatge += totsElsGrups.get(grupViatge).getNPersonas();
        }

        if (ultimViatgeDesti.size() < 3 && (personesAlViatge + personesDelGrup) <= 15) {
            ultimViatgeDesti.add(idGrup);
        } else {
            ArrayList<Integer> nouViatge = new ArrayList<>();
            nouViatge.add(idGrup);
            viatgesDesti.add(nouViatge);
        }
    }

    /**
     * Operador 2: "Intercanviar Grups". Avalua la viabilitat física d'un intercanvi directe
     * de dos grups entre diferents vols.
     * @return Cert si l'intercanvi manté ambdós vols dins de les restriccions de capacitat, fals altrament.
     */
    public boolean intercanviarGrups(int h1, int v1, int p1, int h2, int v2, int p2) {
        ArrayList<Integer> viatge1 = assignacioHelicopters.get(h1).get(v1);
        ArrayList<Integer> viatge2 = assignacioHelicopters.get(h2).get(v2);

        int idGrup1 = viatge1.get(p1);
        int idGrup2 = viatge2.get(p2);

        int persones1 = totsElsGrups.get(idGrup1).getNPersonas();
        int persones2 = totsElsGrups.get(idGrup2).getNPersonas();

        int capacitatActualViatge1 = 0;
        for (int g : viatge1) capacitatActualViatge1 += totsElsGrups.get(g).getNPersonas();

        int capacitatActualViatge2 = 0;
        for (int g : viatge2) capacitatActualViatge2 += totsElsGrups.get(g).getNPersonas();

        int novaCapacitat1 = capacitatActualViatge1 - persones1 + persones2;
        int novaCapacitat2 = capacitatActualViatge2 - persones2 + persones1;

        if (novaCapacitat1 <= 15 && novaCapacitat2 <= 15) {
            viatge1.set(p1, idGrup2);
            viatge2.set(p2, idGrup1);
            return true;
        }

        return false;
    }

    /**
     * Mètode de traça: Imprimeix per consola l'estructura de rutes i assignacions de tota la flota
     * per tal de monitoritzar l'estat actual en fase de desenvolupament.
     */
    public void imprimirEstat() {
        System.out.println("--- ESTAT ACTUAL DE LA FLOTA ---");
        for (int i = 0; i < assignacioHelicopters.size(); i++) {
            System.out.println("Helicòpter " + i + ":");
            ArrayList<ArrayList<Integer>> viatges = assignacioHelicopters.get(i);
            if (viatges.isEmpty()) {
                System.out.println("  Sense viatges assignats.");
            } else {
                for (int v = 0; v < viatges.size(); v++) {
                    System.out.println("  Viatge " + (v+1) + ": Grups " + viatges.get(v));
                }
            }
        }
        System.out.println("--------------------------------");
    }
}
