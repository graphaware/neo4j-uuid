package ga.uuid;

import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import java.util.Map;

abstract class ProcedureIntegrationTest extends EmbeddedDatabaseIntegrationTest{

    @Override
    public void setUp() throws Exception {
        super.setUp();
        registerProcedure();
    }

    @Override
    protected String configFile() {
        return getClass().getClassLoader().getResource("neo4j-uuid-all.properties").getPath();
    }

    protected abstract Class procedureClass();

    protected void emptyDb() {
        try (Transaction tx = getDatabase().beginTx()) {
            getDatabase().execute("MATCH (n) DETACH DELETE n");
            tx.success();
        }
    }

    private void registerProcedure() throws KernelException {
        ((GraphDatabaseAPI) getDatabase()).getDependencyResolver().resolveDependency(Procedures.class).register(procedureClass());
    }
}
