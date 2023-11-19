package dev.mathops.db;

import java.util.List;
import java.util.Set;

/**
 * A base class for factory classes that are particular to a schema and that can generate the implementations of the
 * interfaces needed to manage each model type.
 */
public interface ISchemaBuilder {

    /**
     * Loads the implementation classes found in the same directory as a subclass.
     */
    void load();

    /**
     * Adds an implementation to the factory.
     *
     * @param <T>  the type of interface to add
     * @param type the model type whose implementation to add
     * @param impl the implementation
     */
    <T extends IDataDomainObject> void addImplementation(Class<? extends T> type, T impl);

    /**
     * Gets the set of model types this factory supports.
     *
     * @return the set of implementations
     */
    Set<Class<? extends IDataDomainObject>> getDataDomainObjects();

    /**
     * Gets an implementation of a specific model interface appropriate to the schema.
     *
     * @param <T>  the type of interface to retrieve
     * @param type the model type whose implementation to retrieve
     * @return the implementation
     */
    <T extends IDataDomainObject> T getImplementation(Class<T> type);

    /**
     * Gets the list of implementation classes this factory supports.
     *
     * @return the list of implementations
     */
    List<IDataDomainObject> getImplClasses();
}
