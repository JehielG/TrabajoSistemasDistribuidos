package Servidor;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.*;


class getFrequencyTable implements Callable<HashMap<String, Integer>>{
    String sequence;
    int k;
    public getFrequencyTable(String sq, int k){
        this.sequence = sq;
        this.k = k;
    }

    public HashMap<String, Integer> call(){
        HashMap<String, Integer> frequencyMap = new HashMap<>();
        int n = this.sequence.length();

        String pattern;
        for(int i=0; i <= n - k; i++) {
            pattern = sequence.substring(i, i+k);
            frequencyMap.put(pattern, frequencyMap.getOrDefault(pattern,0) + 1);
        }
        return frequencyMap;
    }
}

class kmerCounting implements Runnable{
    double[][] resultados;
    HashMap<String, Integer> frequencyTableA;
    HashMap<String, Integer> frequencyTableB;
    int lA, lB, i, j, k;
    CountDownLatch cdl;

    public kmerCounting(HashMap<String, Integer> ftA, HashMap<String, Integer> ftB, int la, int lb,
                        double[][] matriz, int i, int j, int k, CountDownLatch cd){
        this.frequencyTableA = ftA;
        this.frequencyTableB = ftB;
        this.lA = la;
        this.lB = lb;
        this.resultados = matriz;
        this.i = i;
        this.j = j;
        this.k = k;
        this.cdl = cd;
    }

    public void run(){
        double denominador = (Math.min(lA, lB) - k + 1);
        double f = 0;
        String pattern;

        for (Map.Entry entry : frequencyTableA.entrySet()) {
            pattern = (String) entry.getKey();
            f += (Math.min(frequencyTableA.get(pattern), frequencyTableB.getOrDefault(pattern, 0)))/denominador;
        }

        resultados[i][j] = Math.log10(0.1 + f);
        this.cdl.countDown();
    }
}

class AtenderPeticion implements Runnable{
    Socket s;
    ExecutorService poolAP = Executors.newCachedThreadPool();
    AtenderPeticion(Socket s){
        this.s = s;
    }
    public void run() {
        try(BufferedReader bfr = new BufferedReader(new InputStreamReader(s.getInputStream()));
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
        BufferedWriter bfw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))){

            String linea;
            ArrayList<String> secuencias = new ArrayList<>();
            ArrayList<Callable<HashMap<String,Integer>>> tareas = new ArrayList<>();

            linea = bfr.readLine();
            int k = Integer.parseInt(linea);
            linea = bfr.readLine();

            while(linea.compareTo("ENDF") != 0){
                tareas.add(new getFrequencyTable(linea, k));
                secuencias.add(linea);
                linea = bfr.readLine();
            }
            ArrayList<Future<HashMap<String, Integer>>> frequencyTables = (ArrayList<Future<HashMap<String, Integer>>>) poolAP.invokeAll(tareas);

            int numeroS = secuencias.size();
            final CountDownLatch cdl = new CountDownLatch(((numeroS*(numeroS-1))/2));
            double[][] matrizP = new double[numeroS][numeroS];

            for (int i=0; i<numeroS; i++){
                for (int j = i+1; j < numeroS; j++) {
                  poolAP.execute(new kmerCounting(frequencyTables.get(i).get(), frequencyTables.get(j).get(), secuencias.get(i).length(),
                          secuencias.get(j).length(), matrizP, i, j, k, cdl));
                }
            }

            cdl.await(); //Esperar al calculo de todas las distancias.
            System.out.println("Calculo completado enviado resultados...");
            oos.writeObject(matrizP); //envio de matriz de distancias
            bfw.flush();
            poolAP.shutdown();
        }catch (IOException e) {
        throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
public class Servidor {
    public static void main(String[] args){
        ExecutorService pool = Executors.newCachedThreadPool();
        try(ServerSocket ss = new ServerSocket(5757)){
            while(true){
                try{
                    Socket s = ss.accept();
                    pool.execute(new AtenderPeticion(s));
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
