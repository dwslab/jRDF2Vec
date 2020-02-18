package original;

public class OrigWalkGeneration {

    public static void main(String[] args) {
        System.out.println(generateQuery(8, 10));
    }

    /**
     * generates the query with the given depth
     *
     * @param depth -
     * @param numberWalks -
     * @return -
     */
    public static String generateQuery(int depth, int numberWalks) {
        String selectPart = "SELECT ?p ?o1";
        String mainPart = "{ $ENTITY$ ?p ?o1  ";
        String query = "";
        int lastO = 1;
        for (int i = 1; i < depth; i++) {
            mainPart += ". ?o" + i + " ?p" + i + "?o" + (i + 1);
            selectPart += " ?p" + i + "?o" + (i + 1);
            lastO = i + 1;
        }
        String lastOS = "?o" + lastO;
        query = selectPart + " WHERE " + mainPart + " . FILTER(!isLiteral("
                + lastOS
                + ")). BIND(RAND() AS ?sortKey) } ORDER BY ?sortKey LIMIT "
                + numberWalks;
        return query;
    }


}
