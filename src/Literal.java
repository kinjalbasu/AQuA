import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dhruv on 9/24/2017.
 */
public class Literal implements Comparable<Literal> {
    private String predicate;
    private List<Literal> terms;
    private boolean isAtom;
    public boolean isNAF = false;
    public boolean isClassicalNegation = false;
    public Set<LiteralType> types = new HashSet<>();

    public Literal(Word predicate, List<Literal> terms){
        String predicateString = predicate.getLemma();
        predicateString = predicateString.replaceAll("-", "_");
        predicateString = predicateString.replaceAll("'", "");
        this.predicate = predicateString;
        this.terms = terms;
        this.isAtom = false;

        for(Literal term : terms){
            this.types.addAll(term.types);
        }
    }

    public Literal(Word atom){
        this.predicate = atom.getLemma();
        if(!atom.isVariable && IsMixedCase(this.predicate) || this.predicate.contains(".") ||
            this.predicate.contains(",") || this.predicate.contains(" ") || IsSASPKeyword(this.predicate)){
            this.predicate = String.format("'%s'", this.predicate);
        }
        this.isAtom = true;

        if(atom.isVariable && atom.getWord().startsWith("X")){
            types.add(LiteralType.ANSWER_QUERY);
        }
        else if(atom.isVariable){
            types.add(LiteralType.CONSTRAINT_QUERY);
        }
        else {
            types.add(LiteralType.FACT);
        }
    }

    public Literal(Word atom, LiteralType type){
        this.predicate = atom.getLemma();
        if(!atom.isVariable && IsMixedCase(this.predicate) || this.predicate.contains(".") ||
                this.predicate.contains(",") || IsSASPKeyword(this.predicate)){
            this.predicate = String.format("'%s'", this.predicate);
        }
        this.isAtom = true;
        types.add(type);
    }

    private boolean IsSASPKeyword(String predicate) {
        if(predicate.equals("include")) return true;
        if(predicate.equals("compare")) return true;
        return false;
    }

    @Override
    public String toString() {
        if(this.isAtom) {
            this.predicate = this.predicate.replaceAll("-", "_");
            return this.predicate;
        }

        if(terms.size() == 0){
            return "Error : No terms found in body";
        }
        String format = "%s(%s)";
        if(this.isNAF && this.isClassicalNegation){
            format = "not -%s(%s)";
        }
        else if(this.isNAF){
            format = "not %s(%s)";
        }
        else if(this.isClassicalNegation){
            format = "-%s(%s)";
        }

        StringBuilder builder = new StringBuilder();
        for(Literal term : terms){
            builder.append(term.toString() + ", ");
        }

        String literalString = builder.substring(0, builder.length()-2);
        return String.format(format, this.predicate, literalString);
    }

    private static boolean IsMixedCase(String content) {
        boolean containsNumbers = content.matches(".*[0-9].*");
        boolean containsLetters = content.matches(".*[A-Za-z_].*");
        return containsNumbers && containsLetters;
    }

    @Override
    public int compareTo(Literal o) {
        return this.toString().compareTo(o.toString());
    }

    public LiteralType GetLiteralType() {
        if(this.types.contains(LiteralType.BASE_CONSTRAINT)) return LiteralType.BASE_CONSTRAINT;
        if(this.types.contains(LiteralType.ANSWER_QUERY)) return LiteralType.ANSWER_QUERY;
        if(this.types.contains(LiteralType.CONSTRAINT_QUERY)) return LiteralType.CONSTRAINT_QUERY;
        return LiteralType.FACT;
    }
}
