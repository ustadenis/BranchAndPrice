
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.util.List;

public class CliquerWrapper {
    private static Cliquer cliquer;

    private static CliquerWrapper instance = new CliquerWrapper();

    public static CliquerWrapper getInstance() {
        return instance;
    }

    private CliquerWrapper() {
    	String myLibraryPath = System.getProperty("user.dir");//or another absolute or relative path
    	System.setProperty("java.library.path", myLibraryPath);
        cliquer = (Cliquer) Native.loadLibrary("Cliquerx64", Cliquer.class);
    }

    /**
     * proxy function for calling C++ Ostergard Cliquer Algorithm.
     * @param from array of ints with 'source' vertex ids.
     * @param to array of ints with 'destination' vertex ids.
     * @param weights array of floats
     *
     * @return int array with max clique.
     *
     * NOTE: from.length must be equal to.length
     */
    public int[] findClique (int[] from, int[] to, float[] weights) {
        if (from.length != to.length) {
            throw new IllegalArgumentException(
                    String.format("Array dimensions must be equal: from.length = %1$s, to.length = %2$s",
                            from.length,
                            to.length
                    )
            );
        }
        Pointer cliqueSize = new Memory(Native.getNativeSize(Integer.TYPE));

        Pointer fromPointer = new Memory(from.length * Native.getNativeSize(Integer.TYPE));
        Pointer toPointer = new Memory(to.length * Native.getNativeSize(Integer.TYPE));
        for (int i = 0; i < from.length; i++) {
            fromPointer.setInt(i * Native.getNativeSize(Integer.TYPE), from[i]);
            toPointer.setInt(i * Native.getNativeSize(Integer.TYPE), to[i]);
        }

        Pointer weightsPointer = new Memory(weights.length * Native.getNativeSize(Integer.TYPE));
        for (int i = 0; i < weights.length; i++) {
            weightsPointer.setFloat(i * Native.getNativeSize(Float.TYPE), weights[i]);
        }

        Pointer p = cliquer.findClique(weights.length, from.length, fromPointer, toPointer, weightsPointer, cliqueSize);

        int size = cliqueSize.getInt(0);
        return p.getIntArray(0, size);
    }

    /**
     * another interface for calling C++ Ostergard
     * @param edges list of edges
     * @param weights array of weights
     *                weight length should be equal to number of vertex.
     * @return int array with max clique.
     */
    public int[] findClique (List<Edge> edges, float[] weights) {
        int[] from = new int[edges.size()];
        int[] to = new int[edges.size()];
        for (int i = 0; i < edges.size(); i++) {
            from[i] = edges.get(i).getX();
            to[i] = edges.get(i).getY();
        }
        return findClique(from, to, weights);
    }

    /**
     * another interface for calling C++ Ostergard
     * @param edges array of edges
     * @param weights array of weights
     *                weight length should be equal to number of vertex.
     * @return int array with max clique.
     */
    public int[] findClique (Edge[] edges, float[] weights) {
        int[] from = new int[edges.length];
        int[] to = new int[edges.length];
        for (int i = 0; i < edges.length; i++) {
            from[i] = edges[i].getX();
            to[i] = edges[i].getY();
        }
        return findClique(from, to, weights);
    }
}
