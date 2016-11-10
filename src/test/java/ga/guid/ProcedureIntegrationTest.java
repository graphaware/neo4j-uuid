package ga.guid;

import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;

import ga.guid.GuidProcedure;

import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

abstract class ProcedureIntegrationTest extends EmbeddedDatabaseIntegrationTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        registerProcedure();
    }

    @Override
    protected String configFile() {
        return "neo4j-guid-all.conf";
    }

    protected abstract Class<? extends GuidProcedure> procedureClass();

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
