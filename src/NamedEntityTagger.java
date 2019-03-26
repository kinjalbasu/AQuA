/**
 * Created by dhruv on 2/4/2018.
 */
public class NamedEntityTagger {

    public enum NamedEntityTags {
        ORGANIZATION,
        DATE,
        O
    }

    public static NamedEntityTags GetEntityTag(String tagString){
        switch (tagString){
            case "ORGANIZATION": return NamedEntityTags.ORGANIZATION;
            case "DATE": return NamedEntityTags.DATE;
        }

        return NamedEntityTags.O;
    }
}
