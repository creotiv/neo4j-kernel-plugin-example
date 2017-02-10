package org.neo4j.examplekernelextension;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.graphdb.factory.Description;
import org.neo4j.helpers.HostnamePort;
import org.neo4j.helpers.Settings;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.lifecycle.Lifecycle;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import static org.neo4j.helpers.Settings.*;


public class ExampleKernelExtensionFactory extends KernelExtensionFactory<ExampleKernelExtensionFactory.Dependencies> {

    public static final String SERVICE_NAME = "EXAMPLEKERNELEXTENSION";

    private final static Logger logger = Logger.getLogger(ExampleKernelExtensionFactory.class.getName());

    /*
        All settings can be found here: https://neo4j.com/docs/java-reference/current/javadocs/org/neo4j/graphdb/factory/GraphDatabaseSettings.html
    */

    @Description("Settings for the Example Kernel Extension")
    public static abstract class ExampleSettings {
        public static Setting<Boolean> debug = setting("examplekernelextension.debug", BOOLEAN, Settings.FALSE);
        public static Setting<String> somevar = setting("examplekernelextension.somevar", STRING, (String) null);
    }

    public ExampleKernelExtensionFactory() {
        super(SERVICE_NAME);
    }

    @Override
    public Lifecycle newKernelExtension(Dependencies dependencies) throws Throwable {
        Config config = dependencies.getConfig();

        return new ExampleExtension(
            dependencies.getGraphDatabaseService(),
            config.get(ExampleSettings.debug),
            config.get(ExampleSettings.somevar)
        );
    }

    public interface Dependencies {
        GraphDatabaseService getGraphDatabaseService();

        Config getConfig();
    }
}
