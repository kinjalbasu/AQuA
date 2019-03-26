import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dhruv on 10/15/2017.
 */
public class Concept implements Comparable<Concept> {
    public String baseConcept;
    public String sense;
    public HashMap<String, List<Concept>> hypernymMap;
    private int rank = 0;

    public Concept(String word, String sense){
        String baseConcept = word.toLowerCase();
        baseConcept = GetSanitizedString(baseConcept);
        this.sense = sense.replaceAll("\\.", "_");
        this.baseConcept = baseConcept;
        this.hypernymMap = new HashMap<>();
    }

    public static String GetSanitizedString(String baseConcept) {
        baseConcept = baseConcept.replaceAll("-", "_");
        baseConcept = baseConcept.replaceAll("'", "");
        baseConcept = baseConcept.replaceAll("\\.", "");
        return baseConcept;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Concept concept = (Concept) o;
        return baseConcept.equals(concept.baseConcept) && sense.equals(concept.sense);
    }

    @Override
    public int hashCode() {
        return baseConcept.hashCode();
    }

    public void SetHypernym(String sense, Concept childConcept) {
        List<Concept> hypernymList = new ArrayList<>();
        if(this.hypernymMap.containsKey(sense)){
            hypernymList = this.hypernymMap.get(sense);
        }

        hypernymList.add(childConcept);
        this.hypernymMap.put(sense, hypernymList);
    }

    public void SetRank(int rank){
        this.rank = rank;
    }

    @Override
    public int compareTo(Concept otherConcept) {
        return Integer.compare(this.rank, otherConcept.rank);
    }
}
