package com.technicalitiesmc.util.module;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Throwables;
import com.technicalitiesmc.util.funcint.LambdaUtils;

import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;

public class ModuleManager<M extends IModule> implements Iterable<M> {

    private final Set<M> modules;

    @SuppressWarnings("unchecked")
    public <A extends Annotation> ModuleManager(ASMDataTable asmTable, Class<M> moduleType, Class<A> annotationType,
            BiPredicate<Class<? extends M>, A> tester, Comparator<Pair<Class<? extends M>, A>> sorter, Function<A, String> nameGetter,
            Function<A, String[]> depGetter) {
        try {
            Map<String, Pair<Class<? extends M>, A>> foundModules = new HashMap<>();

            // Find all annotated elements, check they're of the right type and load them
            for (ASMData data : asmTable.getAll(annotationType.getName())) {
                Class<?> modClass = Class.forName(data.getClassName());
                if (!moduleType.isAssignableFrom(modClass)) {
                    throw new RuntimeException("Could not load module " + modClass.getName() + ". It does not have " + moduleType.getName()
                            + " as its parent.");
                }
                Class<? extends M> clazz = (Class<? extends M>) modClass;
                A ann = modClass.getAnnotation(annotationType);
                if (tester.test(clazz, ann)) {
                    foundModules.put(nameGetter.apply(ann), Pair.of(clazz, ann));
                }
            }

            // Remove all dependants on disabled modules
            int lastCount;
            do {
                lastCount = foundModules.size();
                Iterator<Pair<Class<? extends M>, A>> it = foundModules.values().iterator();
                while (it.hasNext()) {
                    String[] deps = depGetter.apply(it.next().getValue());
                    if (deps.length > 0 && !foundModules.keySet().containsAll(Arrays.asList(deps))) {
                        it.remove();
                    }
                }
            } while (lastCount != foundModules.size());

            // Sort them
            List<Pair<Class<? extends M>, A>> list = new ArrayList<>(foundModules.values());
            list.sort(sorter);

            // Make the final set
            this.modules = list.stream() // Take all the pairs
                    .map(Pair::getKey) // Get just the key (the class)
                    .map(LambdaUtils.safeFunction(Class::newInstance)) // Instantiate it
                    .collect(Collectors.toSet()); // And make a set with all the modules
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public Set<M> getModules() {
        return modules;
    }

    @Override
    public Iterator<M> iterator() {
        return modules.iterator();
    }

}
