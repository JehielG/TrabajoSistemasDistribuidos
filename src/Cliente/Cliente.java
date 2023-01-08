package Cliente;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class Cliente {
    public static void main(String[] args){

        int k, numeroS=0;
        File f;
        File fr = new File("KmerDistance.csv");
        ArrayList<String> identificadores = new ArrayList<>();

        if(args.length == 2){
            f = new File(args[0]);
            k = Integer.parseInt(args[1]);
        }else{
            f = new File("100sq.fasta");
            k = 3;
        }

        try(Socket s = new Socket("localhost", 5757);
            BufferedWriter bfw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            BufferedReader fbfr = new BufferedReader(new FileReader(f));
            BufferedWriter fbwr = new BufferedWriter(new FileWriter(fr));
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream())){
            bfw.write(k+"\n"); //envio de k
            String linea;
            linea = fbfr.readLine();
            identificadores.add(linea);
            linea = fbfr.readLine();
           while( linea != null){
                bfw.write(linea+"\n");
                linea = fbfr.readLine();
                identificadores.add(linea);
                linea = fbfr.readLine();
                numeroS++;
           }
           bfw.write("ENDF\n");
           bfw.flush();
           double[][] resultados = (double[][]) ois.readObject(); //recepcion de resultados

            //Escritura de resultados en archivo csv
            String aux="";
            for (int i = 0; i < identificadores.size()-1; i++) {
                aux+= ","+"\""+identificadores.get(i)+"\"";
            }
            aux = aux + "\n";
            fbwr.write(aux);

            for (int i=0; i<numeroS; i++){
                aux = "\""+identificadores.get(i)+"\"";
                for (int j = 0; j < numeroS; j++) {
                    aux += ","+resultados[i][j];
                }
                aux = aux + "\n";
                fbwr.write(aux);
            }
        }catch(IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
