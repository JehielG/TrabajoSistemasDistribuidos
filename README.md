## Servidor de cómputo con aplicaciones bioinformáticas (Calculo de distancia evolutiva basada en k-meros)

La aplicación cliente recibe como parámetros el nombre del archivo (.fasta) y el valor de k
Por ejemplo: 2000sq.fasta 4
En caso de que no se reciba ningún parámetro, la aplicación cliente tiene asignado por defecto el archivo 100sq.fasta y un valor de k=3, con el objetivo de demostrar la funcionalidad de la aplicación.
Para visualizar los resultados se crea un archivo .csv, que contiene la matriz de distancias evolutivas de las secuencias proporcionadas.
La distancia evolutiva es calculada utilizando recuento fraccionario común de k-mer, la distancia se calcula en función del recuento mínimo de cada k-mer en las dos secuencias, por lo que si dos secuencias son muy diferentes, los mínimos serán pequeños. La fórmula es la siguiente:

$$ \displaystyle \textrm{dist}(s_1,s_2) = \log(0.1 + \sum_i(\min(p(s_1)_i,p(s_2)_i)/(\min(n,m)-k+1)).$$

Aquí $n$ es la longitud de $s_1$ y $m$ es la longitud de $S_2$.
Este método se ha descrito en [Edgar, 2004]. 
