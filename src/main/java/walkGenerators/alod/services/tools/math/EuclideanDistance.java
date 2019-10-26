package walkGenerators.alod.services.tools.math;

public class EuclideanDistance implements VectorDistance {
    @Override
    public double calculateDistance(double[] v1, double[] v2) {
        return MathOperations.calculateEuclideanDistance(v1, v2);
    }
}
