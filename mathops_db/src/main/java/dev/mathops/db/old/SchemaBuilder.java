package dev.mathops.db.old;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.log.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An interface for factory classes that are particular to a schema and that can generate the implementations of the
 * interfaces needed to manage each model type.
 */
public class SchemaBuilder implements ISchemaBuilder {

    /** The interface classes this builder should scan. */
    private final List<Class<? extends IDataDomainObject>> interfaceClasses;

    /** A map from model interface to the implementation object. */
    private final Map<Class<? extends IDataDomainObject>, IDataDomainObject> map;

    /**
     * Constructs a new {@code SchemaBuilder}.
     *
     * @param theInterfaceClasses the interface classes this builder should scan
     */
    protected SchemaBuilder(final Collection<Class<?>> theInterfaceClasses) {

        this.interfaceClasses = new ArrayList<>(theInterfaceClasses.size());

        for (final Class<?> cls : theInterfaceClasses) {
            if (IDataDomainObject.class.isAssignableFrom(cls)) {
                this.interfaceClasses.add((Class<? extends IDataDomainObject>) cls);
            }
        }

        this.map = new ConcurrentHashMap<>(0);
    }

    /**
     * Adds an implementation to the factory.
     *
     * @param interfaceCls the interface class whose implementation to add
     * @param impl         the implementation
     */
    @Override
    public final <T extends IDataDomainObject> void
    addImplementation(final Class<? extends T> interfaceCls, final T impl) {

        this.map.put(interfaceCls, impl);
    }

    /**
     * Gets the set of model types this factory supports.
     *
     * @return the set of implementations
     */
    @Override
    public final Set<Class<? extends IDataDomainObject>> getDataDomainObjects() {

        return Collections.unmodifiableSet(this.map.keySet());
    }

    /**
     * Gets an implementation of a specific model interface appropriate to the schema.
     *
     * @param interfaceCls the interface class whose implementation to retrieve
     * @return the implementation
     */
    @Override
    public final <T extends IDataDomainObject> T getImplementation(final Class<T> interfaceCls) {

        return (T) this.map.get(interfaceCls);
    }

    /**
     * Gets the list of implementation classes this factory supports.
     *
     * @return the list of implementations
     */
    @Override
    public final List<IDataDomainObject> getImplClasses() {

        final List<IDataDomainObject> list = new ArrayList<>(this.map.values());
        Collections.sort(list);

        return list;
    }

    /**
     * Loads the implementation classes found in the same directory as a subclass.
     */
    @Override
    public final void load() {

        for (final Class<? extends IDataDomainObject> interfaceCls : this.interfaceClasses) {
            final String simple = interfaceCls.getSimpleName();

            final String name = SimpleBuilder.concat(getClass().getPackage().getName(), ".Impl", simple.substring(1));

            try {
                final Class<?> clazz = Class.forName(name);
                final Constructor<?> constructor = clazz.getConstructor();
                final Object inst = constructor.newInstance();

                if (inst instanceof final IDataDomainObject domainObj) {
                    this.map.put(interfaceCls, domainObj);
                }
            } catch (final ClassNotFoundException ex) {
                // No action - not all interfaces need be supported by all schemata
            } catch (final NoSuchMethodException | SecurityException ex) {
                Log.warning(Res.fmt(Res.SCH_BLD_NO_CONSTRUCTOR, name));
            } catch (final InstantiationException | IllegalAccessException | IllegalArgumentException
                           | InvocationTargetException ex) {
                Log.warning(Res.fmt(Res.SCH_BLD_CANT_CONSTRUCT, name), ex);
            }
        }
    }
}
