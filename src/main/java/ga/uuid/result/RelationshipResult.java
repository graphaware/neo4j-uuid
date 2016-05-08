package ga.uuid.result;

import org.neo4j.graphdb.Relationship;

public class RelationshipResult {

    public final Relationship relationship;

    public RelationshipResult(Relationship relationship) {
        this.relationship = relationship;
    }

}
