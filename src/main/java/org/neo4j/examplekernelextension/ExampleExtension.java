package org.neo4j.examplekernelextension;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.lifecycle.Lifecycle;

import java.util.*;
import java.util.logging.Logger;
import java.io.IOException;
import java.text.ParseException;


public class ExampleExtension implements Lifecycle {
    private final GraphDatabaseService gds;
    private final static Logger logger = Logger.getLogger(ExampleExtension.class.getName());
    private final Boolean debug;
    private final String somevar;
    private ExampleEventHandler handler;

    private static String generateErrorMessage(String detail) {
        return "ExampleExtension: " + detail;
    }

    public ExampleExtension(GraphDatabaseService gds, Boolean debug, String somevar) {
        this.gds = gds;
        this.debug = debug;
        this.somevar = somevar;
    }

    @Override
    public void init() throws Throwable {
        // creating our kernel that will be making all the work
        handler = new ExampleEventHandler(gds, debug, somevar);
        // registering our kernel event handler
        gds.registerTransactionEventHandler(handler);
        logger.info("ExampleExtension: Init");
    }

    @Override
    public void start() throws Throwable {
    }

    @Override
    public void stop() throws Throwable {
    }

    @Override
    public void shutdown() throws Throwable {
        gds.unregisterTransactionEventHandler(handler);
        logger.info("ExampleExtension: Shutdown");
    }
}
