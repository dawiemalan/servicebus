//file:noinspection unused
package dmcs.common.utils;

import com.expediagroup.beans.BeanUtils;
import com.expediagroup.beans.transformer.BeanTransformer;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DtoUtils2 {

    private static final String[] DEFAULT_IGNORE = {"metaClass", "metaMethods", "currentStatusCode", "active", "statusEffectiveDate"};
    private static final String[] ENTITY_IGNORE = {"id", "dateCreated", "lastUpdated", "createdBy", "updatedBy", "createdById", "updatedById"};

    private static final BeanUtils beanUtils = new BeanUtils();
    private static final Map<String, Boolean> ENTITY_CLASSES = new ConcurrentHashMap<>();
    private static final Set<String> ENTITY_TYPES = Set.of("EntityObject", "TimestampedEntity");

    private DtoUtils2() {
    }

    /**
     * Copies properties from source to target
     */
    public static <T> T copyProperties(Object source, T target, String... excludedProperties) {

        BeanTransformer transformer = beanUtils.getTransformer()
                .setPrimitiveTypeConversionEnabled(true)
                .setDefaultValueForMissingField(true)
                .setDefaultValueForMissingPrimitiveField(true)
                .skipTransformationForField(DEFAULT_IGNORE);

        if (excludedProperties != null)
            transformer.skipTransformationForField(excludedProperties);

        if (isEntityObject(target.getClass()))
            transformer.skipTransformationForField(ENTITY_IGNORE);

        transformer.transform(source, target);

        return target;
    }

    static boolean isEntityObject(Class<?> target) {

        Class<?> currentClass = target;
        boolean isEntity = false;

        if (ENTITY_CLASSES.containsKey(target.getName()))
            return ENTITY_CLASSES.get(target.getName());

        // check if target has @Entity annotation
        isEntity = Arrays.stream(currentClass.getAnnotations())
                .anyMatch(a -> a.annotationType().getSimpleName().equals("Entity")); // NOSONAR

        if (!isEntity) {

            while (currentClass != null) { // NOSONAR

                if (StringUtils.equals(currentClass.getSimpleName(), "Object"))
                    break;

                if (ENTITY_TYPES.contains(currentClass.getSimpleName())) {
                    isEntity = true;
                    break;
                }

                currentClass = currentClass.getSuperclass();
            }
        }

        ENTITY_CLASSES.put(target.getName(), isEntity);

        return isEntity;
    }
}
