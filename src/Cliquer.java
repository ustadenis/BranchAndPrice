
import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface Cliquer extends Library {
    /**
     * proxy function for calling C++ Ostergard Cliquer Algorithm
     * @param vnbr number of vertex for array pre-allocation
     * @param enbr number of edges in graph for pre-allocation
     * @param from array of ints with 'source' vertex ids
     * @param to array of ints with 'destination' vertex ids
     * @param weights pointer to float array of weights
     * @param size this variable will store size of clique
     * @return pointer to clique array
     */
    Pointer findClique(int vnbr, int enbr, Pointer from, Pointer to, Pointer weights, Pointer size);
}
